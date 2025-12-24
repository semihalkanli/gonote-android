package com.example.gonote.data.local

import androidx.room.TypeConverter
import com.example.gonote.data.local.entity.UserStatus

class Converters {
    @TypeConverter
    fun fromUserStatus(status: UserStatus): String {
        return status.name
    }

    @TypeConverter
    fun toUserStatus(status: String): UserStatus {
        return try {
            UserStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            UserStatus.ACTIVE // Default fallback
        }
    }
}



