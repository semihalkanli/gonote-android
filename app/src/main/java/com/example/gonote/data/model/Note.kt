package com.example.gonote.data.model

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val city: String,
    val country: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val isFavorite: Boolean = false,
    val photos: List<String> = emptyList(),
    val category: String = "Personal"
)
