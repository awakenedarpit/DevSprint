package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.model.Hackathon
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.HackTrackDanger
import com.example.ui.theme.HackTrackPrimary
import com.example.ui.theme.HackTrackSuccess
import com.example.ui.theme.HackTrackWarning
import com.example.viewmodel.HackTrackViewModel

data class DeadlineEvent(
    val title: String,
    val competition: String,
    val type: String, // Registration, Submission, Presentation, Task
    val date: String,
    val isCritical: Boolean = false
)

@Composable
fun CalendarScreen(
    viewModel: HackTrackViewModel,
    onNavigateToHackathonDetail: (String) -> Unit
) {
    val hackathons by viewModel.hackathons.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val events = remember(hackathons, tasks) {
        val list = mutableListOf<DeadlineEvent>()
        hackathons.filter { !it.isArchived }.forEach { h ->
            if (h.submissionDeadline.isNotBlank()) {
                list.add(
                    DeadlineEvent(
                        title = "Final Project & Code Submission",
                        competition = h.name,
                        type = "Submission Deadline",
                        date = h.submissionDeadline,
                        isCritical = true
                    )
                )
            }
            if (h.registrationDeadline.isNotBlank()) {
                list.add(
                    DeadlineEvent(
                        title = "Team Registration Closes",
                        competition = h.name,
                        type = "Registration",
                        date = h.registrationDeadline,
                        isCritical = false
                    )
                )
            }
            if (h.finalEventDate.isNotBlank()) {
                list.add(
                    DeadlineEvent(
                        title = "Grand Finale & Winner Demos",
                        competition = h.name,
                        type = "Presentation",
                        date = h.finalEventDate,
                        isCritical = false
                    )
                )
            }
        }
        tasks.filter { it.status != "Completed" && it.dueDate.isNotBlank() }.forEach { t ->
            list.add(
                DeadlineEvent(
                    title = t.title,
                    competition = "Sprint Task",
                    type = "Task Milestone",
                    date = t.dueDate,
                    isCritical = t.priority == "Critical"
                )
            )
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Month / Header Card
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
                    Text(
                        text = "Deadlines & Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${events.size} upcoming",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stay ahead of competition countdowns with automated priority tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.EventBusy,
                title = "No upcoming deadlines",
                description = "Register for hackathons or add task due dates to populate your timeline."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    val typeColor = when (event.type) {
                        "Submission Deadline" -> HackTrackDanger
                        "Registration" -> HackTrackWarning
                        "Presentation" -> HackTrackSuccess
                        else -> HackTrackPrimary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(typeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (event.type == "Submission Deadline") Icons.Default.Timer else Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = event.competition,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = typeColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = event.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = typeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = event.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
