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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomDeadline
import com.example.model.Hackathon
import com.example.model.TaskItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.viewmodel.HackTrackViewModel

@Composable
fun DashboardScreen(
    viewModel: HackTrackViewModel,
    onNavigateToHackathonDetail: (String) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onOpenAddHackathonDialog: () -> Unit,
    onOpenAddTaskDialog: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val hackathons by viewModel.hackathons.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val customDeadlines by viewModel.customDeadlines.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var hackathonToEdit by remember { mutableStateOf<Hackathon?>(null) }
    var hackathonToDeleteId by remember { mutableStateOf<String?>(null) }

    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDeleteId by remember { mutableStateOf<String?>(null) }

    var showAddDeadlineDialog by remember { mutableStateOf(false) }

    val activeHackathons = remember(hackathons) {
        hackathons.filter { it.status in listOf("Building", "Registered", "Finalist", "Shortlisted") && !it.isArchived }
    }

    val pendingTasks = remember(tasks) {
        tasks.filter { it.status != "Completed" }.take(5)
    }

    // Top upcoming deadlines chronologically
    val upcomingDeadlines = remember(hackathons, customDeadlines) {
        val list = mutableListOf<UnifiedDeadline>()
        customDeadlines.forEach { cd ->
            list.add(
                UnifiedDeadline(
                    id = cd.id,
                    title = cd.title,
                    competition = cd.competitionName,
                    type = cd.type,
                    date = cd.date,
                    isCritical = cd.isCritical,
                    notes = cd.notes,
                    isCustom = true,
                    customDeadline = cd,
                    hackathonId = cd.hackathonId
                )
            )
        }
        hackathons.filter { !it.isArchived }.forEach { h ->
            if (h.submissionDeadline.isNotBlank()) {
                list.add(
                    UnifiedDeadline(
                        id = "h_sub_${h.id}",
                        title = "Final Project Submission",
                        competition = h.name,
                        type = "Submission Deadline",
                        date = h.submissionDeadline,
                        isCritical = true,
                        hackathonId = h.id
                    )
                )
            }
            if (h.registrationDeadline.isNotBlank()) {
                list.add(
                    UnifiedDeadline(
                        id = "h_reg_${h.id}",
                        title = "Registration Closes",
                        competition = h.name,
                        type = "Registration",
                        date = h.registrationDeadline,
                        isCritical = false,
                        hackathonId = h.id
                    )
                )
            }
        }
        list.sortedBy { DateUtils.getDaysRemaining(it.date) ?: 999 }.take(4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    HackTrackPrimary,
                                    HackTrackSecondary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${DateUtils.getGreeting()}, ${profile.name.split(" ").firstOrNull() ?: "Hacker"}! 🚀",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = DateUtils.formatTodayDate(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = onNavigateToAiAssistant,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "HackTrack AI",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenAddHackathonDialog,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = HackTrackPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Hackathon", fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = onOpenAddTaskDialog,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.25f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Task")
                            }
                        }
                    }
                }
            }
        }

        // B. Key Competition Statistics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Competition Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active Sprints",
                        value = "${stats.activeHackathons}",
                        icon = Icons.Default.RocketLaunch,
                        iconColor = HackTrackPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending Tasks",
                        value = "${stats.pendingTasks}",
                        icon = Icons.Default.AssignmentLate,
                        iconColor = HackTrackWarning,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Submissions",
                        value = "${stats.completedSubmissions}",
                        icon = Icons.Default.CloudDone,
                        iconColor = HackTrackSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Trophies & Wins",
                        value = "${stats.totalWins}",
                        icon = Icons.Default.EmojiEvents,
                        iconColor = Color(0xFFEAB308),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // C. Upcoming Deadlines Countdown Spotlight
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = HackTrackPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Upcoming Deadlines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = onNavigateToCalendar) {
                    Text("All Deadlines", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (upcomingDeadlines.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No upcoming deadlines right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showAddDeadlineDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Add Event", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcomingDeadlines.forEach { dl ->
                        val badgeInfo = DateUtils.getBadgeInfo(dl.date)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (dl.hackathonId != null) {
                                        onNavigateToHackathonDetail(dl.hackathonId)
                                    } else {
                                        onNavigateToCalendar()
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dl.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${dl.competition} • ${dl.date}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    color = when (badgeInfo.severity) {
                                        com.example.util.DeadlineBadgeSeverity.CRITICAL -> HackTrackDanger.copy(alpha = 0.15f)
                                        com.example.util.DeadlineBadgeSeverity.WARNING -> HackTrackWarning.copy(alpha = 0.15f)
                                        com.example.util.DeadlineBadgeSeverity.OVERDUE -> Color.Gray.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = badgeInfo.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (badgeInfo.severity) {
                                            com.example.util.DeadlineBadgeSeverity.CRITICAL -> HackTrackDanger
                                            com.example.util.DeadlineBadgeSeverity.WARNING -> Color(0xFFB45309)
                                            com.example.util.DeadlineBadgeSeverity.OVERDUE -> Color.Gray
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // D. Active Competitions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Hackathons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { /* All hackathons */ }) {
                    Text("View All (${hackathons.size})", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (activeHackathons.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.RocketLaunch,
                    title = "No active hackathons",
                    description = "Join a new challenge to track milestones, tasks and submissions.",
                    actionButtonText = "Register Hackathon",
                    onActionClick = onOpenAddHackathonDialog
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(activeHackathons, key = { it.id }) { hackathon ->
                        Box(modifier = Modifier.width(290.dp)) {
                            HackathonCard(
                                hackathon = hackathon,
                                onClick = { onNavigateToHackathonDetail(hackathon.id) },
                                onEdit = { hackathonToEdit = hackathon },
                                onDelete = { hackathonToDeleteId = hackathon.id }
                            )
                        }
                    }
                }
            }
        }

        // E. Priority Sprint Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Focus Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToTasks) {
                    Text("Task Board", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (pendingTasks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HackTrackSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All sprint tasks are up to date! Great job.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingTasks.forEach { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                            onClick = { taskToEdit = task },
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDeleteId = task.id }
                        )
                    }
                }
            }
        }

        // F. AI Strategic Assistance Teaser Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAiAssistant),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HackTrackPrimaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(HackTrackPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Need Hackathon Strategy?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HackTrackOnPrimaryContainer
                        )
                        Text(
                            text = "Let HackTrack AI break down problem statements or outline your pitch deck.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HackTrackOnPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = HackTrackPrimary
                    )
                }
            }
        }
    }

    // Edit Hackathon Dialog
    if (hackathonToEdit != null) {
        EditHackathonDialog(
            hackathon = hackathonToEdit!!,
            onDismiss = { hackathonToEdit = null },
            onConfirm = { updated ->
                viewModel.updateHackathon(updated)
                hackathonToEdit = null
            }
        )
    }

    // Delete Hackathon Confirmation
    if (hackathonToDeleteId != null) {
        DeleteConfirmationDialog(
            title = "Delete Hackathon?",
            message = "Are you sure you want to delete this hackathon competition?",
            onDismiss = { hackathonToDeleteId = null },
            onConfirm = {
                viewModel.deleteHackathon(hackathonToDeleteId!!)
                hackathonToDeleteId = null
            }
        )
    }

    // Edit Task Dialog
    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            hackathons = hackathons,
            onDismiss = { taskToEdit = null },
            onConfirm = { updated ->
                viewModel.updateTask(updated)
                taskToEdit = null
            }
        )
    }

    // Delete Task Confirmation
    if (taskToDeleteId != null) {
        DeleteConfirmationDialog(
            title = "Delete Task?",
            message = "Are you sure you want to delete this task?",
            onDismiss = { taskToDeleteId = null },
            onConfirm = {
                viewModel.deleteTask(taskToDeleteId!!)
                taskToDeleteId = null
            }
        )
    }

    // Add Deadline Dialog
    if (showAddDeadlineDialog) {
        AddEditDeadlineDialog(
            hackathons = hackathons,
            onDismiss = { showAddDeadlineDialog = false },
            onConfirm = { deadline ->
                viewModel.addCustomDeadline(deadline)
                showAddDeadlineDialog = false
            }
        )
    }
}
