package com.example

import com.example.data.SampleDataProvider
import com.example.model.DashboardStats
import org.junit.Assert.*
import org.junit.Test

class HackTrackUnitTest {

    @Test
    fun testSampleDataIntegrity() {
        val testUid = "test_user_123"
        val hackathons = SampleDataProvider.getDefaultHackathons(testUid)
        val tasks = SampleDataProvider.getDefaultTasks(testUid)
        val projects = SampleDataProvider.getDefaultProjects(testUid)
        val submissions = SampleDataProvider.getDefaultSubmissions(testUid)
        val achievements = SampleDataProvider.getDefaultAchievements(testUid)

        assertTrue(hackathons.isNotEmpty())
        assertTrue(tasks.isNotEmpty())
        assertTrue(projects.isNotEmpty())
        assertTrue(submissions.isNotEmpty())
        assertTrue(achievements.isNotEmpty())

        hackathons.forEach { h ->
            assertEquals(testUid, h.userId)
            assertTrue(h.name.isNotBlank())
            assertTrue(h.organizer.isNotBlank())
        }
    }

    @Test
    fun testDashboardStatsComputation() {
        val hackathons = SampleDataProvider.getDefaultHackathons("u1")
        val tasks = SampleDataProvider.getDefaultTasks("u1")
        val submissions = SampleDataProvider.getDefaultSubmissions("u1")
        val achievements = SampleDataProvider.getDefaultAchievements("u1")

        val active = hackathons.count { it.status in listOf("Registered", "Building", "Finalist", "Shortlisted") && !it.isArchived }
        val pending = tasks.count { it.status != "Completed" }
        val completedSubs = submissions.count { it.status in listOf("Completed", "Submitted", "Verified") }
        val wins = hackathons.count { it.status == "Winner" } + achievements.count { it.result.contains("Winner", ignoreCase = true) }

        val stats = DashboardStats(
            totalHackathons = hackathons.size,
            activeHackathons = active,
            pendingTasks = pending,
            completedSubmissions = completedSubs,
            shortlistedCount = hackathons.count { it.status in listOf("Shortlisted", "Finalist") },
            totalWins = wins
        )

        assertEquals(4, stats.totalHackathons)
        assertTrue(stats.activeHackathons >= 2)
        assertTrue(stats.pendingTasks >= 3)
        assertTrue(stats.totalWins >= 1)
    }
}
