package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Hackathon
import com.example.model.TaskItem
import com.example.ui.components.AddHackathonDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.screens.*
import com.example.ui.theme.HackTrackTheme
import com.example.ui.theme.ThemeMode
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
    ANALYTICS("Analytics", Icons.Filled.Insights, Icons.Outlined.Insights),
    AI("Dev Sprint AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ACHIEVEMENTS("Milestones", Icons.Filled.MilitaryTech, Icons.Outlined.MilitaryTech),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val hackTrackViewModel: HackTrackViewModel = viewModel()
            val themeMode by hackTrackViewModel.themeMode.collectAsState()

            HackTrackTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(hackTrackViewModel = hackTrackViewModel)
                }
            }
        }
    }
}

@Composable
fun AppRoot(hackTrackViewModel: HackTrackViewModel) {
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
            hackTrackViewModel = hackTrackViewModel,
            onSignOut = {
                currentUser = null
                isGuestMode = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    hackTrackViewModel: HackTrackViewModel,
    onSignOut: () -> Unit
) {
    val aiViewModel: AiAssistantViewModel = viewModel()
    val themeMode by hackTrackViewModel.themeMode.collectAsState()

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
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (viewingHackathonId != null) "Workspace" else currentDestination.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        actions = {
                            // Quick Theme Mode Toggle Button
                            IconButton(
                                onClick = {
                                    val nextMode = when (themeMode) {
                                        ThemeMode.LIGHT -> ThemeMode.DARK
                                        ThemeMode.DARK -> ThemeMode.SYSTEM
                                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                    }
                                    hackTrackViewModel.setThemeMode(nextMode)
                                }
                            ) {
                                Icon(
                                    imageVector = when (themeMode) {
                                        ThemeMode.LIGHT -> Icons.Default.LightMode
                                        ThemeMode.DARK -> Icons.Default.DarkMode
                                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    },
                                    contentDescription = "Toggle Theme"
                                )
                            }

                            // Calendar quick access
                            IconButton(
                                onClick = {
                                    viewingHackathonId = null
                                    currentDestination = NavDestination.CALENDAR
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentDestination == NavDestination.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                    contentDescription = "Calendar",
                                    tint = if (currentDestination == NavDestination.CALENDAR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Profile & Settings quick access
                            IconButton(
                                onClick = {
                                    viewingHackathonId = null
                                    currentDestination = NavDestination.PROFILE
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentDestination == NavDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile & Settings",
                                    tint = if (currentDestination == NavDestination.PROFILE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
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
                                onNavigateToCalendar = { currentDestination = NavDestination.CALENDAR },
                                onNavigateToAnalytics = { currentDestination = NavDestination.ANALYTICS },
                                onNavigateToAchievements = { currentDestination = NavDestination.ACHIEVEMENTS },
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
                            NavDestination.ANALYTICS -> AnalyticsScreen(
                                viewModel = hackTrackViewModel,
                                onNavigateToTasks = { currentDestination = NavDestination.TASKS },
                                onNavigateToHackathons = { currentDestination = NavDestination.HACKATHONS },
                                onNavigateToAiAssistant = { currentDestination = NavDestination.AI }
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
            },
            onExtractFromUrl = { url, onSuccess, onError ->
                hackTrackViewModel.extractHackathonFromUrl(url, onSuccess, onError)
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
