package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TaskItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TaskCard
import com.example.ui.theme.HackTrackPrimary
import com.example.ui.theme.HackTrackSuccess
import com.example.viewmodel.HackTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: HackTrackViewModel,
    onOpenAddTaskDialog: () -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()
    val hackathons by viewModel.hackathons.collectAsState()

    val searchQuery by viewModel.taskSearchQuery.collectAsState()
    val priorityFilter by viewModel.taskPriorityFilter.collectAsState()
    val statusFilter by viewModel.taskStatusFilter.collectAsState()

    var isKanbanView by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDeleteId by remember { mutableStateOf<String?>(null) }

    val priorities = listOf("All", "Critical", "High", "Medium", "Low")
    val statuses = listOf("All", "Backlog", "To Do", "In Progress", "Under Review", "Completed", "Blocked")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTaskDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & View Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setTaskSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search sprint tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setTaskSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                FilledTonalIconButton(
                    onClick = { isKanbanView = !isKanbanView },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isKanbanView) Icons.Default.ViewList else Icons.Default.ViewKanban,
                        contentDescription = "Toggle Kanban View"
                    )
                }
            }

            // Priority Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(priorities) { p ->
                    val selected = if (p == "All") priorityFilter == null else priorityFilter == p
                    FilterChip(
                        selected = selected,
                        onClick = {
                            viewModel.setTaskPriorityFilter(if (p == "All") null else p)
                        },
                        label = { Text(p, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (tasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Checklist,
                    title = "No tasks found",
                    description = if (searchQuery.isNotBlank()) "No tasks match your filters." else "Add your first hackathon task to track your progress.",
                    actionButtonText = "Create Task",
                    onActionClick = onOpenAddTaskDialog,
                    modifier = Modifier.weight(1f)
                )
            } else if (!isKanbanView) {
                // List View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                            onClick = { taskToEdit = task },
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDeleteId = task.id }
                        )
                    }
                }
            } else {
                // Kanban Board View
                val kanbanStatuses = listOf("To Do", "In Progress", "Under Review", "Completed")
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    kanbanStatuses.forEach { statusCol ->
                        val colTasks = tasks.filter { it.status == statusCol }
                        KanbanColumn(
                            title = statusCol,
                            tasks = colTasks,
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { taskToEdit = it },
                            onDeleteTask = { taskToDeleteId = it },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }
    }

    if (taskToEdit != null) {
        com.example.ui.components.EditTaskDialog(
            task = taskToEdit!!,
            hackathons = hackathons,
            onDismiss = { taskToEdit = null },
            onConfirm = { updated ->
                viewModel.updateTask(updated)
                taskToEdit = null
            }
        )
    }

    if (taskToDeleteId != null) {
        com.example.ui.components.DeleteConfirmationDialog(
            title = "Delete Task?",
            message = "Are you sure you want to delete this task?",
            onDismiss = { taskToDeleteId = null },
            onConfirm = {
                viewModel.deleteTask(taskToDeleteId!!)
                taskToDeleteId = null
            }
        )
    }
}

@Composable
fun KanbanColumn(
    title: String,
    tasks: List<TaskItem>,
    onToggleComplete: (String) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${tasks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight()
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
}
