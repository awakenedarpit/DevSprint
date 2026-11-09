package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "Student Innovator",
    val email: String = "",
    val college: String = "Engineering & Technology Institute",
    val course: String = "Computer Science & Engineering",
    val year: String = "3rd Year",
    val bio: String = "Passionate full-stack hacker building AI & web applications.",
    val avatarUrl: String = "",
    val timezone: String = "GMT+5:30",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Hackathon(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val organizer: String = "",
    val websiteUrl: String = "",
    val registrationUrl: String = "",
    val description: String = "",
    val problemStatement: String = "",
    val domain: String = "AI / Web3 / Fintech",
    val mode: String = "Hybrid", // Online, Offline, Hybrid
    val teamName: String = "CodeCrafters",
    val teamSize: Int = 4,
    val status: String = "Building", // Interested, Registered, Building, Submitted, Finalist, Winner, Runner-up, Completed
    val registrationDate: String = "",
    val registrationDeadline: String = "",
    val submissionDeadline: String = "",
    val finalEventDate: String = "",
    val prizeInfo: String = "$10,000 Pool",
    val isArchived: Boolean = false,
    val rounds: List<HackathonRound> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HackathonRound(
    val id: String = "",
    val hackathonId: String = "",
    val title: String = "",
    val roundOrder: Int = 1,
    val roundDate: String = "",
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@IgnoreExtraProperties
data class CustomDeadline(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hackathonId: String = "",
    val title: String = "",
    val competitionName: String = "",
    val date: String = "",
    val type: String = "Submission Deadline",
    val isCritical: Boolean = false,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TaskItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hackathonId: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Development", // Ideation, Frontend, Backend, AI/ML, Design, Pitch
    val priority: String = "High", // Critical, High, Medium, Low
    val status: String = "In Progress", // Backlog, To Do, In Progress, Under Review, Completed, Blocked
    val dueDate: String = "",
    val assignedMember: String = "Me",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Project(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hackathonId: String = "",
    val title: String = "",
    val description: String = "",
    val problemStatement: String = "",
    val proposedSolution: String = "",
    val techStack: String = "Kotlin, Jetpack Compose, Firebase, Gemini API",
    val githubRepo: String = "https://github.com/example/project",
    val liveDeployment: String = "",
    val demoVideo: String = "",
    val documentation: String = "",
    val status: String = "Development", // Idea, Research, Planning, Development, Testing, Prototype Ready, Completed
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SubmissionItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hackathonId: String = "",
    val title: String = "",
    val deliverableType: String = "Pitch Deck", // Pitch Deck, GitHub Repo, Demo Video, Prototype URL, Executive Summary
    val fileUrl: String = "",
    val externalUrl: String = "",
    val notes: String = "",
    val status: String = "In Progress", // Pending, In Progress, Completed, Submitted, Verified
    val submittedAt: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class TeamMember(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val college: String = "",
    val role: String = "Fullstack Developer", // Team Lead, Frontend Developer, Backend Developer, AI/ML Engineer, UI/UX Designer
    val skills: String = "Kotlin, Python, React",
    val responsibilities: String = "Architecture and core APIs",
    val contactInfo: String = ""
)

@IgnoreExtraProperties
data class Achievement(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hackathonId: String = "",
    val competitionName: String = "",
    val projectName: String = "",
    val result: String = "Winner", // Winner, 1st Runner-Up, 2nd Runner-Up, Finalist, Top 10, Special Recognition
    val rank: String = "1st Place",
    val date: String = "",
    val organizer: String = "",
    val certificateUrl: String = "",
    val proofUrl: String = "",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// Statistics summary model for dashboard
data class DashboardStats(
    val totalHackathons: Int = 0,
    val activeHackathons: Int = 0,
    val pendingTasks: Int = 0,
    val completedSubmissions: Int = 0,
    val shortlistedCount: Int = 0,
    val totalWins: Int = 0
)
