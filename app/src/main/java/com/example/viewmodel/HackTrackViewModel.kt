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

    fun addAchievement(achievement: Achievement) {
        viewModelScope.launch {
            repository.addAchievement(achievement)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun resetToSampleData() {
        repository.resetToSampleData()
    }
}
