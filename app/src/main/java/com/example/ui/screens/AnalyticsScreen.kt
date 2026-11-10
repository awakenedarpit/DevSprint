package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Hackathon
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.HackTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HackTrackViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToHackathons: () -> Unit,
    onNavigateToAiAssistant: () -> Unit
) {
    val hackathons by viewModel.hackathons.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    var selectedHackathonFilterId by remember { mutableStateOf<String?>(null) }

    // Filter tasks based on selected hackathon (or all)
    val scopedTasks = remember(tasks, selectedHackathonFilterId) {
        if (selectedHackathonFilterId == null) tasks
        else tasks.filter { it.hackathonId == selectedHackathonFilterId }
    }

    // Task status counts
    val completedCount = scopedTasks.count { it.status == "Completed" }
    val inProgressCount = scopedTasks.count { it.status == "In Progress" }
    val todoCount = scopedTasks.count { it.status == "To Do" }
    val backlogCount = scopedTasks.count { it.status == "Backlog" }
    val blockedCount = scopedTasks.count { it.status == "Blocked" }
    val underReviewCount = scopedTasks.count { it.status == "Under Review" }
    val totalTasks = scopedTasks.size

    val taskCompletionRate = if (totalTasks > 0) (completedCount * 100) / totalTasks else 0

    // Priority counts
    val criticalCount = scopedTasks.count { it.priority == "Critical" }
    val highCount = scopedTasks.count { it.priority == "High" }
    val mediumCount = scopedTasks.count { it.priority == "Medium" }
    val lowCount = scopedTasks.count { it.priority == "Low" }

    // Competition statistics
    val totalCompetitions = hackathons.size
    val activeCompetitions = hackathons.count { it.status in listOf("Building", "Registered", "Shortlisted", "Finalist") }
    val totalWins = achievements.count { it.result in listOf("Winner", "1st Place", "2nd Place", "3rd Place") }
    val winRate = if (totalCompetitions > 0) (totalWins * 100) / totalCompetitions else 0

    // Sprint Health calculation: based on completion rate and absence of blocked tasks
    val sprintHealthScore = remember(totalTasks, completedCount, blockedCount) {
        if (totalTasks == 0) 100
        else {
            val base = (completedCount.toFloat() / totalTasks) * 80f + 20f
            val penalty = (blockedCount * 10f).coerceAtMost(30f)
            (base - penalty).toInt().coerceIn(10, 100)
        }
    }

    val sprintStatusMessage = when {
        sprintHealthScore >= 80 -> "Optimal Sprint Momentum 🚀"
        sprintHealthScore >= 55 -> "Moderate Velocity — Review Blockers"
        else -> "Sprint Warning — Critical Tasks Overdue"
    }

    // Chart Slices for Task Status Donut Chart
    val taskStatusSlices = remember(completedCount, inProgressCount, todoCount, backlogCount, blockedCount, underReviewCount) {
        listOf(
            ChartSlice("completed", "Completed", completedCount.toFloat(), HackTrackSuccess),
            ChartSlice("in_progress", "In Progress", inProgressCount.toFloat(), HackTrackPrimary),
            ChartSlice("under_review", "Under Review", underReviewCount.toFloat(), Color(0xFF8B5CF6)),
            ChartSlice("todo", "To Do", todoCount.toFloat(), Color(0xFF0284C7)),
            ChartSlice("backlog", "Backlog", backlogCount.toFloat(), HackTrackWarning),
            ChartSlice("blocked", "Blocked", blockedCount.toFloat(), HackTrackDanger)
        ).filter { it.value > 0f || totalTasks == 0 }
    }

    // Priority Bar Items
    val priorityBars = remember(criticalCount, highCount, mediumCount, lowCount) {
        listOf(
            BarItemData("Critical", criticalCount.toFloat(), "$criticalCount", HackTrackDanger),
            BarItemData("High", highCount.toFloat(), "$highCount", HackTrackWarning),
            BarItemData("Medium", mediumCount.toFloat(), "$mediumCount", HackTrackPrimary),
            BarItemData("Low", lowCount.toFloat(), "$lowCount", Color(0xFF10B981))
        )
    }

    // Competition Lifecycle Funnel stages
    val funnelStages = remember(hackathons, achievements) {
        listOf(
            "Registered" to hackathons.count { it.status !in listOf("Interested", "Withdrawn") },
            "Building (Active)" to hackathons.count { it.status in listOf("Building", "Team Formation") },
            "Submitted" to hackathons.count { it.status in listOf("Submitted", "Finalist", "Winner", "Completed") },
            "Shortlisted / Finalist" to hackathons.count { it.status in listOf("Shortlisted", "Finalist", "Winner") },
            "Winners & Podiums" to achievements.size
        )
    }

    // Domain distribution
    val domainDistribution = remember(hackathons) {
        val domains = hackathons.groupBy { it.domain.ifBlank { "General Tech" } }
        val colors = listOf(
            HackTrackPrimary,
            HackTrackSecondary,
            Color(0xFF06B6D4),
            HackTrackSuccess,
            HackTrackWarning,
            Color(0xFFEC4899)
        )
        domains.entries.take(5).mapIndexed { idx, entry ->
            ChartSlice(
                id = entry.key,
                label = entry.key,
                value = entry.value.size.toFloat(),
                color = colors[idx % colors.size]
            )
        }
    }

    // Competition Mode Distribution
    val modeDistribution = remember(hackathons) {
        listOf(
            ChartSlice("online", "Online", hackathons.count { it.mode.equals("Online", ignoreCase = true) }.toFloat(), HackTrackPrimary),
            ChartSlice("offline", "In-Person", hackathons.count { it.mode.equals("Offline", ignoreCase = true) }.toFloat(), HackTrackSuccess),
            ChartSlice("hybrid", "Hybrid", hackathons.count { it.mode.equals("Hybrid", ignoreCase = true) }.toFloat(), HackTrackWarning)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Scope & Competition Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Analytics Scope",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedHackathonFilterId == null,
                            onClick = { selectedHackathonFilterId = null },
                            label = { Text("All Competitions (${hackathons.size})") },
                            leadingIcon = if (selectedHackathonFilterId == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    items(hackathons) { h ->
                        FilterChip(
                            selected = selectedHackathonFilterId == h.id,
                            onClick = {
                                selectedHackathonFilterId = if (selectedHackathonFilterId == h.id) null else h.id
                            },
                            label = { Text(h.name) },
                            leadingIcon = if (selectedHackathonFilterId == h.id) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Executive Sprint Velocity & Health Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sprint Health & Momentum",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sprintHealthScore >= 75) HackTrackSuccess.copy(alpha = 0.15f)
                            else HackTrackWarning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (sprintHealthScore >= 75) "On Track" else "Needs Attention",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (sprintHealthScore >= 75) HackTrackSuccess else HackTrackWarning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 240-degree Radial Health Gauge
                    SprintVelocityHealthGauge(
                        score = sprintHealthScore,
                        statusMessage = sprintStatusMessage
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 3 KPI Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$taskCompletionRate%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = HackTrackSuccess
                            )
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(32.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$inProgressCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = HackTrackPrimary
                            )
                            Text(
                                text = "In Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(32.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$blockedCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (blockedCount > 0) HackTrackDanger else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Blockers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Cards Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsMetricCard(
                        title = "Win Rate",
                        value = "$winRate%",
                        subtitle = "$totalWins Podium Wins",
                        icon = Icons.Default.EmojiEvents,
                        iconColor = Color(0xFFEAB308),
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsMetricCard(
                        title = "Active Sprints",
                        value = "$activeCompetitions",
                        subtitle = "$totalCompetitions Total Enrolled",
                        icon = Icons.Default.RocketLaunch,
                        iconColor = HackTrackPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsMetricCard(
                        title = "Submissions",
                        value = "${submissions.size}",
                        subtitle = "${submissions.count { it.status == "Submitted" || it.status == "Verified" }} Completed",
                        icon = Icons.Default.CloudDone,
                        iconColor = HackTrackSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsMetricCard(
                        title = "Projects Built",
                        value = "${projects.size}",
                        subtitle = "${projects.count { it.status == "Completed" || it.status == "Prototype Ready" }} Ready",
                        icon = Icons.Default.Layers,
                        iconColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Task Status Distribution (Interactive Donut Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Task Status Distribution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap any segment or legend to inspect breakdown",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(onClick = onNavigateToTasks) {
                            Text("View Tasks")
                        }
                    }

                    if (scopedTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks found for this scope. Add tasks to see breakdown.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        InteractiveDonutChart(
                            slices = taskStatusSlices,
                            centerTitle = "Total Tasks",
                            centerSubtitle = "$taskCompletionRate% Done"
                        )
                    }
                }
            }
        }

        // Task Priority Matrix (Bar Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Task Priority Matrix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Workload distribution across urgency levels",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (criticalCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HackTrackDanger.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$criticalCount Critical",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HackTrackDanger,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    AnimatedBarChart(
                        items = priorityBars,
                        barMaxHeight = 120.dp
                    )
                }
            }
        }

        // Competition Lifecycle Funnel (Funnel Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Competition Lifecycle Funnel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Conversion from Registered to Podiums",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(onClick = onNavigateToHackathons) {
                            Text("View All")
                        }
                    }

                    ConversionFunnelView(stages = funnelStages)
                }
            }
        }

        // Domain & Mode Distribution Card
        if (hackathons.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Domain & Participation Modes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Top Technology Tracks:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        SegmentedDistributionBar(segments = domainDistribution)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Event Mode Distribution (Online / In-Person / Hybrid):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        SegmentedDistributionBar(segments = modeDistribution)
                    }
                }
            }
        }

        // AI Sprint Advisor Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dev Sprint AI Strategy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Analyze your bottlenecks and generate a 48h sprint action plan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToAiAssistant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ask AI")
                    }
                }
            }
        }
    }
}
