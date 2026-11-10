package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HackTrackRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HackTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HackTrackRepository.getInstance(application)

    val hackathons: StateFlow<List<Hackathon>> = repository.hackathons
    val tasks: StateFlow<List<TaskItem>> = repository.tasks
    val projects: StateFlow<List<Project>> = repository.projects
    val submissions: StateFlow<List<SubmissionItem>> = repository.submissions
    val achievements: StateFlow<List<Achievement>> = repository.achievements
    val customDeadlines: StateFlow<List<CustomDeadline>> = repository.customDeadlines
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val isSyncing: StateFlow<Boolean> = repository.isSyncing

    // Selected Hackathon for Workspace Detail Screen
    private val _selectedHackathonId = MutableStateFlow<String?>(null)
    val selectedHackathonId: StateFlow<String?> = _selectedHackathonId.asStateFlow()

    val selectedHackathon: StateFlow<Hackathon?> = combine(hackathons, _selectedHackathonId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filters & Search
    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

    private val _taskStatusFilter = MutableStateFlow<String?>(null)
    val taskStatusFilter: StateFlow<String?> = _taskStatusFilter.asStateFlow()

    private val _taskPriorityFilter = MutableStateFlow<String?>(null)
    val taskPriorityFilter: StateFlow<String?> = _taskPriorityFilter.asStateFlow()

    private val _hackathonStatusFilter = MutableStateFlow<String?>(null)
    val hackathonStatusFilter: StateFlow<String?> = _hackathonStatusFilter.asStateFlow()

    // Filtered Tasks
    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        tasks,
        _taskSearchQuery,
        _taskStatusFilter,
        _taskPriorityFilter
    ) { allTasks, query, status, priority ->
        allTasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesStatus = status == null || task.status == status
            val matchesPriority = priority == null || task.priority == priority
            matchesQuery && matchesStatus && matchesPriority
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        hackathons,
        tasks,
        submissions,
        achievements
    ) { hList, tList, sList, aList ->
        val active = hList.count { it.status in listOf("Registered", "Building", "Finalist", "Shortlisted") && !it.isArchived }
        val pending = tList.count { it.status != "Completed" }
        val completedSubs = sList.count { it.status in listOf("Completed", "Submitted", "Verified") }
        val shortlisted = hList.count { it.status in listOf("Shortlisted", "Finalist") }
        val wins = hList.count { it.status == "Winner" } + aList.count { it.result.contains("Winner", ignoreCase = true) }

        DashboardStats(
            totalHackathons = hList.size,
            activeHackathons = active,
            pendingTasks = pending,
            completedSubmissions = completedSubs,
            shortlistedCount = shortlisted,
            totalWins = wins
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun selectHackathon(id: String?) {
        _selectedHackathonId.value = id
    }

    fun setTaskSearchQuery(query: String) {
        _taskSearchQuery.value = query
    }

    fun setTaskStatusFilter(status: String?) {
        _taskStatusFilter.value = status
    }

    fun setTaskPriorityFilter(priority: String?) {
        _taskPriorityFilter.value = priority
    }

    fun setHackathonStatusFilter(status: String?) {
        _hackathonStatusFilter.value = status
    }

    // CRUD Actions
    fun addHackathon(hackathon: Hackathon) {
        viewModelScope.launch {
            repository.addHackathon(hackathon)
        }
    }

    fun updateHackathon(hackathon: Hackathon) {
        viewModelScope.launch {
            repository.updateHackathon(hackathon)
        }
    }

    fun deleteHackathon(id: String) {
        viewModelScope.launch {
            repository.deleteHackathon(id)
            if (_selectedHackathonId.value == id) {
                _selectedHackathonId.value = null
            }
        }
    }

    fun addTask(task: TaskItem) {
        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun toggleTaskCompletion(id: String) {
        repository.toggleTaskCompletion(id)
    }

    fun addProject(project: Project) {
        viewModelScope.launch {
            repository.addProject(project)
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    private val aiService = com.example.ai.HackTrackAiService(application)

    val themeMode: StateFlow<com.example.ui.theme.ThemeMode> = repository.themeMode
    val isGoogleCalendarSyncEnabled: StateFlow<Boolean> = repository.isGoogleCalendarSyncEnabled

    private val _isExtractingUrl = MutableStateFlow(false)
    val isExtractingUrl: StateFlow<Boolean> = _isExtractingUrl.asStateFlow()

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setGoogleCalendarSyncEnabled(enabled: Boolean) {
        repository.setGoogleCalendarSyncEnabled(enabled)
    }

    fun extractHackathonFromUrl(
        url: String,
        onSuccess: (Hackathon) -> Unit,
        onError: (String) -> Unit
    ) {
        if (url.isBlank()) {
            onError("Please enter a valid website link.")
            return
        }
        _isExtractingUrl.value = true
        viewModelScope.launch {
            try {
                val extracted = aiService.extractHackathonFromUrl(url.trim())
                _isExtractingUrl.value = false
                onSuccess(extracted)
            } catch (e: Exception) {
                _isExtractingUrl.value = false
                onError(e.localizedMessage ?: "Failed to extract hackathon details.")
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun addSubmission(submission: SubmissionItem) {
        viewModelScope.launch {
            repository.addSubmission(submission)
        }
    }

    fun updateSubmission(submission: SubmissionItem) {
        viewModelScope.launch {
            repository.updateSubmission(submission)
        }
    }

    fun deleteSubmission(id: String) {
        viewModelScope.launch {
            repository.deleteSubmission(id)
        }
    }

    fun addAchievement(achievement: Achievement) {
        viewModelScope.launch {
            repository.addAchievement(achievement)
        }
    }

    fun updateAchievement(achievement: Achievement) {
        viewModelScope.launch {
            repository.updateAchievement(achievement)
        }
    }

    fun deleteAchievement(id: String) {
        viewModelScope.launch {
            repository.deleteAchievement(id)
        }
    }

    // Round operations
    fun addRound(hackathonId: String, round: HackathonRound) {
        viewModelScope.launch {
            repository.addRoundToHackathon(hackathonId, round)
        }
    }

    fun updateRound(hackathonId: String, round: HackathonRound) {
        viewModelScope.launch {
            repository.updateRoundInHackathon(hackathonId, round)
        }
    }

    fun deleteRound(hackathonId: String, roundId: String) {
        viewModelScope.launch {
            repository.deleteRoundFromHackathon(hackathonId, roundId)
        }
    }

    fun toggleRoundCompletion(hackathonId: String, roundId: String) {
        viewModelScope.launch {
            repository.toggleRoundCompletion(hackathonId, roundId)
        }
    }

    // Custom deadline operations
    fun addCustomDeadline(deadline: CustomDeadline) {
        viewModelScope.launch {
            repository.addCustomDeadline(deadline)
        }
    }

    fun updateCustomDeadline(deadline: CustomDeadline) {
        viewModelScope.launch {
            repository.updateCustomDeadline(deadline)
        }
    }

    fun deleteCustomDeadline(id: String) {
        viewModelScope.launch {
            repository.deleteCustomDeadline(id)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun clearAllData() {
        repository.clearAllData()
    }

    fun loadSampleData() {
        repository.loadSampleData()
    }

    fun resetToSampleData() {
        repository.resetToSampleData()
    }
}
