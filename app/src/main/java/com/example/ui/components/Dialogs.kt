package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.Hackathon
import com.example.model.TaskItem

@Composable
fun AddHackathonDialog(
    onDismiss: () -> Unit,
    onConfirm: (Hackathon) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("AI / Web3 / Fintech") }
    var mode by remember { mutableStateOf("Hybrid") }
    var teamName by remember { mutableStateOf("CodeCrafters") }
    var submissionDeadline by remember { mutableStateOf("Nov 25, 2026") }
    var prizeInfo by remember { mutableStateOf("$10,000 USD") }
    var description by remember { mutableStateOf("") }
    var problemStatement by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Hackathon") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Competition Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("Organizer / Host *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Category / Domain") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mode,
                    onValueChange = { mode = it },
                    label = { Text("Mode (Online / Offline / Hybrid)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = submissionDeadline,
                    onValueChange = { submissionDeadline = it },
                    label = { Text("Submission Deadline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prizeInfo,
                    onValueChange = { prizeInfo = it },
                    label = { Text("Prize Pool Info") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = problemStatement,
                    onValueChange = { problemStatement = it },
                    label = { Text("Problem Statement") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            Hackathon(
                                name = name.trim(),
                                organizer = organizer.ifBlank { "Independent" },
                                domain = domain,
                                mode = mode,
                                teamName = teamName,
                                submissionDeadline = submissionDeadline,
                                prizeInfo = prizeInfo,
                                problemStatement = problemStatement,
                                description = description,
                                status = "Registered"
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddTaskDialog(
    hackathons: List<Hackathon>,
    defaultHackathonId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var category by remember { mutableStateOf("Development") }
    var dueDate by remember { mutableStateOf("Nov 15, 2026") }
    var assignedMember by remember { mutableStateOf("Me") }
    var selectedHackathonId by remember { mutableStateOf(defaultHackathonId ?: hackathons.firstOrNull()?.id ?: "") }

    val priorities = listOf("Critical", "High", "Medium", "Low")
    val categories = listOf("Ideation", "Development", "Frontend", "Backend", "AI/ML", "Pitch", "Documentation")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Sprint Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Acceptance Criteria") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedMember,
                    onValueChange = { assignedMember = it },
                    label = { Text("Assigned Team Member") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Priority Level", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            TaskItem(
                                hackathonId = selectedHackathonId,
                                title = title.trim(),
                                description = description,
                                priority = priority,
                                category = category,
                                dueDate = dueDate,
                                assignedMember = assignedMember,
                                status = "To Do"
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
