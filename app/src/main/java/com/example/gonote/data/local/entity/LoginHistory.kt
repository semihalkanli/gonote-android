package com.example.gonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login_history")
data class LoginHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val email: String,
    val loginAt: Long = System.currentTimeMillis(),
    val ipAddress: String,
    val deviceInfo: String
)



