package com.example.gonote

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gonote.data.DemoDataManager
import com.example.gonote.data.local.GoNoteDatabase
import com.example.gonote.data.local.UserPreferences
import com.example.gonote.data.repository.NoteRepository
import com.example.gonote.notifications.MemoryNotificationWorker
import com.example.gonote.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GoNoteApplication : Application() {
    val database by lazy { GoNoteDatabase.getDatabase(this) }
    val repository by lazy {
        NoteRepository(
            database.noteDao(),
            database.photoDao(),
            database.userDao(),
            database.adminActivityLogDao(),
            database.loginHistoryDao(),
            database.notificationDao()
        )
    }
    private val userPreferences by lazy { UserPreferences(this) }
    private val demoDataManager by lazy {
        DemoDataManager(this, repository, userPreferences)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        scheduleMemoryNotifications()
        loadDemoData()
    }

    private fun loadDemoData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                demoDataManager.loadDemoDataIfNeeded()
            } catch (e: Exception) {
                Log.e("GoNoteApp", "Failed to load demo data", e)
                // Fail silently, don't crash the app
            }
        }
    }

    private fun scheduleMemoryNotifications() {
        val memoryWorkRequest = PeriodicWorkRequestBuilder<MemoryNotificationWorker>(
            1, TimeUnit.DAYS // Check daily
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "memory_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            memoryWorkRequest
        )
    }
}
