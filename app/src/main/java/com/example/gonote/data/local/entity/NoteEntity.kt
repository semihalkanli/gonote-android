package com.example.gonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val city: String,
    val country: String,
    val timestamp: Long,
    val userId: String,
    val isFavorite: Boolean = false,
    val category: String = "Personal" // Default category
)
