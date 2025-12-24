package com.example.gonote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gonote.data.local.dao.AdminActivityLogDao
import com.example.gonote.data.local.dao.LoginHistoryDao
import com.example.gonote.data.local.dao.NoteDao
import com.example.gonote.data.local.dao.NotificationDao
import com.example.gonote.data.local.dao.PhotoDao
import com.example.gonote.data.local.dao.UserDao
import com.example.gonote.data.local.entity.AdminActivityLog
import com.example.gonote.data.local.entity.LoginHistory
import com.example.gonote.data.local.entity.NoteEntity
import com.example.gonote.data.local.entity.NotificationEntity
import com.example.gonote.data.local.entity.PhotoEntity
import com.example.gonote.data.local.entity.UserEntity

@Database(
    entities = [
        NoteEntity::class,
        PhotoEntity::class,
        UserEntity::class,
        AdminActivityLog::class,
        LoginHistory::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GoNoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun photoDao(): PhotoDao
    abstract fun userDao(): UserDao
    abstract fun adminActivityLogDao(): AdminActivityLogDao
    abstract fun loginHistoryDao(): LoginHistoryDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: GoNoteDatabase? = null

        // Migration from version 1 to 2: Add category column
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN category TEXT NOT NULL DEFAULT 'Personal'")
            }
        }

        // Migration from version 2 to 3: Add users table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT NOT NULL PRIMARY KEY,
                        email TEXT NOT NULL,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // Migration from version 3 to 4: Add admin features
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to users table
                database.execSQL("ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                database.execSQL("ALTER TABLE users ADD COLUMN lastLoginAt INTEGER")
                database.execSQL("ALTER TABLE users ADD COLUMN lastLoginIp TEXT")
                
                // Create admin_activity_logs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS admin_activity_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        adminEmail TEXT NOT NULL,
                        actionType TEXT NOT NULL,
                        targetId TEXT NOT NULL,
                        targetType TEXT NOT NULL,
                        details TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create login_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS login_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        email TEXT NOT NULL,
                        loginAt INTEGER NOT NULL,
                        ipAddress TEXT NOT NULL,
                        deviceInfo TEXT NOT NULL
                    )
                """.trimIndent())
                
                // Create notifications table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        sentAt INTEGER NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): GoNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoNoteDatabase::class.java,
                    "gonote_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .setJournalMode(JournalMode.TRUNCATE) // Better for Android
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
