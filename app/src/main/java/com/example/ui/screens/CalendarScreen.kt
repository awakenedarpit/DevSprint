package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomDeadline
import com.example.model.Hackathon
import com.example.ui.components.AddEditDeadlineDialog
import com.example.ui.components.DeadlineCard
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.HackTrackPrimary
import com.example.ui.theme.HackTrackSuccess
import com.example.viewmodel.HackTrackViewModel

data class UnifiedDeadline(
    val id: String,
    val title: String,
    val competition: String,
    val type: String,
    val date: String,
    val isCritical: Boolean = false,
    val notes: String = "",
    val isCustom: Boolean = false,
    val customDeadline: CustomDeadline? = null,
    val hackathonId: String? = null
)

@Composable
fun CalendarScreen(
    viewModel: HackTrackViewModel,
    onNavigateToHackathonDetail: (String) -> Unit
) {
    val hackathons by viewModel.hackathons.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val customDeadlines by viewModel.customDeadlines.collectAsState()
    val isCalendarSyncEnabled by viewModel.isGoogleCalendarSyncEnabled.collectAsState()
    val context = LocalContext.current

    var showAddDeadlineDialog by remember { mutableStateOf(false) }
    var deadlineToEdit by remember { mutableStateOf<CustomDeadline?>(null) }
    var deadlineToDeleteId by remember { mutableStateOf<String?>(null) }

    val allDeadlines = remember(hackathons, tasks, customDeadlines) {
        val list = mutableListOf<UnifiedDeadline>()

        // 1. Custom Deadlines
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

        // 2. Hackathon-level deadlines
        hackathons.filter { !it.isArchived }.forEach { h ->
            if (h.submissionDeadline.isNotBlank()) {
                list.add(
                    UnifiedDeadline(
                        id = "h_sub_${h.id}",
                        title = "Final Project & Code Submission",
                        competition = h.name,
                        type = "Submission Deadline",
                        date = h.submissionDeadline,
                        isCritical = true,
                        notes = "Prize pool: ${h.prizeInfo}",
                        hackathonId = h.id
                    )
                )
            }
            if (h.registrationDeadline.isNotBlank()) {
                list.add(
                    UnifiedDeadline(
                        id = "h_reg_${h.id}",
                        title = "Team Registration Closes",
                        competition = h.name,
                        type = "Registration",
                        date = h.registrationDeadline,
                        isCritical = false,
                        hackathonId = h.id
                    )
                )
            }
            if (h.finalEventDate.isNotBlank()) {
                list.add(
                    UnifiedDeadline(
                        id = "h_fin_${h.id}",
                        title = "Grand Finale & Winner Presentations",
                        competition = h.name,
                        type = "Demo Day",
                        date = h.finalEventDate,
                        isCritical = false,
                        hackathonId = h.id
                    )
                )
            }
            // Add custom rounds from hackathon if any
            h.rounds.forEach { r ->
                if (r.roundDate.isNotBlank()) {
                    list.add(
                        UnifiedDeadline(
                            id = "rnd_${r.id}",
                            title = r.title,
                            competition = h.name,
                            type = "Timeline Round",
                            date = r.roundDate,
                            isCritical = false,
                            notes = r.notes,
                            hackathonId = h.id
                        )
                    )
                }
            }
        }

        // 3. Sprint Tasks with due dates
        tasks.filter { it.status != "Completed" && it.dueDate.isNotBlank() }.forEach { t ->
            val hackName = hackathons.firstOrNull { it.id == t.hackathonId }?.name ?: "Sprint Task"
            list.add(
                UnifiedDeadline(
                    id = "tsk_${t.id}",
                    title = t.title,
                    competition = hackName,
                    type = "Task Due Date",
                    date = t.dueDate,
                    isCritical = t.priority == "Critical",
                    notes = t.notes,
                    hackathonId = t.hackathonId
                )
            )
        }

        // Sort by parsed date
        list.sortedBy { com.example.util.DateUtils.getDaysRemaining(it.date) ?: 999 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Header Card with Calendar Sync and Add Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Deadlines & Milestones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${allDeadlines.size} upcoming competition milestones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddDeadlineDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Event")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Google Calendar Sync Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (isCalendarSyncEnabled) HackTrackPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google Calendar Sync",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isCalendarSyncEnabled) "Enabled — One-tap sync to system calendar" else "Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCalendarSyncEnabled) HackTrackSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isCalendarSyncEnabled,
                        onCheckedChange = { viewModel.setGoogleCalendarSyncEnabled(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (allDeadlines.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.EventBusy,
                title = "No upcoming deadlines",
                description = "Register for hackathons, add timeline rounds, or create custom deadline milestones.",
                actionButtonText = "Add First Deadline",
                onActionClick = { showAddDeadlineDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allDeadlines, key = { it.id }) { item ->
                    DeadlineCard(
                        title = item.title,
                        competitionName = item.competition,
                        dateStr = item.date,
                        type = item.type,
                        isCritical = item.isCritical,
                        notes = item.notes,
                        onEdit = {
                            if (item.isCustom && item.customDeadline != null) {
                                deadlineToEdit = item.customDeadline
                            } else if (item.hackathonId != null) {
                                onNavigateToHackathonDetail(item.hackathonId)
                            } else {
                                Toast.makeText(context, "Managed in task sprint", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            if (item.isCustom) {
                                deadlineToDeleteId = item.id
                            } else {
                                Toast.makeText(context, "Manage this date inside '${item.competition}' workspace", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onAddToCalendar = if (isCalendarSyncEnabled) {
                            {
                                try {
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, "${item.competition}: ${item.title}")
                                        putExtra(CalendarContract.Events.DESCRIPTION, "Category: ${item.type}\nNotes: ${item.notes}\nManaged in HackTrack Command Centre")
                                        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open calendar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else null
                    )
                }
            }
        }
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

    // Edit Deadline Dialog
    if (deadlineToEdit != null) {
        AddEditDeadlineDialog(
            initialDeadline = deadlineToEdit,
            hackathons = hackathons,
            onDismiss = { deadlineToEdit = null },
            onConfirm = { updated ->
                viewModel.updateCustomDeadline(updated)
                deadlineToEdit = null
            }
        )
    }

    // Delete Deadline Dialog
    if (deadlineToDeleteId != null) {
        DeleteConfirmationDialog(
            title = "Delete Deadline?",
            message = "Are you sure you want to delete this custom deadline?",
            onDismiss = { deadlineToDeleteId = null },
            onConfirm = {
                viewModel.deleteCustomDeadline(deadlineToDeleteId!!)
                deadlineToDeleteId = null
            }
        )
    }
}
