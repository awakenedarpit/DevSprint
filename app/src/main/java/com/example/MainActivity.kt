package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Hackathon
import com.example.model.TaskItem
import com.example.ui.components.AddHackathonDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.screens.*
import com.example.ui.theme.HackTrackTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.AiAssistantViewModel
import com.example.viewmodel.HackTrackViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class NavDestination(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    HACKATHONS("Hackathons", Icons.Filled.RocketLaunch, Icons.Outlined.RocketLaunch),
    TASKS("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    AI("HackTrack AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    ACHIEVEMENTS("Trophies", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            HackTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isGuestMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !isGuestMode) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            },
            onContinueAsGuest = {
                isGuestMode = true
            }
        )
    } else {
        MainAppContent(
            onSignOut = {
                currentUser = null
                isGuestMode = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(onSignOut: () -> Unit) {
    val hackTrackViewModel: HackTrackViewModel = viewModel()
    val aiViewModel: AiAssistantViewModel = viewModel()

    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    var viewingHackathonId by remember { mutableStateOf<String?>(null) }

    var showAddHackathonDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var defaultHackathonIdForTask by remember { mutableStateOf<String?>(null) }

    val hackathons by hackTrackViewModel.hackathons.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Navigation Rail on large screens
            if (isTablet) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NavDestination.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination && viewingHackathonId == null,
                            onClick = {
                                viewingHackathonId = null
                                currentDestination = destination
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == destination && viewingHackathonId == null) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                bottomBar = {
                    if (!isTablet && viewingHackathonId == null) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavDestination.entries.take(5).forEach { destination ->
                                NavigationBarItem(
                                    selected = currentDestination == destination,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination == destination) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (viewingHackathonId != null) {
                        HackathonWorkspaceScreen(
                            hackathonId = viewingHackathonId!!,
                            viewModel = hackTrackViewModel,
                            onBack = { viewingHackathonId = null },
                            onOpenAddTaskForHackathon = { hId ->
                                defaultHackathonIdForTask = hId
                                showAddTaskDialog = true
                            }
                        )
                    } else {
                        when (currentDestination) {
                            NavDestination.HOME -> DashboardScreen(
                                viewModel = hackTrackViewModel,
                                onNavigateToHackathonDetail = { id -> viewingHackathonId = id },
                                onNavigateToTasks = { currentDestination = NavDestination.TASKS },
                                onNavigateToAiAssistant = { currentDestination = NavDestination.AI },
                                onOpenAddHackathonDialog = { showAddHackathonDialog = true },
                                onOpenAddTaskDialog = {
                                    defaultHackathonIdForTask = null
                                    showAddTaskDialog = true
                                }
                            )
                            NavDestination.HACKATHONS -> HackathonsScreen(
                                viewModel = hackTrackViewModel,
                                onNavigateToDetail = { id -> viewingHackathonId = id },
                                onOpenAddDialog = { showAddHackathonDialog = true }
                            )
                            NavDestination.TASKS -> TasksScreen(
                                viewModel = hackTrackViewModel,
                                onOpenAddTaskDialog = {
                                    defaultHackathonIdForTask = null
                                    showAddTaskDialog = true
                                }
                            )
                            NavDestination.CALENDAR -> CalendarScreen(
                                viewModel = hackTrackViewModel,
                                onNavigateToHackathonDetail = { id -> viewingHackathonId = id }
                            )
                            NavDestination.AI -> AiAssistantScreen(
                                aiViewModel = aiViewModel,
                                hackTrackViewModel = hackTrackViewModel
                            )
                            NavDestination.ACHIEVEMENTS -> AchievementsScreen(
                                viewModel = hackTrackViewModel,
                                onOpenAddDialog = { /* Add achievement */ }
                            )
                            NavDestination.PROFILE -> ProfileSettingsScreen(
                                viewModel = hackTrackViewModel,
                                onSignOutRequested = onSignOut
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddHackathonDialog) {
        AddHackathonDialog(
            onDismiss = { showAddHackathonDialog = false },
            onConfirm = { newHack ->
                hackTrackViewModel.addHackathon(newHack)
                showAddHackathonDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            hackathons = hackathons,
            defaultHackathonId = defaultHackathonIdForTask,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { newTask ->
                hackTrackViewModel.addTask(newTask)
                showAddTaskDialog = false
            }
        )
    }
}
