package com.example.gonote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.gonote.data.local.UserPreferences
import com.example.gonote.presentation.navigation.NavGraph
import com.example.gonote.presentation.navigation.Screen
import com.example.gonote.ui.theme.GoNoteTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferences = UserPreferences(this)

        // Read login state synchronously to avoid flicker
        val isLoggedIn = runBlocking {
            userPreferences.isLoggedIn.first()
        }

        setContent {
            val navController = rememberNavController()
            val isDarkMode by userPreferences.isDarkMode.collectAsState(initial = false)
            val autoDarkMode by userPreferences.autoDarkMode.collectAsState(initial = false)
            val systemDarkMode = androidx.compose.foundation.isSystemInDarkTheme()

            val startDestination = if (isLoggedIn) {
                Screen.Home.route
            } else {
                Screen.Login.route
            }

            // Use system dark mode if auto is enabled, otherwise use manual setting
            val effectiveDarkMode = if (autoDarkMode) systemDarkMode else isDarkMode

            GoNoteTheme(darkTheme = effectiveDarkMode) {
                NavGraph(
                    navController = navController,
                    userPreferences = userPreferences,
                    startDestination = startDestination
                )
            }
        }
    }
}