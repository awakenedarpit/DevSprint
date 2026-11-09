package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Hackathon
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HackathonCard
import com.example.viewmodel.HackTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonsScreen(
    viewModel: HackTrackViewModel,
    onNavigateToDetail: (String) -> Unit,
    onOpenAddDialog: () -> Unit
) {
    val hackathons by viewModel.hackathons.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Building", "Registered", "Finalist", "Winner", "Completed", "Archived")

    val displayedHackathons = remember(hackathons, searchQuery, selectedFilter) {
        hackathons.filter { h ->
            val matchesSearch = searchQuery.isBlank() ||
                    h.name.contains(searchQuery, ignoreCase = true) ||
                    h.organizer.contains(searchQuery, ignoreCase = true) ||
                    h.domain.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "All" -> !h.isArchived
                "Archived" -> h.isArchived
                else -> h.status.equals(selectedFilter, ignoreCase = true) && !h.isArchived
            }
            matchesSearch && matchesFilter
        }
    }

    var hackathonToEdit by remember { mutableStateOf<Hackathon?>(null) }
    var hackathonToDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddDialog,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add / Extract URL", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search competitions, organizers, domains...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
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

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // List of Hackathons
            if (displayedHackathons.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.RocketLaunch,
                    title = "No competitions found",
                    description = if (searchQuery.isNotBlank()) "No competitions match your search query." else "You haven't added any hackathons in this category yet.",
                    actionButtonText = "Add Hackathon",
                    onActionClick = onOpenAddDialog,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayedHackathons, key = { it.id }) { hackathon ->
                        HackathonCard(
                            hackathon = hackathon,
                            onClick = { onNavigateToDetail(hackathon.id) },
                            onEdit = { hackathonToEdit = hackathon },
                            onDelete = { hackathonToDeleteId = hackathon.id }
                        )
                    }
                }
            }
        }
    }

    if (hackathonToEdit != null) {
        com.example.ui.components.EditHackathonDialog(
            hackathon = hackathonToEdit!!,
            onDismiss = { hackathonToEdit = null },
            onConfirm = { updated ->
                viewModel.updateHackathon(updated)
                hackathonToEdit = null
            }
        )
    }

    if (hackathonToDeleteId != null) {
        com.example.ui.components.DeleteConfirmationDialog(
            title = "Delete Hackathon?",
            message = "Are you sure you want to delete this hackathon competition?",
            onDismiss = { hackathonToDeleteId = null },
            onConfirm = {
                viewModel.deleteHackathon(hackathonToDeleteId!!)
                hackathonToDeleteId = null
            }
        )
    }
}
