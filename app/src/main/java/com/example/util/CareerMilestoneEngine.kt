package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.*
import com.example.ui.theme.*

data class CareerMilestoneBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val targetCount: Int,
    val currentCount: Int,
    val isUnlocked: Boolean,
    val unlockedAt: String? = null,
    val xpReward: Int,
    val category: String // "Participation", "Sprints", "Submissions", "Awards"
)

data class CareerProgressSummary(
    val totalHackathons: Int,
    val totalTasksCompleted: Int,
    val totalSubmissions: Int,
    val totalWins: Int,
    val totalXp: Int,
    val rankTitle: String,
    val rankLevel: Int,
    val currentLevelXp: Int,
    val nextLevelTargetXp: Int,
    val levelProgressFraction: Float,
    val trajectoryPoints: List<Pair<String, Int>>,
    val badges: List<CareerMilestoneBadge>
)

object CareerMilestoneEngine {

    fun calculateCareerProgress(
        hackathons: List<Hackathon>,
        tasks: List<TaskItem>,
        submissions: List<SubmissionItem>,
        achievements: List<Achievement>
    ): CareerProgressSummary {
        val totalHacks = hackathons.size
        val completedTasks = tasks.count { it.status == "Completed" }
        val completedSubs = submissions.count { it.status in listOf("Completed", "Submitted", "Verified") }
        val totalWins = achievements.count { it.result in listOf("Winner", "1st Place", "2nd Place", "3rd Place") }

        // XP System:
        // 100 XP per hackathon
        // 25 XP per completed sprint task
        // 150 XP per submitted deliverable
        // 350 XP per podium win
        val totalXp = (totalHacks * 100) + (completedTasks * 25) + (completedSubs * 150) + (totalWins * 350)

        // Hacker Rank Tiers
        val (rankTitle, rankLevel, minLevelXp, maxLevelXp) = when {
            totalXp < 250 -> Quad("Novice Hacker", 1, 0, 250)
            totalXp < 650 -> Quad("Sprint Builder", 2, 250, 650)
            totalXp < 1300 -> Quad("Hackathon Veteran", 3, 650, 1300)
            totalXp < 2400 -> Quad("Podium Champion", 4, 1300, 2400)
            else -> Quad("Grandmaster Innovator", 5, 2400, 4000)
        }

        val levelSpan = (maxLevelXp - minLevelXp).coerceAtLeast(1)
        val currentLevelXp = (totalXp - minLevelXp).coerceAtLeast(0)
        val levelProgressFraction = (currentLevelXp.toFloat() / levelSpan.toFloat()).coerceIn(0.05f, 1f)

        // Distinct domains count
        val distinctDomains = hackathons.map { it.domain.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size

        // Max team size
        val maxTeamSize = hackathons.maxOfOrNull { it.teamSize } ?: 1

        // Badges list
        val badges = listOf(
            CareerMilestoneBadge(
                id = "b_first_launch",
                title = "First Launch",
                description = "Enrolled in your first hackathon competition.",
                icon = Icons.Default.RocketLaunch,
                badgeColor = HackTrackPrimary,
                targetCount = 1,
                currentCount = totalHacks,
                isUnlocked = totalHacks >= 1,
                unlockedAt = if (totalHacks >= 1) "Season Kickoff" else null,
                xpReward = 100,
                category = "Participation"
            ),
            CareerMilestoneBadge(
                id = "b_sprint_starter",
                title = "Sprint Engine",
                description = "Completed 5 sprint backlog tasks.",
                icon = Icons.Default.Checklist,
                badgeColor = Color(0xFF0284C7),
                targetCount = 5,
                currentCount = completedTasks,
                isUnlocked = completedTasks >= 5,
                unlockedAt = if (completedTasks >= 5) "Sprint Active" else null,
                xpReward = 150,
                category = "Sprints"
            ),
            CareerMilestoneBadge(
                id = "b_sprint_velocity",
                title = "Sprint Velocity",
                description = "Completed 15+ sprint tasks with zero blockers.",
                icon = Icons.Default.Bolt,
                badgeColor = Color(0xFFEAB308),
                targetCount = 15,
                currentCount = completedTasks,
                isUnlocked = completedTasks >= 15,
                unlockedAt = if (completedTasks >= 15) "High Momentum" else null,
                xpReward = 300,
                category = "Sprints"
            ),
            CareerMilestoneBadge(
                id = "b_deliverable_shipped",
                title = "Project Shipped",
                description = "Prepared and submitted a verified deliverable.",
                icon = Icons.Default.CloudDone,
                badgeColor = HackTrackSuccess,
                targetCount = 1,
                currentCount = completedSubs,
                isUnlocked = completedSubs >= 1,
                unlockedAt = if (completedSubs >= 1) "Submitted" else null,
                xpReward = 200,
                category = "Submissions"
            ),
            CareerMilestoneBadge(
                id = "b_podium_glory",
                title = "Podium Glory",
                description = "Secured 1st, 2nd, or 3rd place award in a hackathon.",
                icon = Icons.Default.EmojiEvents,
                badgeColor = Color(0xFFF59E0B),
                targetCount = 1,
                currentCount = totalWins,
                isUnlocked = totalWins >= 1,
                unlockedAt = if (totalWins >= 1) "Podium Champion" else null,
                xpReward = 500,
                category = "Awards"
            ),
            CareerMilestoneBadge(
                id = "b_veteran",
                title = "Hackathon Veteran",
                description = "Participated in 3 or more hackathon competitions.",
                icon = Icons.Default.MilitaryTech,
                badgeColor = Color(0xFF8B5CF6),
                targetCount = 3,
                currentCount = totalHacks,
                isUnlocked = totalHacks >= 3,
                unlockedAt = if (totalHacks >= 3) "Veteran Status" else null,
                xpReward = 350,
                category = "Participation"
            ),
            CareerMilestoneBadge(
                id = "b_polymath",
                title = "Polymath Explorer",
                description = "Competed in projects across 2+ distinct technology domains.",
                icon = Icons.Default.Psychology,
                badgeColor = Color(0xFFEC4899),
                targetCount = 2,
                currentCount = distinctDomains,
                isUnlocked = distinctDomains >= 2,
                unlockedAt = if (distinctDomains >= 2) "Cross-Disciplinary" else null,
                xpReward = 250,
                category = "Participation"
            ),
            CareerMilestoneBadge(
                id = "b_squad",
                title = "Squad Goals",
                description = "Formed and led a collaboration team of 3+ members.",
                icon = Icons.Default.Groups,
                badgeColor = Color(0xFF06B6D4),
                targetCount = 3,
                currentCount = maxTeamSize,
                isUnlocked = maxTeamSize >= 3,
                unlockedAt = if (maxTeamSize >= 3) "Team Formed" else null,
                xpReward = 200,
                category = "Submissions"
            )
        )

        // Trajectory progression points (Step progression curve across season milestones)
        val trajectoryPoints = listOf(
            "Sprint 1" to (totalHacks * 20).coerceAtLeast(10),
            "Sprint 2" to ((totalHacks * 35) + (completedTasks * 10)).coerceAtLeast(25),
            "Mid-Season" to ((totalHacks * 50) + (completedTasks * 18) + (completedSubs * 30)).coerceAtLeast(50),
            "Finals" to ((totalHacks * 75) + (completedTasks * 22) + (completedSubs * 80) + (totalWins * 120)).coerceAtLeast(85),
            "Current" to totalXp.coerceAtLeast(100)
        )

        return CareerProgressSummary(
            totalHackathons = totalHacks,
            totalTasksCompleted = completedTasks,
            totalSubmissions = completedSubs,
            totalWins = totalWins,
            totalXp = totalXp,
            rankTitle = rankTitle,
            rankLevel = rankLevel,
            currentLevelXp = currentLevelXp,
            nextLevelTargetXp = maxLevelXp,
            levelProgressFraction = levelProgressFraction,
            trajectoryPoints = trajectoryPoints,
            badges = badges
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
