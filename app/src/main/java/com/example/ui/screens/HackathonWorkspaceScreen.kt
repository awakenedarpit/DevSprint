package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.HackTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonWorkspaceScreen(
    hackathonId: String,
    viewModel: HackTrackViewModel,
    onBack: () -> Unit,
    onOpenAddTaskForHackathon: (String) -> Unit
) {
    BackHandler { onBack() }

    val hackathons by viewModel.hackathons.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val hackathon = remember(hackathons, hackathonId) {
        hackathons.firstOrNull { it.id == hackathonId }
    }

    if (hackathon == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Competition not found")
        }
        return
    }

    val hackathonTasks = remember(tasks, hackathonId) {
        tasks.filter { it.hackathonId == hackathonId }
    }

    val hackathonProjects = remember(projects, hackathonId) {
        projects.filter { it.hackathonId == hackathonId }
    }

    val hackathonSubmissions = remember(submissions, hackathonId) {
        submissions.filter { it.hackathonId == hackathonId }
    }

    val hackathonAchievements = remember(achievements, hackathonId) {
        achievements.filter { it.hackathonId == hackathonId }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Tasks", "Timeline", "Project", "Submissions", "Results")

    var showEditStatusDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = hackathon.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${hackathon.organizer} • ${hackathon.mode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    StatusBadge(status = hackathon.status)
                    IconButton(onClick = { showEditStatusDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Status")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> OverviewTabContent(hackathon)
                1 -> TasksTabContent(
                    tasks = hackathonTasks,
                    onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAddTask = { onOpenAddTaskForHackathon(hackathon.id) }
                )
                2 -> TimelineTabContent(hackathon)
                3 -> ProjectTabContent(hackathonProjects, hackathon)
                4 -> SubmissionsTabContent(hackathonSubmissions, onUpdateStatus = { sub, newStatus ->
                    viewModel.updateSubmission(sub.copy(status = newStatus))
                })
                5 -> ResultsTabContent(hackathonAchievements, hackathon)
            }
        }
    }

    if (showEditStatusDialog) {
        val statusList = listOf(
            "Interested", "Registered", "Building", "Submitted", "Shortlisted", "Finalist", "Winner", "Completed"
        )
        AlertDialog(
            onDismissRequest = { showEditStatusDialog = false },
            title = { Text("Update Competition Status") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    statusList.forEach { statusOption ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateHackathon(hackathon.copy(status = statusOption))
                                    showEditStatusDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (hackathon.status == statusOption) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(
                                text = statusOption,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                fontWeight = if (hackathon.status == statusOption) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditStatusDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun OverviewTabContent(hackathon: Hackathon) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Problem Statement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hackathon.problemStatement.isNotBlank()) hackathon.problemStatement else "No problem statement specified yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Competition Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DetailRow(icon = Icons.Default.Category, label = "Domain", value = hackathon.domain)
                    DetailRow(icon = Icons.Default.Groups, label = "Team", value = "${hackathon.teamName} (${hackathon.teamSize} members)")
                    DetailRow(icon = Icons.Default.CalendarToday, label = "Submission Deadline", value = hackathon.submissionDeadline)
                    DetailRow(icon = Icons.Default.EmojiEvents, label = "Prizes & Recognition", value = hackathon.prizeInfo)
                    if (hackathon.websiteUrl.isNotBlank()) {
                        DetailRow(icon = Icons.Default.Language, label = "Website", value = hackathon.websiteUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTabContent(
    tasks: List<TaskItem>,
    onToggleComplete: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Associated Tasks (${tasks.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddTask,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Task")
            }
        }

        if (tasks.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Assignment,
                title = "No tasks for this competition",
                description = "Break your hackathon into structured action items.",
                actionButtonText = "Add First Task",
                onActionClick = onAddTask
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task.id) },
                        onClick = {},
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineTabContent(hackathon: Hackathon) {
    val rounds = listOf(
        "Round 1: Registration & Team Formation" to hackathon.registrationDeadline,
        "Round 2: Problem Statement & Abstract Submission" to "Sprint Day 1",
        "Round 3: Working Prototype Demonstration" to hackathon.submissionDeadline,
        "Round 4: Shortlisting & Semi-finals Jury" to "Sprint Day 2",
        "Round 5: Grand Finale Presentation" to hackathon.finalEventDate
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rounds) { (title, date) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (date.isNotBlank()) "Target: $date" else "Date: Scheduled during event",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectTabContent(projects: List<Project>, hackathon: Hackathon) {
    if (projects.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.Code,
            title = "No project linked yet",
            description = "Link your code repository, demo video, and proposed solution to this hackathon workspace."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(projects, key = { it.id }) { project ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(status = project.status)
                        }

                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        DetailRow(icon = Icons.Default.Layers, label = "Tech Stack", value = project.techStack)
                        if (project.githubRepo.isNotBlank()) {
                            DetailRow(icon = Icons.Default.Code, label = "GitHub", value = project.githubRepo)
                        }
                        if (project.liveDeployment.isNotBlank()) {
                            DetailRow(icon = Icons.Default.Launch, label = "Live URL", value = project.liveDeployment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionsTabContent(
    submissions: List<SubmissionItem>,
    onUpdateStatus: (SubmissionItem, String) -> Unit
) {
    if (submissions.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.CloudUpload,
            title = "No submission deliverables",
            description = "Track your pitch deck, repo, demo video, and abstract submission deliverables."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(submissions, key = { it.id }) { sub ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(status = sub.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deliverable: ${sub.deliverableType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (sub.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sub.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (sub.status != "Completed" && sub.status != "Submitted") {
                                OutlinedButton(
                                    onClick = { onUpdateStatus(sub, "Completed") },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Mark Complete", fontSize = 11.sp)
                                }
                            }
                            if (sub.status != "Submitted") {
                                Button(
                                    onClick = { onUpdateStatus(sub, "Submitted") },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Official Submit", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultsTabContent(achievements: List<Achievement>, hackathon: Hackathon) {
    if (achievements.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.EmojiEvents,
            title = "Results Pending",
            description = "Once the judges announce results, record your trophies, certificates, and portfolio accomplishments here!"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements, key = { it.id }) { ach ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ach.result,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HackTrackWarning
                            )
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = ach.rank,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        )
    }
}
