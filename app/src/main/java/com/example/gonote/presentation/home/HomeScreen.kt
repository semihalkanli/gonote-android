package com.example.gonote.presentation.home

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gonote.GoNoteApplication
import com.example.gonote.data.local.UserPreferences
import com.example.gonote.presentation.map.MapScreen
import com.example.gonote.presentation.map.MapViewModel
import com.example.gonote.presentation.map.MapViewModelFactory
import com.example.gonote.presentation.map.SortOption
import com.example.gonote.presentation.map.DateFilter
import com.google.android.gms.maps.model.LatLng

@Composable
fun HomeScreen(
    userPreferences: UserPreferences,
    onAddNoteClick: (LatLng) -> Unit,
    onEditNoteClick: (Long) -> Unit,
    onStatsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as GoNoteApplication

    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(application.repository, context)
    )

    val mapState by mapViewModel.mapState.collectAsState()
    val userId by userPreferences.userId.collectAsState(initial = null)

    // Load notes when userId is available
    LaunchedEffect(userId) {
        userId?.let { id ->
            mapViewModel.loadNotes(id)
            mapViewModel.checkLocationPermission()
            mapViewModel.checkNetworkConnection()
            mapViewModel.checkGpsStatus()
        }
    }

    MapScreen(
        mapState = mapState,
        userId = userId ?: "",
        onAddNoteClick = onAddNoteClick,
        onMarkerClick = { note ->
            onEditNoteClick(note.id)
        },
        onToggleFavorite = { noteId, isFavorite ->
            mapViewModel.toggleFavorite(noteId, isFavorite)
        },
        onDeleteNote = { note ->
            mapViewModel.deleteNote(note)
        },
        onPermissionGranted = {
            mapViewModel.onPermissionGranted()
        },
        onErrorDismiss = {
            mapViewModel.clearError()
        },
        onStatsClick = onStatsClick,
        onSettingsClick = onSettingsClick,
        onSearchQueryChange = { query ->
            mapViewModel.updateSearchQuery(query)
        },
        onToggleFavoritesFilter = {
            mapViewModel.toggleFavoritesFilter()
        },
        onSortOptionChange = { option ->
            mapViewModel.updateSortOption(option)
        },
        onDateFilterChange = { filter ->
            mapViewModel.updateDateFilter(filter)
        },
        onDismissNetworkSnackbar = {
            mapViewModel.dismissNetworkSnackbar()
        },
        onDismissGpsSnackbar = {
            mapViewModel.dismissGpsSnackbar()
        }
    )
}
