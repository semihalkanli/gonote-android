package com.example.gonote.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonote.data.model.Note
import com.example.gonote.data.repository.NoteRepository
import com.example.gonote.util.NetworkUtil
import com.example.gonote.util.NetworkMonitor
import com.example.gonote.util.LocationMonitor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SortOption {
    DATE_NEWEST,
    DATE_OLDEST,
    TITLE_AZ,
    TITLE_ZA,
    FAVORITES_FIRST
}

enum class DateFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR
}

data class MapState(
    val allNotes: List<Note> = emptyList(),
    val notes: List<Note> = emptyList(),
    val currentLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLocationPermission: Boolean = false,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val sortOption: SortOption = SortOption.DATE_NEWEST,
    val dateFilter: DateFilter = DateFilter.ALL,
    val isNetworkAvailable: Boolean = false,
    val networkCheckCompleted: Boolean = false,
    val showNetworkLostSnackbar: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val gpsCheckCompleted: Boolean = false,
    val showGpsDisabledSnackbar: Boolean = false
)

class MapViewModel(
    private val repository: NoteRepository,
    private val context: Context
) : ViewModel() {

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val networkMonitor = NetworkMonitor(context)
    private val locationMonitor = LocationMonitor(context)
    
    init {
        // Monitor network changes in real-time
        viewModelScope.launch {
            networkMonitor.networkStatus.collect { isAvailable ->
                val wasAvailable = _mapState.value.isNetworkAvailable
                
                _mapState.value = _mapState.value.copy(
                    isNetworkAvailable = isAvailable,
                    // Show snackbar only when network is lost (not on first check)
                    showNetworkLostSnackbar = _mapState.value.networkCheckCompleted && wasAvailable && !isAvailable
                )
            }
        }
        
        // Monitor GPS/Location changes in real-time
        viewModelScope.launch {
            locationMonitor.locationStatus.collect { isEnabled ->
                val wasEnabled = _mapState.value.isGpsEnabled
                val checkCompleted = _mapState.value.gpsCheckCompleted
                
                // Update GPS state
                _mapState.value = _mapState.value.copy(
                    isGpsEnabled = isEnabled,
                    // Show snackbar only when GPS is disabled after initial check
                    showGpsDisabledSnackbar = checkCompleted && wasEnabled && !isEnabled
                )
                
                // If GPS was just enabled, get current location
                if (checkCompleted && !wasEnabled && isEnabled && _mapState.value.hasLocationPermission) {
                    getCurrentLocation()
                }
            }
        }
    }

    fun loadNotes(userId: String) {
        viewModelScope.launch {
            repository.getAllNotes(userId).collect { notes ->
                _mapState.value = _mapState.value.copy(allNotes = notes)
                applyFiltersAndSort()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _mapState.value = _mapState.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }

    fun toggleFavoritesFilter() {
        _mapState.value = _mapState.value.copy(
            showFavoritesOnly = !_mapState.value.showFavoritesOnly
        )
        applyFiltersAndSort()
    }

    fun updateSortOption(option: SortOption) {
        _mapState.value = _mapState.value.copy(sortOption = option)
        applyFiltersAndSort()
    }

    fun updateDateFilter(filter: DateFilter) {
        _mapState.value = _mapState.value.copy(dateFilter = filter)
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val state = _mapState.value
        var filtered = state.allNotes

        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { note ->
                note.title.contains(state.searchQuery, ignoreCase = true) ||
                note.content.contains(state.searchQuery, ignoreCase = true) ||
                note.city.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // Apply date filter
        if (state.dateFilter != DateFilter.ALL) {
            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()

            filtered = filtered.filter { note ->
                val noteTime = note.timestamp
                calendar.timeInMillis = now

                when (state.dateFilter) {
                    DateFilter.TODAY -> {
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        noteTime >= calendar.timeInMillis
                    }
                    DateFilter.THIS_WEEK -> {
                        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        noteTime >= calendar.timeInMillis
                    }
                    DateFilter.THIS_MONTH -> {
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        noteTime >= calendar.timeInMillis
                    }
                    DateFilter.THIS_YEAR -> {
                        calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        noteTime >= calendar.timeInMillis
                    }
                    else -> true
                }
            }
        }

        // Apply favorites filter
        if (state.showFavoritesOnly) {
            filtered = filtered.filter { it.isFavorite }
        }

        // Apply sorting
        val sorted = when (state.sortOption) {
            SortOption.DATE_NEWEST -> filtered.sortedByDescending { it.id }
            SortOption.DATE_OLDEST -> filtered.sortedBy { it.id }
            SortOption.TITLE_AZ -> filtered.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> filtered.sortedByDescending { it.title.lowercase() }
            SortOption.FAVORITES_FIRST -> filtered.sortedByDescending { it.isFavorite }
        }

        _mapState.value = _mapState.value.copy(notes = sorted)
    }

    fun checkLocationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _mapState.value = _mapState.value.copy(hasLocationPermission = hasPermission)

        if (hasPermission) {
            getCurrentLocation()
        }
    }

    fun onPermissionGranted() {
        _mapState.value = _mapState.value.copy(hasLocationPermission = true)
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                _mapState.value = _mapState.value.copy(isLoading = true)

                // Try to get current location first
                val cancellationTokenSource = CancellationTokenSource()
                var location: Location? = null

                try {
                    location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).await()
                } catch (e: Exception) {
                    // If current location fails, try last known location
                    try {
                        location = fusedLocationClient.lastLocation.await()
                    } catch (lastLocException: Exception) {
                        // Ignore and use default location
                    }
                }

                location?.let {
                    _mapState.value = _mapState.value.copy(
                        currentLocation = LatLng(it.latitude, it.longitude),
                        isLoading = false
                    )
                } ?: run {
                    // Use default location (Istanbul) if no location available
                    _mapState.value = _mapState.value.copy(
                        currentLocation = LatLng(39.1425, 34.1709),
                        isLoading = false
                    )
                }
            } catch (e: SecurityException) {
                _mapState.value = _mapState.value.copy(
                    currentLocation = LatLng(39.1425, 34.1709),
                    isLoading = false,
                    error = "Location permission denied"
                )
            } catch (e: Exception) {
                _mapState.value = _mapState.value.copy(
                    currentLocation = LatLng(39.1425, 34.1709),
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun toggleFavorite(noteId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(noteId, isFavorite)
            } catch (e: Exception) {
                _mapState.value = _mapState.value.copy(
                    error = "Failed to update favorite: ${e.message}"
                )
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                _mapState.value = _mapState.value.copy(
                    error = "Failed to delete note: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _mapState.value = _mapState.value.copy(error = null)
    }

    fun checkNetworkConnection() {
        val isAvailable = NetworkUtil.isNetworkAvailable(context)
        _mapState.value = _mapState.value.copy(
            isNetworkAvailable = isAvailable,
            networkCheckCompleted = true
        )
    }
    
    fun dismissNetworkSnackbar() {
        _mapState.value = _mapState.value.copy(showNetworkLostSnackbar = false)
    }
    
    fun checkGpsStatus() {
        val isEnabled = locationMonitor.isLocationEnabled()
        _mapState.value = _mapState.value.copy(
            isGpsEnabled = isEnabled,
            gpsCheckCompleted = true
        )
    }
    
    fun dismissGpsSnackbar() {
        _mapState.value = _mapState.value.copy(showGpsDisabledSnackbar = false)
    }
}
