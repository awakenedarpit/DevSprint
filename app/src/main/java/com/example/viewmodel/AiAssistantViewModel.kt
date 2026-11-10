package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.HackTrackAiService
import com.example.model.Hackathon
import com.example.model.Project
import com.example.model.TaskItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val content: String,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class AiAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val aiService = HackTrackAiService(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                content = "👋 **Welcome to Dev Sprint AI!** I am your hackathon mentor, architect, and strategy engine.\n\nAsk me to:\n- ⚡ **Break down problem statements** into MVP features\n- ⏱️ **Generate a 48-Hour sprint roadmap**\n- 📑 **Review and outline your pitch deck**\n- 🎯 **Prioritize your weekly submissions and deadlines**"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun sendMessage(
        query: String,
        activeHackathons: List<Hackathon>,
        activeProjects: List<Project>,
        pendingTasks: List<TaskItem>
    ) {
        if (query.isBlank() || _isGenerating.value) return

        val userMsg = ChatMessage(sender = "user", content = query.trim())
        val aiMsgId = UUID.randomUUID().toString()
        val initialAiMsg = ChatMessage(id = aiMsgId, sender = "ai", content = "", isStreaming = true)

        _messages.update { it + userMsg + initialAiMsg }
        _isGenerating.value = true

        viewModelScope.launch {
            val responseAccumulator = StringBuilder()
            try {
                aiService.streamAssistance(
                    userPrompt = query,
                    activeHackathons = activeHackathons,
                    activeProjects = activeProjects,
                    pendingTasks = pendingTasks
                ).collect { chunk ->
                    responseAccumulator.append(chunk)
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == aiMsgId) {
                                msg.copy(content = responseAccumulator.toString(), isStreaming = true)
                            } else msg
                        }
                    }
                }
            } catch (e: Exception) {
                if (responseAccumulator.isEmpty()) {
                    responseAccumulator.append("I encountered an issue generating a response: ${e.localizedMessage}. Please try again.")
                }
            } finally {
                _messages.update { list ->
                    list.map { msg ->
                        if (msg.id == aiMsgId) {
                            msg.copy(content = responseAccumulator.toString(), isStreaming = false)
                        } else msg
                    }
                }
                _isGenerating.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                sender = "ai",
                content = "Chat cleared! How can I help you dominate your next hackathon?"
            )
        )
    }
}
