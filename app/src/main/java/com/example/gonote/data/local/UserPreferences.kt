package com.example.gonote.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val AUTO_DARK_MODE = booleanPreferencesKey("auto_dark_mode")
        private val DEMO_DATA_LOADED = booleanPreferencesKey("demo_data_loaded")
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    val autoDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DARK_MODE] ?: false
    }

    suspend fun saveUserSession(userId: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email
            preferences[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences[IS_LOGGED_IN] = false
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun setAutoDarkMode(isAuto: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DARK_MODE] = isAuto
        }
    }

    val isDemoDataLoaded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEMO_DATA_LOADED] ?: false
    }

    suspend fun setDemoDataLoaded(loaded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEMO_DATA_LOADED] = loaded
        }
    }
}
