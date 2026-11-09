package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "HackTrackRepo"

class HackTrackRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val auth = Firebase.auth

    // In-memory reactive state flows for zero-latency UI
    private val _hackathons = MutableStateFlow<List<Hackathon>>(emptyList())
    val hackathons: StateFlow<List<Hackathon>> = _hackathons.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _submissions = MutableStateFlow<List<SubmissionItem>>(emptyList())
    val submissions: StateFlow<List<SubmissionItem>> = _submissions.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile(userId = auth.currentUser?.uid ?: "guest"))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Load initial local/sample baseline
        loadBaselineData(auth.currentUser?.uid ?: "guest_user")

        // Listen to Auth state changes to synchronize with Firestore
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                syncWithFirestore(user.uid)
            } else {
                loadBaselineData("guest_user")
            }
        }
    }

    private fun loadBaselineData(userId: String) {
        _hackathons.value = SampleDataProvider.getDefaultHackathons(userId)
        _tasks.value = SampleDataProvider.getDefaultTasks(userId)
        _projects.value = SampleDataProvider.getDefaultProjects(userId)
        _submissions.value = SampleDataProvider.getDefaultSubmissions(userId)
        _achievements.value = SampleDataProvider.getDefaultAchievements(userId)
        _userProfile.value = UserProfile(
            userId = userId,
            name = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Arpit Raj",
            email = auth.currentUser?.email ?: "raj2010arpit@gmail.com"
        )
    }

    fun syncWithFirestore(uid: String) {
        repositoryScope.launch {
            _isSyncing.value = true
            try {
                val userDoc = firestore.collection("users").document(uid)

                // 1. Sync User Profile
                val profileSnap = userDoc.get().await()
                if (profileSnap.exists()) {
                    profileSnap.toObject(UserProfile::class.java)?.let {
                        _userProfile.value = it
                    }
                } else {
                    // Seed initial profile on Firestore
                    val initialProfile = _userProfile.value.copy(
                        userId = uid,
                        name = auth.currentUser?.displayName ?: "Arpit Raj",
                        email = auth.currentUser?.email ?: "raj2010arpit@gmail.com"
                    )
                    userDoc.set(
                        mapOf(
                            "userId" to uid,
                            "name" to initialProfile.name,
                            "email" to initialProfile.email,
                            "college" to initialProfile.college,
                            "course" to initialProfile.course,
                            "year" to initialProfile.year,
                            "bio" to initialProfile.bio,
                            "timezone" to initialProfile.timezone,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }

                // 2. Observe Hackathons
                userDoc.collection("hackathons").snapshots().collect { snap ->
                    val remote = snap.toObjects<Hackathon>()
                    if (remote.isNotEmpty()) {
                        _hackathons.value = remote
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore sync note (offline/initial): ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // --- Hackathon Operations ---
    suspend fun addHackathon(hackathon: Hackathon) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        val id = if (hackathon.id.isNotBlank()) hackathon.id else "hack_" + UUID.randomUUID().toString().take(8)
        val newItem = hackathon.copy(id = id, userId = uid)
        _hackathons.update { listOf(newItem) + it }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("hackathons").document(id)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "name" to newItem.name,
                            "organizer" to newItem.organizer,
                            "websiteUrl" to newItem.websiteUrl,
                            "registrationUrl" to newItem.registrationUrl,
                            "description" to newItem.description,
                            "problemStatement" to newItem.problemStatement,
                            "domain" to newItem.domain,
                            "mode" to newItem.mode,
                            "teamName" to newItem.teamName,
                            "teamSize" to newItem.teamSize,
                            "status" to newItem.status,
                            "registrationDate" to newItem.registrationDate,
                            "registrationDeadline" to newItem.registrationDeadline,
                            "submissionDeadline" to newItem.submissionDeadline,
                            "finalEventDate" to newItem.finalEventDate,
                            "prizeInfo" to newItem.prizeInfo,
                            "isArchived" to newItem.isArchived,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save hackathon to Firestore: ${e.message}")
            }
        }
    }

    suspend fun updateHackathon(hackathon: Hackathon) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        _hackathons.update { list -> list.map { if (it.id == hackathon.id) hackathon else it } }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("hackathons").document(hackathon.id)
                    .set(hackathon.copy(updatedAt = null)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update hackathon in Firestore: ${e.message}")
            }
        }
    }

    suspend fun deleteHackathon(id: String) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        _hackathons.update { list -> list.filter { it.id != id } }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("hackathons").document(id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete hackathon: ${e.message}")
            }
        }
    }

    // --- Task Operations ---
    suspend fun addTask(task: TaskItem) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        val id = if (task.id.isNotBlank()) task.id else "task_" + UUID.randomUUID().toString().take(8)
        val newItem = task.copy(id = id, userId = uid)
        _tasks.update { listOf(newItem) + it }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("tasks").document(id)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "hackathonId" to newItem.hackathonId,
                            "projectId" to newItem.projectId,
                            "title" to newItem.title,
                            "description" to newItem.description,
                            "category" to newItem.category,
                            "priority" to newItem.priority,
                            "status" to newItem.status,
                            "dueDate" to newItem.dueDate,
                            "assignedMember" to newItem.assignedMember,
                            "notes" to newItem.notes,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save task to Firestore: ${e.message}")
            }
        }
    }

    suspend fun updateTask(task: TaskItem) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        _tasks.update { list -> list.map { if (it.id == task.id) task else it } }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("tasks").document(task.id)
                    .set(task.copy(updatedAt = null)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task in Firestore: ${e.message}")
            }
        }
    }

    suspend fun deleteTask(id: String) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        _tasks.update { list -> list.filter { it.id != id } }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("tasks").document(id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task: ${e.message}")
            }
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        val existing = _tasks.value.firstOrNull { it.id == taskId } ?: return
        val newStatus = if (existing.status == "Completed") "In Progress" else "Completed"
        val updated = existing.copy(status = newStatus)
        repositoryScope.launch {
            updateTask(updated)
        }
    }

    // --- Project Operations ---
    suspend fun addProject(project: Project) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        val id = if (project.id.isNotBlank()) project.id else "proj_" + UUID.randomUUID().toString().take(8)
        val newItem = project.copy(id = id, userId = uid)
        _projects.update { listOf(newItem) + it }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("projects").document(id)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "hackathonId" to newItem.hackathonId,
                            "title" to newItem.title,
                            "description" to newItem.description,
                            "problemStatement" to newItem.problemStatement,
                            "proposedSolution" to newItem.proposedSolution,
                            "techStack" to newItem.techStack,
                            "githubRepo" to newItem.githubRepo,
                            "liveDeployment" to newItem.liveDeployment,
                            "demoVideo" to newItem.demoVideo,
                            "documentation" to newItem.documentation,
                            "status" to newItem.status,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save project to Firestore: ${e.message}")
            }
        }
    }

    suspend fun updateProject(project: Project) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        _projects.update { list -> list.map { if (it.id == project.id) project else it } }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("projects").document(project.id)
                    .set(project.copy(updatedAt = null)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update project: ${e.message}")
            }
        }
    }

    // --- Submission Operations ---
    suspend fun addSubmission(submission: SubmissionItem) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        val id = if (submission.id.isNotBlank()) submission.id else "sub_" + UUID.randomUUID().toString().take(8)
        val newItem = submission.copy(id = id, userId = uid)
        _submissions.update { listOf(newItem) + it }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("submissions").document(id)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "hackathonId" to newItem.hackathonId,
                            "title" to newItem.title,
                            "deliverableType" to newItem.deliverableType,
                            "fileUrl" to newItem.fileUrl,
                            "externalUrl" to newItem.externalUrl,
                            "notes" to newItem.notes,
                            "status" to newItem.status,
                            "submittedAt" to newItem.submittedAt,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add submission: ${e.message}")
            }
        }
    }

    suspend fun updateSubmission(submission: SubmissionItem) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        _submissions.update { list -> list.map { if (it.id == submission.id) submission else it } }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("submissions").document(submission.id)
                    .set(submission.copy(updatedAt = null)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update submission: ${e.message}")
            }
        }
    }

    // --- Achievement Operations ---
    suspend fun addAchievement(achievement: Achievement) {
        val uid = auth.currentUser?.uid ?: "guest_user"
        val id = if (achievement.id.isNotBlank()) achievement.id else "ach_" + UUID.randomUUID().toString().take(8)
        val newItem = achievement.copy(id = id, userId = uid)
        _achievements.update { listOf(newItem) + it }

        if (auth.currentUser != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("achievements").document(id)
                    .set(
                        mapOf(
                            "userId" to uid,
                            "hackathonId" to newItem.hackathonId,
                            "competitionName" to newItem.competitionName,
                            "projectName" to newItem.projectName,
                            "result" to newItem.result,
                            "rank" to newItem.rank,
                            "date" to newItem.date,
                            "organizer" to newItem.organizer,
                            "certificateUrl" to newItem.certificateUrl,
                            "proofUrl" to newItem.proofUrl,
                            "description" to newItem.description,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add achievement: ${e.message}")
            }
        }
    }

    // --- Profile Operations ---
    suspend fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .set(profile.copy(updatedAt = null)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update profile: ${e.message}")
            }
        }
    }

    fun resetToSampleData() {
        val uid = auth.currentUser?.uid ?: "guest_user"
        loadBaselineData(uid)
    }

    companion object {
        @Volatile
        private var INSTANCE: HackTrackRepository? = null

        fun getInstance(context: Context): HackTrackRepository {
            return INSTANCE ?: synchronized(this) {
                val dbId = context.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(dbId)
                HackTrackRepository(context.applicationContext, firestore).also { INSTANCE = it }
            }
        }
    }
}
