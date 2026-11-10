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

    // Dialog states
    var showEditHackathonDialog by remember { mutableStateOf(false) }
    var showDeleteHackathonDialog by remember { mutableStateOf(false) }

    // Task editing
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDeleteId by remember { mutableStateOf<String?>(null) }

    // Round editing
    var showAddRoundDialog by remember { mutableStateOf(false) }
    var roundToEdit by remember { mutableStateOf<HackathonRound?>(null) }
    var roundToDeleteId by remember { mutableStateOf<String?>(null) }

    // Project editing
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) }
    var projectToDeleteId by remember { mutableStateOf<String?>(null) }

    // Submission editing
    var showAddSubmissionDialog by remember { mutableStateOf(false) }
    var submissionToEdit by remember { mutableStateOf<SubmissionItem?>(null) }
    var submissionToDeleteId by remember { mutableStateOf<String?>(null) }

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
                    IconButton(onClick = { showEditHackathonDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Hackathon")
                    }
                    IconButton(onClick = { showDeleteHackathonDialog = true }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Hackathon",
                            tint = MaterialTheme.colorScheme.error
                        )
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
                    onEditTask = { taskToEdit = it },
                    onDeleteTask = { taskToDeleteId = it },
                    onAddTask = { onOpenAddTaskForHackathon(hackathon.id) }
                )
                2 -> TimelineTabContent(
                    hackathon = hackathon,
                    onAddRound = { showAddRoundDialog = true },
                    onEditRound = { roundToEdit = it },
                    onDeleteRound = { roundToDeleteId = it },
                    onToggleRound = { viewModel.toggleRoundCompletion(hackathon.id, it) },
                    onGenerateStandardRounds = {
                        val standard = listOf(
                            HackathonRound(id = "rnd_1", hackathonId = hackathon.id, title = "Registration & Team Formation", roundOrder = 1, roundDate = hackathon.registrationDeadline.ifBlank { "Sprint Day 0" }, isCompleted = true, notes = "Register team members"),
                            HackathonRound(id = "rnd_2", hackathonId = hackathon.id, title = "Problem Statement & Abstract Submission", roundOrder = 2, roundDate = "Sprint Day 1", isCompleted = false, notes = "Submit project abstract and architecture"),
                            HackathonRound(id = "rnd_3", hackathonId = hackathon.id, title = "Prototype & Code Drop", roundOrder = 3, roundDate = hackathon.submissionDeadline.ifBlank { "Sprint Day 2" }, isCompleted = false, notes = "Submit GitHub repository and demo video"),
                            HackathonRound(id = "rnd_4", hackathonId = hackathon.id, title = "Grand Finale Jury Presentation", roundOrder = 4, roundDate = hackathon.finalEventDate.ifBlank { "Demo Day" }, isCompleted = false, notes = "10 min live pitch with Q&A")
                        )
                        viewModel.updateHackathon(hackathon.copy(rounds = standard))
                    }
                )
                3 -> ProjectTabContent(
                    projects = hackathonProjects,
                    hackathon = hackathon,
                    onAddProject = { showAddProjectDialog = true },
                    onEditProject = { projectToEdit = it },
                    onDeleteProject = { projectToDeleteId = it }
                )
                4 -> SubmissionsTabContent(
                    submissions = hackathonSubmissions,
                    hackathon = hackathon,
                    onAddSubmission = { showAddSubmissionDialog = true },
                    onEditSubmission = { submissionToEdit = it },
                    onDeleteSubmission = { submissionToDeleteId = it },
                    onUpdateStatus = { sub, newStatus ->
                        viewModel.updateSubmission(sub.copy(status = newStatus))
                    }
                )
                5 -> ResultsTabContent(hackathonAchievements, hackathon)
            }
        }
    }

    // Edit Hackathon Dialog
    if (showEditHackathonDialog) {
        EditHackathonDialog(
            hackathon = hackathon,
            onDismiss = { showEditHackathonDialog = false },
            onConfirm = { updated ->
                viewModel.updateHackathon(updated)
                showEditHackathonDialog = false
            }
        )
    }

    // Delete Hackathon Confirmation
    if (showDeleteHackathonDialog) {
        DeleteConfirmationDialog(
            title = "Delete Hackathon?",
            message = "Are you sure you want to permanently delete '${hackathon.name}'? This will also remove associated tasks and submissions.",
            onDismiss = { showDeleteHackathonDialog = false },
            onConfirm = {
                viewModel.deleteHackathon(hackathon.id)
                showDeleteHackathonDialog = false
                onBack()
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

    // Add Round Dialog
    if (showAddRoundDialog) {
        AddEditRoundDialog(
            hackathonId = hackathon.id,
            onDismiss = { showAddRoundDialog = false },
            onConfirm = { round ->
                viewModel.addRound(hackathon.id, round)
                showAddRoundDialog = false
            }
        )
    }

    // Edit Round Dialog
    if (roundToEdit != null) {
        AddEditRoundDialog(
            initialRound = roundToEdit,
            hackathonId = hackathon.id,
            onDismiss = { roundToEdit = null },
            onConfirm = { updated ->
                viewModel.updateRound(hackathon.id, updated)
                roundToEdit = null
            }
        )
    }

    // Delete Round Confirmation
    if (roundToDeleteId != null) {
        DeleteConfirmationDialog(
            title = "Delete Milestone?",
            message = "Are you sure you want to delete this timeline round?",
            onDismiss = { roundToDeleteId = null },
            onConfirm = {
                viewModel.deleteRound(hackathon.id, roundToDeleteId!!)
                roundToDeleteId = null
            }
        )
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        AddEditProjectDialog(
            hackathonId = hackathon.id,
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { project ->
                viewModel.addProject(project)
                showAddProjectDialog = false
            }
        )
    }

    // Edit Project Dialog
    if (projectToEdit != null) {
        AddEditProjectDialog(
            initialProject = projectToEdit,
            hackathonId = hackathon.id,
            onDismiss = { projectToEdit = null },
            onConfirm = { updated ->
                viewModel.updateProject(updated)
                projectToEdit = null
            }
        )
    }

    // Delete Project Confirmation
    if (projectToDeleteId != null) {
        DeleteConfirmationDialog(
            title = "Delete Project?",
            message = "Are you sure you want to delete this project entry?",
            onDismiss = { projectToDeleteId = null },
            onConfirm = {
                viewModel.deleteProject(projectToDeleteId!!)
                projectToDeleteId = null
            }
        )
    }

    // Add Submission Dialog
    if (showAddSubmissionDialog) {
        AddEditSubmissionDialog(
            hackathonId = hackathon.id,
            onDismiss = { showAddSubmissionDialog = false },
            onConfirm = { sub ->
                viewModel.addSubmission(sub)
                showAddSubmissionDialog = false
            }
        )
    }

    // Edit Submission Dialog
    if (submissionToEdit != null) {
        AddEditSubmissionDialog(
            initialSubmission = submissionToEdit,
            hackathonId = hackathon.id,
            onDismiss = { submissionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateSubmission(updated)
                submissionToEdit = null
            }
        )
    }

    // Delete Submission Confirmation
    if (submissionToDeleteId != null) {
        DeleteConfirmationDialog(
            title = "Delete Deliverable?",
            message = "Are you sure you want to delete this deliverable checklist item?",
            onDismiss = { submissionToDeleteId = null },
            onConfirm = {
                viewModel.deleteSubmission(submissionToDeleteId!!)
                submissionToDeleteId = null
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
        // Quick Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Mode: ${hackathon.mode}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Team: ${hackathon.teamName} (${hackathon.teamSize} members)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Problem Statement Card
        if (hackathon.problemStatement.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Problem Statement / Challenge",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = hackathon.problemStatement,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Live Animated Hackathon Countdown Timer
        if (hackathon.submissionDeadline.isNotBlank()) {
            item {
                LiveHackathonCountdownTimer(
                    competitionName = hackathon.name,
                    deadlineTitle = "Final Project & Code Submission",
                    deadlineDateStr = hackathon.submissionDeadline
                )
            }
        }

        // Key Deadlines Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Key Event Dates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    DetailRow(
                        icon = Icons.Default.Event,
                        label = "Registration Deadline",
                        value = hackathon.registrationDeadline.ifBlank { "Not specified" }
                    )
                    DetailRow(
                        icon = Icons.Default.Flag,
                        label = "Submission Deadline",
                        value = hackathon.submissionDeadline.ifBlank { "Not specified" }
                    )
                    DetailRow(
                        icon = Icons.Default.EmojiEvents,
                        label = "Final Event / Demo Day",
                        value = hackathon.finalEventDate.ifBlank { "Not specified" }
                    )
                    if (hackathon.prizeInfo.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Default.Stars,
                            label = "Prize Pool",
                            value = hackathon.prizeInfo
                        )
                    }
                }
            }
        }

        // Links Card
        if (hackathon.websiteUrl.isNotBlank() || hackathon.registrationUrl.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Official Links",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (hackathon.websiteUrl.isNotBlank()) {
                            DetailRow(icon = Icons.Default.Language, label = "Website", value = hackathon.websiteUrl)
                        }
                        if (hackathon.registrationUrl.isNotBlank()) {
                            DetailRow(icon = Icons.Default.Link, label = "Registration", value = hackathon.registrationUrl)
                        }
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
    onEditTask: (TaskItem) -> Unit,
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
                        onClick = { onEditTask(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineTabContent(
    hackathon: Hackathon,
    onAddRound: () -> Unit,
    onEditRound: (HackathonRound) -> Unit,
    onDeleteRound: (String) -> Unit,
    onToggleRound: (String) -> Unit,
    onGenerateStandardRounds: () -> Unit
) {
    val displayRounds = hackathon.rounds

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Milestones & Rounds (${displayRounds.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddRound,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Round")
            }
        }

        if (displayRounds.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Timeline,
                title = "No custom milestones yet",
                description = "Add competition rounds like Idea Submission, Code Drop, and Demo Day.",
                actionButtonText = "Generate Standard Rounds",
                onActionClick = onGenerateStandardRounds
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayRounds, key = { it.id.ifBlank { it.title } }) { round ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = round.isCompleted,
                                onCheckedChange = { onToggleRound(round.id) },
                                colors = CheckboxDefaults.colors(checkedColor = HackTrackSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = round.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (round.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                if (round.roundDate.isNotBlank()) {
                                    Text(
                                        text = "Target Date: ${round.roundDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (round.notes.isNotBlank()) {
                                    Text(
                                        text = round.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onEditRound(round) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Round", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { onDeleteRound(round.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Round", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectTabContent(
    projects: List<Project>,
    hackathon: Hackathon,
    onAddProject: () -> Unit,
    onEditProject: (Project) -> Unit,
    onDeleteProject: (String) -> Unit
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
                text = "Projects (${projects.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddProject,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Project")
            }
        }

        if (projects.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Code,
                title = "No project linked yet",
                description = "Link your code repository, demo video, and proposed solution to this hackathon workspace.",
                actionButtonText = "Create Project",
                onActionClick = onAddProject
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = project.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    StatusBadge(status = project.status)
                                }
                                Row {
                                    IconButton(onClick = { onEditProject(project) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Project", tint = MaterialTheme.colorScheme.outline)
                                    }
                                    IconButton(onClick = { onDeleteProject(project.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Project", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (project.description.isNotBlank()) {
                                Text(
                                    text = project.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            DetailRow(icon = Icons.Default.Layers, label = "Tech Stack", value = project.techStack)
                            if (project.githubRepo.isNotBlank()) {
                                DetailRow(icon = Icons.Default.Code, label = "GitHub", value = project.githubRepo)
                            }
                            if (project.liveDeployment.isNotBlank()) {
                                DetailRow(icon = Icons.Default.Launch, label = "Live URL", value = project.liveDeployment)
                            }
                            if (project.demoVideo.isNotBlank()) {
                                DetailRow(icon = Icons.Default.PlayCircle, label = "Demo Video", value = project.demoVideo)
                            }
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
    hackathon: Hackathon,
    onAddSubmission: () -> Unit,
    onEditSubmission: (SubmissionItem) -> Unit,
    onDeleteSubmission: (String) -> Unit,
    onUpdateStatus: (SubmissionItem, String) -> Unit
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
                text = "Deliverables (${submissions.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddSubmission,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Deliverable")
            }
        }

        if (submissions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.CloudUpload,
                title = "No submission deliverables",
                description = "Track your pitch deck, repo, demo video, and abstract submission deliverables.",
                actionButtonText = "Add Deliverable",
                onActionClick = onAddSubmission
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = sub.status)
                                    IconButton(
                                        onClick = { onEditSubmission(sub) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteSubmission(sub.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
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
}

@Composable
fun ResultsTabContent(achievements: List<Achievement>, hackathon: Hackathon) {
    if (achievements.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.EmojiEvents,
            title = "Results Pending",
            description = "Once the judges announce results, your trophies, certificates, and portfolio accomplishments appear here!"
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
