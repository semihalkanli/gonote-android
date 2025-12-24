package com.example.gonote.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gonote.GoNoteApplication
import com.example.gonote.MainActivity
import com.example.gonote.R
import com.example.gonote.data.local.UserPreferences
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class MemoryNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val application = context.applicationContext as GoNoteApplication
            val userPreferences = UserPreferences(context)
            val userId = userPreferences.userId.first()

            if (userId == null) {
                return Result.success()
            }

            // Get all notes from the user
            val notes = application.repository.getAllNotes(userId).first()

            // Check for memories from exactly 1 year ago
            val calendar = Calendar.getInstance()
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            val memoriesFromLastYear = notes.filter { note ->
                val noteCalendar = Calendar.getInstance().apply {
                    timeInMillis = note.timestamp
                }
                val noteDayOfYear = noteCalendar.get(Calendar.DAY_OF_YEAR)
                val noteYear = noteCalendar.get(Calendar.YEAR)

                // Same day but different year (1+ years ago)
                noteDayOfYear == currentDayOfYear && noteYear < currentYear
            }

            if (memoriesFromLastYear.isNotEmpty()) {
                val oldestMemory = memoriesFromLastYear.maxByOrNull {
                    currentYear - Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.YEAR)
                }

                oldestMemory?.let { note ->
                    val yearCalendar = Calendar.getInstance().apply {
                        timeInMillis = note.timestamp
                    }
                    val yearsAgo = currentYear - yearCalendar.get(Calendar.YEAR)

                    sendMemoryNotification(
                        title = if (yearsAgo == 1) "1 yıl önce bugün" else "$yearsAgo yıl önce bugün",
                        message = "${note.city} konumunda \"${note.title}\" notunu oluşturmuştun",
                        noteId = note.id
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendMemoryNotification(title: String, message: String, noteId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "memory_notifications"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Memory Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for past memories"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("noteId", noteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(noteId.toInt(), notification)
    }
}
