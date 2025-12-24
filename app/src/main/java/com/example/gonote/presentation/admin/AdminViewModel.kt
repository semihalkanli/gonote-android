package com.example.gonote.presentation.admin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonote.data.local.dao.CategoryCount
import com.example.gonote.data.local.dao.CityNoteCount
import com.example.gonote.data.local.entity.AdminActivityLog
import com.example.gonote.data.local.entity.LoginHistory
import com.example.gonote.data.local.entity.UserStatus
import com.example.gonote.data.model.Note
import com.example.gonote.data.repository.NoteRepository
import com.example.gonote.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class UserStats(
    val userId: String,
    val email: String,
    val noteCount: Int,
    val photoCount: Int,
    val lastActivity: Long?,
    val createdAt: Long,
    val status: UserStatus = UserStatus.ACTIVE,
    val lastLoginAt: Long? = null,
    val lastLoginIp: String? = null
)

data class AdminDashboardState(
    val totalUsers: Int = 0,
    val totalNotes: Int = 0,
    val totalPhotos: Int = 0,
    val isLoading: Boolean = true
)

data class AdminUsersState(
    val users: List<UserStats> = emptyList(),
    val isLoading: Boolean = true
)

data class AdminStatsState(
    val citiesWithCount: List<CityNoteCount> = emptyList(),
    val categoriesWithCount: List<CategoryCount> = emptyList(),
    val allNotes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)

data class AdminLogsState(
    val logs: List<AdminActivityLog> = emptyList(),
    val isLoading: Boolean = true
)

data class LoginHistoryState(
    val history: List<LoginHistory> = emptyList(),
    val isLoading: Boolean = true
)

