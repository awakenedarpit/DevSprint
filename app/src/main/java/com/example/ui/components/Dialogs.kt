package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Hackathon
import com.example.model.TaskItem
import com.example.ui.theme.HackTrackPrimary
import com.example.ui.theme.HackTrackPrimaryContainer
import com.example.ui.theme.HackTrackSuccess

@Composable
fun AddHackathonDialog(
    onDismiss: () -> Unit,
    onConfirm: (Hackathon) -> Unit,
    onExtractFromUrl: ((String, (Hackathon) -> Unit, (String) -> Unit) -> Unit)? = null
) {
    var websiteUrl by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("AI / Web3 / Fintech") }
    var mode by remember { mutableStateOf("Online") }
    var teamName by remember { mutableStateOf("Solo / Team") }
    var submissionDeadline by remember { mutableStateOf("") }
    var registrationDeadline by remember { mutableStateOf("") }
    var prizeInfo by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var problemStatement by remember { mutableStateOf("") }

    var isExtracting by remember { mutableStateOf(false) }
    var extractionMessage by remember { mutableStateOf<String?>(null) }
    var isExtractionError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HackTrackPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Hackathon", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Smart URL Extraction Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Extract from Website Link",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = "Paste any hackathon URL (Devpost, Unstop, MLH, etc.) and AI will extract details instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        OutlinedTextField(
                            value = websiteUrl,
                            onValueChange = { websiteUrl = it },
                            placeholder = { Text("https://devpost.com/software/...") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Button(
                            onClick = {
                                if (websiteUrl.isNotBlank() && onExtractFromUrl != null) {
                                    isExtracting = true
                                    extractionMessage = null
                                    isExtractionError = false
                                    onExtractFromUrl(
                                        websiteUrl.trim(),
                                        { extracted ->
                                            isExtracting = false
                                            name = extracted.name
                                            organizer = extracted.organizer
                                            domain = extracted.domain
                                            mode = extracted.mode
                                            if (extracted.submissionDeadline.isNotBlank()) {
                                                submissionDeadline = extracted.submissionDeadline
                                            }
                                            if (extracted.registrationDeadline.isNotBlank()) {
                                                registrationDeadline = extracted.registrationDeadline
                                            }
                                            if (extracted.prizeInfo.isNotBlank()) {
                                                prizeInfo = extracted.prizeInfo
                                            }
                                            if (extracted.description.isNotBlank()) {
                                                description = extracted.description
                                            }
                                            if (extracted.problemStatement.isNotBlank()) {
                                                problemStatement = extracted.problemStatement
                                            }
                                            extractionMessage = "✨ Details extracted successfully! Review below."
                                            isExtractionError = false
                                        },
                                        { error ->
                                            isExtracting = false
                                            extractionMessage = "Extraction note: $error"
                                            isExtractionError = true
                                        }
                                    )
                                }
                            },
                            enabled = websiteUrl.isNotBlank() && !isExtracting,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isExtracting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Website...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-Extract Details", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (extractionMessage != null) {
                            Text(
                                text = extractionMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isExtractionError) MaterialTheme.colorScheme.error else HackTrackSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Competition Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

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
                    label = { Text("Submission Deadline (e.g., Nov 25, 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = registrationDeadline,
                    onValueChange = { registrationDeadline = it },
                    label = { Text("Registration Deadline") },
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
                    label = { Text("Problem Statement / Theme") },
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
                                websiteUrl = websiteUrl.trim(),
                                domain = domain,
                                mode = mode,
                                teamName = teamName,
                                submissionDeadline = submissionDeadline,
                                registrationDeadline = registrationDeadline,
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
                Text("Save Hackathon")
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

@Composable
fun EditHackathonDialog(
    hackathon: Hackathon,
    onDismiss: () -> Unit,
    onConfirm: (Hackathon) -> Unit
) {
    var name by remember { mutableStateOf(hackathon.name) }
    var organizer by remember { mutableStateOf(hackathon.organizer) }
    var websiteUrl by remember { mutableStateOf(hackathon.websiteUrl) }
    var domain by remember { mutableStateOf(hackathon.domain) }
    var mode by remember { mutableStateOf(hackathon.mode) }
    var teamName by remember { mutableStateOf(hackathon.teamName) }
    var teamSize by remember { mutableStateOf(hackathon.teamSize.toString()) }
    var status by remember { mutableStateOf(hackathon.status) }
    var submissionDeadline by remember { mutableStateOf(hackathon.submissionDeadline) }
    var registrationDeadline by remember { mutableStateOf(hackathon.registrationDeadline) }
    var finalEventDate by remember { mutableStateOf(hackathon.finalEventDate) }
    var prizeInfo by remember { mutableStateOf(hackathon.prizeInfo) }
    var problemStatement by remember { mutableStateOf(hackathon.problemStatement) }
    var description by remember { mutableStateOf(hackathon.description) }

    val statusOptions = listOf("Registered", "Building", "Submitted", "Shortlisted", "Finalist", "Winner", "Runner-up", "Completed")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HackTrackPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Hackathon", fontWeight = FontWeight.Bold)
            }
        },
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
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Event Website URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Current Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.take(4).forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.drop(4).forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = submissionDeadline,
                    onValueChange = { submissionDeadline = it },
                    label = { Text("Submission Deadline (e.g., Nov 25, 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = registrationDeadline,
                    onValueChange = { registrationDeadline = it },
                    label = { Text("Registration Deadline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = finalEventDate,
                    onValueChange = { finalEventDate = it },
                    label = { Text("Final Event / Demo Day Date") },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = teamSize,
                        onValueChange = { teamSize = it },
                        label = { Text("Team Size") },
                        singleLine = true,
                        modifier = Modifier.width(90.dp)
                    )
                }
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
                            hackathon.copy(
                                name = name.trim(),
                                organizer = organizer.ifBlank { "Independent" },
                                websiteUrl = websiteUrl.trim(),
                                domain = domain,
                                mode = mode,
                                teamName = teamName,
                                teamSize = teamSize.toIntOrNull() ?: hackathon.teamSize,
                                status = status,
                                submissionDeadline = submissionDeadline.trim(),
                                registrationDeadline = registrationDeadline.trim(),
                                finalEventDate = finalEventDate.trim(),
                                prizeInfo = prizeInfo.trim(),
                                problemStatement = problemStatement.trim(),
                                description = description.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save Changes")
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
fun EditTaskDialog(
    task: TaskItem,
    hackathons: List<Hackathon>,
    onDismiss: () -> Unit,
    onConfirm: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var priority by remember { mutableStateOf(task.priority) }
    var status by remember { mutableStateOf(task.status) }
    var category by remember { mutableStateOf(task.category) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var assignedMember by remember { mutableStateOf(task.assignedMember) }
    var notes by remember { mutableStateOf(task.notes) }

    val priorities = listOf("Critical", "High", "Medium", "Low")
    val statuses = listOf("To Do", "In Progress", "Under Review", "Completed", "Blocked")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
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

                Text("Status", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.take(3).forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.drop(3).forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

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

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (e.g., Nov 15, 2026)") },
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
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Code Pointers") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            task.copy(
                                title = title.trim(),
                                description = description.trim(),
                                priority = priority,
                                status = status,
                                category = category,
                                dueDate = dueDate.trim(),
                                assignedMember = assignedMember.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Changes")
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
fun AddEditRoundDialog(
    initialRound: com.example.model.HackathonRound? = null,
    hackathonId: String,
    onDismiss: () -> Unit,
    onConfirm: (com.example.model.HackathonRound) -> Unit
) {
    var title by remember { mutableStateOf(initialRound?.title ?: "") }
    var roundDate by remember { mutableStateOf(initialRound?.roundDate ?: "Nov 20, 2026") }
    var roundOrder by remember { mutableStateOf((initialRound?.roundOrder ?: 1).toString()) }
    var notes by remember { mutableStateOf(initialRound?.notes ?: "") }
    var isCompleted by remember { mutableStateOf(initialRound?.isCompleted ?: false) }

    val isEdit = initialRound != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Timeline Round" else "Add Timeline Round", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Round Name / Milestone *") },
                    placeholder = { Text("e.g., Idea Pitch / Prototype Submission") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = roundDate,
                    onValueChange = { roundDate = it },
                    label = { Text("Round Date (e.g., Nov 20, 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = roundOrder,
                    onValueChange = { roundOrder = it },
                    label = { Text("Round Number / Order") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Deliverables / Criteria") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark as completed", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            com.example.model.HackathonRound(
                                id = initialRound?.id ?: "",
                                hackathonId = hackathonId,
                                title = title.trim(),
                                roundOrder = roundOrder.toIntOrNull() ?: 1,
                                roundDate = roundDate.trim(),
                                isCompleted = isCompleted,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (isEdit) "Save Round" else "Add Round")
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
fun AddEditDeadlineDialog(
    initialDeadline: com.example.model.CustomDeadline? = null,
    hackathons: List<Hackathon>,
    defaultHackathonId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (com.example.model.CustomDeadline) -> Unit
) {
    var title by remember { mutableStateOf(initialDeadline?.title ?: "") }
    var competitionName by remember {
        mutableStateOf(
            initialDeadline?.competitionName
                ?: hackathons.firstOrNull { it.id == defaultHackathonId }?.name
                ?: hackathons.firstOrNull()?.name
                ?: "Hackathon Sprint"
        )
    }
    var date by remember { mutableStateOf(initialDeadline?.date ?: "Nov 25, 2026") }
    var type by remember { mutableStateOf(initialDeadline?.type ?: "Submission Deadline") }
    var isCritical by remember { mutableStateOf(initialDeadline?.isCritical ?: false) }
    var notes by remember { mutableStateOf(initialDeadline?.notes ?: "") }

    val deadlineTypes = listOf("Submission Deadline", "Round Presentation", "Registration", "Pitch Deck Freeze", "Team Meeting", "Demo Day")
    val isEdit = initialDeadline != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Deadline" else "Add Event / Deadline", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Milestone / Deadline Title *") },
                    placeholder = { Text("e.g., Code Freeze & Pitch Deck") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = competitionName,
                    onValueChange = { competitionName = it },
                    label = { Text("Competition / Context Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Deadline Date (e.g., Nov 25, 2026) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Deadline Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    deadlineTypes.take(3).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    deadlineTypes.drop(3).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = isCritical,
                        onCheckedChange = { isCritical = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flag as Critical Priority ⚠️", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Submission Links") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank()) {
                        val matchedHack = hackathons.firstOrNull { it.name.equals(competitionName.trim(), ignoreCase = true) }
                        onConfirm(
                            com.example.model.CustomDeadline(
                                id = initialDeadline?.id ?: "",
                                hackathonId = matchedHack?.id ?: defaultHackathonId ?: "",
                                title = title.trim(),
                                competitionName = competitionName.trim(),
                                date = date.trim(),
                                type = type,
                                isCritical = isCritical,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && date.isNotBlank()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Deadline")
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
fun AddEditProjectDialog(
    initialProject: com.example.model.Project? = null,
    hackathonId: String,
    onDismiss: () -> Unit,
    onConfirm: (com.example.model.Project) -> Unit
) {
    var title by remember { mutableStateOf(initialProject?.title ?: "") }
    var description by remember { mutableStateOf(initialProject?.description ?: "") }
    var problemStatement by remember { mutableStateOf(initialProject?.problemStatement ?: "") }
    var proposedSolution by remember { mutableStateOf(initialProject?.proposedSolution ?: "") }
    var techStack by remember { mutableStateOf(initialProject?.techStack ?: "Kotlin, Compose, Firebase, Gemini") }
    var githubRepo by remember { mutableStateOf(initialProject?.githubRepo ?: "") }
    var liveDeployment by remember { mutableStateOf(initialProject?.liveDeployment ?: "") }
    var demoVideo by remember { mutableStateOf(initialProject?.demoVideo ?: "") }
    var status by remember { mutableStateOf(initialProject?.status ?: "Development") }

    val isEdit = initialProject != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Project Details" else "New Project", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Project Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = techStack,
                    onValueChange = { techStack = it },
                    label = { Text("Tech Stack") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = problemStatement,
                    onValueChange = { problemStatement = it },
                    label = { Text("Problem Statement") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = proposedSolution,
                    onValueChange = { proposedSolution = it },
                    label = { Text("Proposed Solution / Architecture") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = githubRepo,
                    onValueChange = { githubRepo = it },
                    label = { Text("GitHub Repository URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = liveDeployment,
                    onValueChange = { liveDeployment = it },
                    label = { Text("Live Prototype / Web URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = demoVideo,
                    onValueChange = { demoVideo = it },
                    label = { Text("Demo Video (YouTube / Loom)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            (initialProject ?: com.example.model.Project(hackathonId = hackathonId)).copy(
                                title = title.trim(),
                                description = description.trim(),
                                problemStatement = problemStatement.trim(),
                                proposedSolution = proposedSolution.trim(),
                                techStack = techStack.trim(),
                                githubRepo = githubRepo.trim(),
                                liveDeployment = liveDeployment.trim(),
                                demoVideo = demoVideo.trim(),
                                status = status
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (isEdit) "Save Project" else "Create Project")
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
fun AddEditSubmissionDialog(
    initialSubmission: com.example.model.SubmissionItem? = null,
    hackathonId: String,
    onDismiss: () -> Unit,
    onConfirm: (com.example.model.SubmissionItem) -> Unit
) {
    var title by remember { mutableStateOf(initialSubmission?.title ?: "") }
    var deliverableType by remember { mutableStateOf(initialSubmission?.deliverableType ?: "Pitch Deck") }
    var externalUrl by remember { mutableStateOf(initialSubmission?.externalUrl ?: "") }
    var fileUrl by remember { mutableStateOf(initialSubmission?.fileUrl ?: "") }
    var notes by remember { mutableStateOf(initialSubmission?.notes ?: "") }
    var status by remember { mutableStateOf(initialSubmission?.status ?: "In Progress") }

    val deliverableTypes = listOf("Pitch Deck", "Demo Video", "GitHub Repo", "Prototype URL", "Documentation")
    val statuses = listOf("Pending", "In Progress", "Completed", "Submitted")
    val isEdit = initialSubmission != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Deliverable" else "Add Deliverable Item", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Deliverable Title *") },
                    placeholder = { Text("e.g., Final 10-Slide Deck PDF") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Deliverable Type", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    deliverableTypes.take(3).forEach { t ->
                        FilterChip(
                            selected = deliverableType == t,
                            onClick = { deliverableType = t },
                            label = { Text(t, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Text("Status", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = externalUrl,
                    onValueChange = { externalUrl = it },
                    label = { Text("Link / URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Submission Guidelines & Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            (initialSubmission ?: com.example.model.SubmissionItem(hackathonId = hackathonId)).copy(
                                title = title.trim(),
                                deliverableType = deliverableType,
                                externalUrl = externalUrl.trim(),
                                fileUrl = fileUrl.trim(),
                                notes = notes.trim(),
                                status = status
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (isEdit) "Save Deliverable" else "Add Deliverable")
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
fun DeleteConfirmationDialog(
    title: String = "Delete Confirmation",
    message: String = "Are you sure you want to permanently delete this item? This action cannot be undone.",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete Permanently")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

