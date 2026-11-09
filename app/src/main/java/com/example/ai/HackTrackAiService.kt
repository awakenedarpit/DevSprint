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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "HackTrackAi"

class HackTrackAiService {

    // Lazy initialization per firebase-ai-logic-android to avoid startup overhead
    private val model: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
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
     * Automatically extracts hackathon and competition details from a website URL.
     */
    suspend fun extractHackathonFromUrl(url: String): Hackathon = withContext(Dispatchers.IO) {
        val pageText = fetchPageContent(url)

        val prompt = """
            You are HackTrack AI. Extract competition and hackathon metadata from this link and web page content.
            
            Website URL: $url
            Page Content Snippet:
            $pageText
            
            Return ONLY a valid JSON object without markdown formatting, code fences, or additional text:
            {
              "name": "Competition Name (or derived from URL if not found)",
              "organizer": "Organizer or Host Organization",
              "domain": "Domain or Category (e.g., AI / Web3 / Fintech / EdTech / Open Innovation)",
              "mode": "Online or Offline or Hybrid",
              "description": "Brief 1-2 sentence description",
              "problemStatement": "Problem statement or challenge theme",
              "submissionDeadline": "e.g., Dec 15, 2026 (or estimated if unknown)",
              "registrationDeadline": "e.g., Dec 01, 2026",
              "prizeInfo": "e.g., $10,000 Prize Pool or Cloud Credits"
            }
        """.trimIndent()

        val rawResponse = try {
            withTimeout(45_000L) {
                model.generateContent(prompt).text.orEmpty()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Extraction failed: ${e.message}", e)
            ""
        }

        // Clean any accidental markdown code fences
        val cleanJson = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .trim()

        if (cleanJson.isNotBlank() && cleanJson.startsWith("{")) {
            try {
                val json = JSONObject(cleanJson)
                return@withContext Hackathon(
                    name = json.optString("name", "New Hackathon"),
                    organizer = json.optString("organizer", "Hackathon Host"),
                    websiteUrl = url,
                    registrationUrl = url,
                    domain = json.optString("domain", "AI / Web / Mobile"),
                    mode = json.optString("mode", "Online"),
                    description = json.optString("description", "Imported from $url"),
                    problemStatement = json.optString("problemStatement", ""),
                    submissionDeadline = json.optString("submissionDeadline", ""),
                    registrationDeadline = json.optString("registrationDeadline", ""),
                    prizeInfo = json.optString("prizeInfo", "Prize Pool & Certificates"),
                    status = "Registered"
                )
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing error on extracted data: ${e.message}")
            }
        }

        // Fallback if model returned plain text or fetch failed
        val domainName = url.substringAfter("://").substringBefore("/").replace("www.", "")
        val pathSnippet = url.substringAfterLast("/").replace("-", " ").replace("_", " ").capitalizeWords()
        val fallbackName = if (pathSnippet.isNotBlank() && pathSnippet.length > 3) pathSnippet else "Hackathon ($domainName)"

        Hackathon(
            name = fallbackName,
            organizer = domainName.split(".").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Organizer",
            websiteUrl = url,
            registrationUrl = url,
            domain = "Technology & Innovation",
            mode = "Online",
            description = "Imported competition from $url",
            status = "Registered"
        )
    }

    private fun fetchPageContent(url: String): String {
        return try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val request = Request.Builder()
                .url(validUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return "HTTP ${response.code}"
                val body = response.body?.string().orEmpty()
                // Strip scripts, styles, and HTML tags for compact token usage
                body.replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .take(15000)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Page fetch warning: ${e.message}")
            "URL: $url"
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

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