class AdminViewModel(
    private val repository: NoteRepository,
    private val context: Context
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(AdminDashboardState())
    val dashboardState: StateFlow<AdminDashboardState> = _dashboardState.asStateFlow()

    private val _usersState = MutableStateFlow(AdminUsersState())
    val usersState: StateFlow<AdminUsersState> = _usersState.asStateFlow()

    private val _statsState = MutableStateFlow(AdminStatsState())
    val statsState: StateFlow<AdminStatsState> = _statsState.asStateFlow()

    private val _userNotes = MutableStateFlow<List<Note>>(emptyList())
    val userNotes: StateFlow<List<Note>> = _userNotes.asStateFlow()

    private val _logsState = MutableStateFlow(AdminLogsState())
    val logsState: StateFlow<AdminLogsState> = _logsState.asStateFlow()

    private val _loginHistoryState = MutableStateFlow(LoginHistoryState())
    val loginHistoryState: StateFlow<LoginHistoryState> = _loginHistoryState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)

            combine(
                repository.getTotalUsersCount(),
                repository.getTotalNotesCountAll(),
                repository.getTotalPhotosCountAll()
            ) { usersCount, notesCount, photosCount ->
                AdminDashboardState(
                    totalUsers = usersCount,
                    totalNotes = notesCount,
                    totalPhotos = photosCount,
                    isLoading = false
                )
            }.collect { state ->
                _dashboardState.value = state
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = _usersState.value.copy(isLoading = true)

            repository.getAllUsers().collect { users ->
                // Debug logging removed for production
                
                val userStatsList = users.map { user ->
                    val noteCount = repository.getTotalNotesCount(user.id).first()
                    val photoCount = repository.getPhotosCount(user.id).first()
                    val lastActivity = repository.getLastActivityByUser(user.id)

                    UserStats(
                        userId = user.id,
                        email = user.email,
                        noteCount = noteCount,
                        photoCount = photoCount,
                        lastActivity = lastActivity,
                        createdAt = user.createdAt,
                        status = user.status,
                        lastLoginAt = user.lastLoginAt,
                        lastLoginIp = user.lastLoginIp
                    )
                }.sortedByDescending { it.createdAt }

                // Debug logging removed for production
                
                _usersState.value = AdminUsersState(
                    users = userStatsList,
                    isLoading = false
                )
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = _statsState.value.copy(isLoading = true)

            combine(
                repository.getCitiesWithNoteCount(),
                repository.getCategoriesWithCount(),
                repository.getAllNotesAdmin()
            ) { cities, categories, notes ->
                AdminStatsState(
                    citiesWithCount = cities,
                    categoriesWithCount = categories,
                    allNotes = notes,
                    isLoading = false
                )
            }.collect { state ->
                _statsState.value = state
            }
        }
    }

    fun loadUserNotes(userId: String) {
        viewModelScope.launch {
            repository.getNotesByUserIdFlow(userId).collect { notes ->
                _userNotes.value = notes
            }
        }
    }

    fun deleteUser(userId: String, adminEmail: String) {
        viewModelScope.launch {
            // Log the action
            repository.logAdminAction(
                adminEmail = adminEmail,
                actionType = "DELETE_USER",
                targetId = userId,
                targetType = "USER",
                details = "User deleted by admin"
            )
            
            repository.deleteUserById(userId)
            loadUsers()
            loadDashboard()
        }
    }

    // ========== USER STATUS MANAGEMENT ==========
    fun updateUserStatus(userId: String, newStatus: UserStatus, adminEmail: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, newStatus)
            
            // Log the action
            val actionType = when (newStatus) {
                UserStatus.BANNED -> "BAN_USER"
                UserStatus.ACTIVE -> "UNBAN_USER"
                UserStatus.INACTIVE -> "DEACTIVATE_USER"
            }
            
            repository.logAdminAction(
                adminEmail = adminEmail,
                actionType = actionType,
                targetId = userId,
                targetType = "USER",
                details = "User status changed to ${newStatus.name}"
            )
            
            loadUsers()
        }
    }

    // ========== ADMIN ACTIVITY LOGS ==========
    fun loadAdminLogs(limit: Int = 100) {
        viewModelScope.launch {
            _logsState.value = _logsState.value.copy(isLoading = true)
            
            repository.getAdminLogs(limit).collect { logs ->
                _logsState.value = AdminLogsState(
                    logs = logs,
                    isLoading = false
                )
            }
        }
    }

    // ========== LOGIN HISTORY ==========
    fun loadLoginHistory(userId: String? = null, limit: Int = 100) {
        viewModelScope.launch {
            _loginHistoryState.value = _loginHistoryState.value.copy(isLoading = true)
            
            val historyFlow = if (userId != null) {
                repository.getLoginHistory(userId, limit)
            } else {
                repository.getAllLoginHistory(limit)
            }
            
            historyFlow.collect { history ->
                _loginHistoryState.value = LoginHistoryState(
                    history = history,
                    isLoading = false
                )
            }
        }
    }

    // ========== NOTIFICATIONS ==========
    fun sendNotification(userIds: List<String>?, title: String, message: String, adminEmail: String) {
        viewModelScope.launch {
            if (userIds.isNullOrEmpty()) {
                // Send to all users
                repository.sendNotification(null, title, message)
                
                repository.logAdminAction(
                    adminEmail = adminEmail,
                    actionType = "SEND_NOTIFICATION",
                    targetId = "ALL",
                    targetType = "USER",
                    details = "Notification: $title - $message"
                )
            } else {
                // Send to specific users
                userIds.forEach { userId ->
                    repository.sendNotification(userId, title, message)
                }
                
                repository.logAdminAction(
                    adminEmail = adminEmail,
                    actionType = "SEND_NOTIFICATION",
                    targetId = userIds.joinToString(","),
                    targetType = "USER",
                    details = "Notification to ${userIds.size} users: $title"
                )
            }
            
            // Show system notification
            val notificationId = Random.nextInt(1000, 9999)
            NotificationHelper.showNotification(
                context = context,
                notificationId = notificationId,
                title = title,
                message = message
            )
        }
    }

    // ========== NOTE EDITING ==========
    fun editNote(noteId: Long, title: String, content: String, category: String, adminEmail: String) {
        viewModelScope.launch {
            repository.updateNoteByAdmin(noteId, title, content, category)
            
            repository.logAdminAction(
                adminEmail = adminEmail,
                actionType = "EDIT_NOTE",
                targetId = noteId.toString(),
                targetType = "NOTE",
                details = "Note edited: $title"
            )
        }
    }
}














