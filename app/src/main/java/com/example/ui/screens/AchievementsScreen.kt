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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.CareerMilestoneEngine
import com.example.viewmodel.HackTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: HackTrackViewModel,
    onOpenAddDialog: () -> Unit
) {
    val hackathons by viewModel.hackathons.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Career & Milestones", "Podium Trophies (${achievements.size})")

    // Calculate real-time career progression summary & badges
    val careerSummary = remember(hackathons, tasks, submissions, achievements) {
        CareerMilestoneEngine.calculateCareerProgress(
            hackathons = hackathons,
            tasks = tasks,
            submissions = submissions,
            achievements = achievements
        )
    }

    var selectedBadgeFilter by remember { mutableStateOf("All") }
    val badgeFilterOptions = listOf("All", "Unlocked", "In Progress", "Participation", "Sprints", "Awards")

    val filteredBadges = remember(careerSummary.badges, selectedBadgeFilter) {
        when (selectedBadgeFilter) {
            "All" -> careerSummary.badges
            "Unlocked" -> careerSummary.badges.filter { it.isUnlocked }
            "In Progress" -> careerSummary.badges.filter { !it.isUnlocked }
            else -> careerSummary.badges.filter { it.category.equals(selectedBadgeFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = onOpenAddDialog,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Trophy")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Primary Segmented Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.MilitaryTech else Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            if (showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showCopiedSnackbar = false }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text("Portfolio summary copied to clipboard!")
                }
            }

            if (selectedTabIndex == 0) {
                // TAB 1: Career & Milestone Tracker
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    // 1. Career Level & XP Card
                    item {
                        CareerLevelXpCard(summary = careerSummary)
                    }

                    // 2. Trajectory Growth Curve Chart
                    item {
                        CareerTrajectoryCurveChart(points = careerSummary.trajectoryPoints)
                    }

                    // 3. Badges Header & Filter Chips
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = HackTrackPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Achievement Badges",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HackTrackSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${careerSummary.badges.count { it.isUnlocked }} / ${careerSummary.badges.size} Unlocked",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HackTrackSuccess,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(badgeFilterOptions) { filter ->
                                    FilterChip(
                                        selected = selectedBadgeFilter == filter,
                                        onClick = { selectedBadgeFilter = filter },
                                        label = { Text(filter) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Milestone Badge Cards
                    items(filteredBadges, key = { it.id }) { badge ->
                        MilestoneBadgeCard(badge = badge)
                    }
                }
            } else {
                // TAB 2: Podium Trophies & Verified Distinctions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    // Header summary card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Hall of Fame & Portfolio",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${achievements.size} Verified Wins & Distinctions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            val summary = buildString {
                                                append("🏆 HACKATHON PORTFOLIO SUMMARY\n\n")
                                                append("Hacker Rank: ${careerSummary.rankTitle} (Level ${careerSummary.rankLevel})\n")
                                                append("Total Competitions: ${careerSummary.totalHackathons} | Wins: ${careerSummary.totalWins}\n\n")
                                                achievements.forEach { ach ->
                                                    append("• ${ach.competitionName} - ${ach.result} (${ach.rank})\n")
                                                    append("  Project: ${ach.projectName} | Organizer: ${ach.organizer}\n")
                                                    append("  ${ach.description}\n\n")
                                                }
                                            }
                                            clipboardManager.setText(AnnotatedString(summary))
                                            showCopiedSnackbar = true
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Export Portfolio", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (achievements.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Outlined.EmojiEvents,
                                title = "No trophies logged yet",
                                description = "Record your wins, runner-up spots, and special recognition awards to build your student portfolio.",
                                actionButtonText = "Record Achievement",
                                onActionClick = onOpenAddDialog
                            )
                        }
                    } else {
                        items(achievements, key = { it.id }) { ach ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFEF3C7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = ach.competitionName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = ach.organizer,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = ach.rank.ifBlank { ach.result },
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (ach.projectName.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Project: ${ach.projectName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    if (ach.description.isNotBlank()) {
                                        Text(
                                            text = ach.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Awarded: ${ach.date}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        if (ach.certificateUrl.isNotBlank() || ach.proofUrl.isNotBlank()) {
                                            TextButton(onClick = { /* Open cert */ }) {
                                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View Credential")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
