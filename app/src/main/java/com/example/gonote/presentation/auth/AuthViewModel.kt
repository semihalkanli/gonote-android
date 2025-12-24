package com.example.gonote.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonote.data.admin.AdminConfig
import com.example.gonote.data.local.UserPreferences
import com.example.gonote.data.local.entity.UserEntity
import com.example.gonote.data.local.entity.UserStatus
import com.example.gonote.data.repository.NoteRepository
import com.example.gonote.util.DeviceInfoUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isRegistered: Boolean = false,
    val userNotFound: Boolean = false,
    val isAdmin: Boolean = false
)

class AuthViewModel(
    private val userPreferences: UserPreferences,
    private val repository: NoteRepository
) : ViewModel() {

    companion object {
        private const val DEMO_USER_EMAIL = "demo@gmail.com"
        private const val DEMO_USER_ID = "demo_user_001"
    }

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            userPreferences.isLoggedIn.collect { isLoggedIn ->
                _authState.value = _authState.value.copy(isLoggedIn = isLoggedIn)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, userNotFound = false, isAdmin = false)

            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Email and password are required"
                )
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Invalid email format"
                )
                return@launch
            }

            if (password.length < 6) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Password must be at least 6 characters"
                )
                return@launch
            }

            val normalizedEmail = email.lowercase().trim()
            
            // Check if this is admin login
            if (AdminConfig.validateCredentials(normalizedEmail, password)) {
                // Record admin login history
                val ipAddress = DeviceInfoUtil.getDeviceIpAddress()
                val deviceInfo = DeviceInfoUtil.getDeviceInfo()
                repository.recordLoginHistory(
                    userId = AdminConfig.ADMIN_USER_ID,
                    email = normalizedEmail,
                    ipAddress = ipAddress,
                    deviceInfo = deviceInfo
                )
                
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    isAdmin = true
                )
                return@launch
            }
            
            // Check if user exists in database (except demo user)
            if (normalizedEmail != DEMO_USER_EMAIL) {
                val existingUser = repository.getUserByEmail(normalizedEmail)
                
                if (existingUser == null) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        userNotFound = true
                    )
                    return@launch
                }
                
                // Check user status
                if (existingUser.status == UserStatus.BANNED) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Your account has been suspended. Please contact support."
                    )
                    return@launch
                }
                
                if (existingUser.status == UserStatus.INACTIVE) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Your account is inactive. Please contact support."
                    )
                    return@launch
                }
                
                // User exists and is active, login with their ID
                // Record login history
                val ipAddress = DeviceInfoUtil.getDeviceIpAddress()
                val deviceInfo = DeviceInfoUtil.getDeviceInfo()
                repository.recordLoginHistory(
                    userId = existingUser.id,
                    email = existingUser.email,
                    ipAddress = ipAddress,
                    deviceInfo = deviceInfo
                )
                repository.updateLastLogin(existingUser.id, ipAddress)
                
                userPreferences.saveUserSession(existingUser.id, existingUser.email)
            } else {
                // Demo user - always allow login, but also ensure user exists in database
                // Check if demo user exists in database, if not create it
                val existingDemoUser = repository.getUserByEmail(DEMO_USER_EMAIL)
                if (existingDemoUser == null) {
                    val demoUser = UserEntity(
                        id = DEMO_USER_ID,
                        email = DEMO_USER_EMAIL,
                        createdAt = System.currentTimeMillis(),
                        status = UserStatus.ACTIVE
                    )
                    repository.registerUser(demoUser)
                }
                
                // Record demo user login history
                val ipAddress = DeviceInfoUtil.getDeviceIpAddress()
                val deviceInfo = DeviceInfoUtil.getDeviceInfo()
                repository.recordLoginHistory(
                    userId = DEMO_USER_ID,
                    email = DEMO_USER_EMAIL,
                    ipAddress = ipAddress,
                    deviceInfo = deviceInfo
                )
                repository.updateLastLogin(DEMO_USER_ID, ipAddress)
                
                userPreferences.saveUserSession(DEMO_USER_ID, DEMO_USER_EMAIL)
            }

            _authState.value = _authState.value.copy(
                isLoading = false,
                isLoggedIn = true
            )
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, isRegistered = false)

            val trimmedPassword = password.trim()
            val trimmedConfirmPassword = confirmPassword.trim()

            // Validate inputs
            if (email.isBlank() || trimmedPassword.isBlank() || trimmedConfirmPassword.isBlank()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "All fields are required"
                )
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Invalid email format"
                )
                return@launch
            }

            if (trimmedPassword.length < 6) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Password must be at least 6 characters"
                )
                return@launch
            }

            if (trimmedPassword != trimmedConfirmPassword) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Passwords do not match"
                )
                return@launch
            }

            val normalizedEmail = email.lowercase().trim()

            // Check if user already exists
            val existingUser = repository.getUserByEmail(normalizedEmail)
            if (existingUser != null) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "An account with this email already exists"
                )
                return@launch
            }

            // Generate user ID
            val userId = if (normalizedEmail == DEMO_USER_EMAIL) {
                DEMO_USER_ID
            } else {
                UUID.randomUUID().toString()
            }

            // Save user to database
            val newUser = UserEntity(
                id = userId,
                email = normalizedEmail,
                createdAt = System.currentTimeMillis(),
                status = UserStatus.ACTIVE
            )
            repository.registerUser(newUser)

            // Don't auto-login, just mark as registered
            _authState.value = _authState.value.copy(
                isLoading = false,
                isRegistered = true
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUserSession()
            _authState.value = AuthState(isLoggedIn = false, isAdmin = false)
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun clearRegisteredFlag() {
        _authState.value = _authState.value.copy(isRegistered = false)
    }

    fun clearUserNotFoundFlag() {
        _authState.value = _authState.value.copy(userNotFound = false)
    }
}
