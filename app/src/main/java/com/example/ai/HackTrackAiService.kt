package com.example.ai

import android.util.Log
import com.example.model.Hackathon
import com.example.model.Project
import com.example.model.TaskItem
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withTimeout

private const val TAG = "HackTrackAi"

class HackTrackAiService {

    // Lazy initialization per firebase-ai-logic-android to avoid startup overhead
    private val model: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    /**
     * Streams generative response for interactive AI advice, roadmap generation, or problem analysis.
     */
    fun streamAssistance(
        userPrompt: String,
        activeHackathons: List<Hackathon>,
        activeProjects: List<Project>,
        pendingTasks: List<TaskItem>
    ): Flow<String> = flow {
        val contextBuilder = StringBuilder()
        contextBuilder.append("You are 'HackTrack AI', an elite hackathon mentor, software architect, and competition strategist.\n")
        contextBuilder.append("Here is the user's active competition command center data:\n")

        if (activeHackathons.isNotEmpty()) {
            contextBuilder.append("\n=== Active Hackathons ===\n")
            activeHackathons.take(4).forEach { h ->
                contextBuilder.append("- ${h.name} (${h.organizer}) | Status: ${h.status} | Mode: ${h.mode} | Submission Deadline: ${h.submissionDeadline}\n")
                if (h.problemStatement.isNotBlank()) {
                    contextBuilder.append("  Problem: ${h.problemStatement}\n")
                }
            }
        }

        if (activeProjects.isNotEmpty()) {
            contextBuilder.append("\n=== Current Projects ===\n")
            activeProjects.take(3).forEach { p ->
                contextBuilder.append("- ${p.title} (Status: ${p.status}, Tech: ${p.techStack})\n")
            }
        }

        if (pendingTasks.isNotEmpty()) {
            contextBuilder.append("\n=== Pending Tasks ===\n")
            pendingTasks.filter { it.status != "Completed" }.take(6).forEach { t ->
                contextBuilder.append("- [${t.priority}] ${t.title} (Due: ${t.dueDate}, Assignee: ${t.assignedMember})\n")
            }
        }

        contextBuilder.append("\nInstructions:\n")
        contextBuilder.append("1. Provide actionable, concise, bulleted advice with clear milestones or checklists.\n")
        contextBuilder.append("2. Ground your advice in the user's actual competitions and projects.\n")
        contextBuilder.append("3. Format clearly with headers, bold keywords, and bullet points.\n\n")
        contextBuilder.append("User Query: $userPrompt\n")

        val prompt = contextBuilder.toString()

        try {
            val stream = model.generateContentStream(prompt).mapNotNull { it.text }
            emitAll(stream)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini stream error: ${e.message}", e)
            emit("\n[Notice: AI response interrupted or App Check initializing: ${e.localizedMessage ?: "Network error"}. Please retry in a moment.]")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Breaks down a hackathon problem statement into structured actionable tasks.
     */
    suspend fun breakDownProblemStatement(problemStatement: String): String {
        val prompt = """
            You are HackTrack AI. Break down the following hackathon problem statement into a 48-hour execution sprint plan:
            
            Problem Statement:
            "$problemStatement"
            
            Provide:
            1. 🎯 Core Value Proposition & Scope (Keep it MVF - Minimum Viable Feature)
            2. 🛠 Recommended Tech Architecture
            3. 📋 Step-by-Step Task Breakdown:
               - Phase 1: Hours 0-12 (Data & Core Backend)
               - Phase 2: Hours 12-28 (Frontend & Integration)
               - Phase 3: Hours 28-40 (Testing & Edge Case Polish)
               - Phase 4: Hours 40-48 (Pitch Deck, Demo Video, & Submission Form)
            4. 🏆 What Hackathon Judges Look For in this domain
        """.trimIndent()

        return try {
            withTimeout(60_000L) {
                val response = model.generateContent(prompt)
                response.text ?: "Could not generate breakdown. Please try again."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Breakdown error: ${e.message}", e)
            "AI breakdown error: ${e.localizedMessage}. Please verify network connectivity."
        }
    }

    /**
     * Generates a 10-slide Pitch Deck Outline for the project.
     */
    suspend fun generatePitchDeckOutline(projectName: String, problem: String, solution: String): String {
        val prompt = """
            You are HackTrack AI. Prepare a winning 10-slide Pitch Deck Outline for a hackathon demo:
            Project: $projectName
            Problem: $problem
            Solution: $solution
            
            Format each slide:
            Slide # - Slide Title:
            - Key message (1 sentence)
            - Visual element recommendation (graph, mockup, animation)
            - What to say during 30-second presentation
        """.trimIndent()

        return try {
            withTimeout(60_000L) {
                val response = model.generateContent(prompt)
                response.text ?: "Could not generate pitch deck outline."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Pitch deck outline error: ${e.message}", e)
            "Error generating pitch outline: ${e.localizedMessage}"
        }
    }
}
