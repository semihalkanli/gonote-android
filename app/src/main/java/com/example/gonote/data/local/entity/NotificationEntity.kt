package com.example.gonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String?,  // null = tüm kullanıcılar için broadcast
    val title: String,
    val message: String,
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)



