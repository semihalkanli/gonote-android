package com.example.gonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserStatus {
    ACTIVE,   // Normal kullanıcı
    INACTIVE, // Geçici olarak pasif
    BANNED    // Engelli
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: UserStatus = UserStatus.ACTIVE,
    val lastLoginAt: Long? = null,
    val lastLoginIp: String? = null
)
