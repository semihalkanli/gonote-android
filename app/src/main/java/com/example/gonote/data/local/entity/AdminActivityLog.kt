package com.example.gonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_activity_logs")
data class AdminActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val adminEmail: String,
    val actionType: String,  // "DELETE_USER", "EDIT_NOTE", "BAN_USER", "UNBAN_USER", "SEND_NOTIFICATION", etc.
    val targetId: String,    // user_id veya note_id
    val targetType: String,  // "USER" veya "NOTE"
    val details: String,     // Ek bilgi (JSON string veya plain text)
    val timestamp: Long = System.currentTimeMillis()
)



