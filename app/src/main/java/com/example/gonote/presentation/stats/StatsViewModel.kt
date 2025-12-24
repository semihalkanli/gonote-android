package com.example.gonote.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonote.data.model.Note
import com.example.gonote.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsState(
    val totalNotes: Int = 0,
    val favoriteNotes: Int = 0,
    val totalPhotos: Int = 0,
    val citiesMap: Map<String, Int> = emptyMap(),
    val topCity: String = "",
    val topCityCount: Int = 0,
    val isLoading: Boolean = true
)

class StatsViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow(StatsState())
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()

    fun loadStats(userId: String) {
        viewModelScope.launch {
            _statsState.value = _statsState.value.copy(isLoading = true)

            repository.getAllNotes(userId).collect { notes ->
                calculateStats(notes)
            }
        }
    }

    private fun calculateStats(notes: List<Note>) {
        val totalNotes = notes.size
        val favoriteNotes = notes.count { it.isFavorite }
        val totalPhotos = notes.sumOf { it.photos.size }

        // Group notes by city
        val citiesMap = notes
            .filter { it.city.isNotBlank() && it.city != "Unknown City" }
            .groupBy { it.city }
            .mapValues { it.value.size }

        val topCity = citiesMap.maxByOrNull { it.value }

        _statsState.value = StatsState(
            totalNotes = totalNotes,
            favoriteNotes = favoriteNotes,
            totalPhotos = totalPhotos,
            citiesMap = citiesMap,
            topCity = topCity?.key ?: "No data",
            topCityCount = topCity?.value ?: 0,
            isLoading = false
        )
    }
}
