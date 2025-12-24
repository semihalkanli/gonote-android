package com.example.gonote.presentation.map

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.ui.layout.ContentScale
import com.example.gonote.data.weather.WeatherService
import com.example.gonote.data.weather.getWeatherEmoji
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle
import coil.compose.AsyncImage
import java.io.File
import com.example.gonote.BuildConfig
import com.example.gonote.data.model.Note
import com.example.gonote.ui.theme.AccentBlue
import com.example.gonote.ui.theme.FavoriteRed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapState: MapState,
    userId: String,
    onAddNoteClick: (LatLng) -> Unit,
    onMarkerClick: (Note) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onPermissionGranted: () -> Unit,
    onErrorDismiss: () -> Unit,
    onStatsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onToggleFavoritesFilter: () -> Unit = {},
    onSortOptionChange: (SortOption) -> Unit = {},
    onDateFilterChange: (DateFilter) -> Unit = {},
    onDismissNetworkSnackbar: () -> Unit = {},
    onDismissGpsSnackbar: () -> Unit = {}
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var networkDialogShown by rememberSaveable { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }
    var gpsDialogShown by rememberSaveable { mutableStateOf(false) }

    var weatherTemp by remember { mutableStateOf<String?>(null) }
    var weatherEmoji by remember { mutableStateOf("🌤️") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch weather when location is available and network is available
    LaunchedEffect(mapState.currentLocation, mapState.isNetworkAvailable) {
        if (mapState.isNetworkAvailable) {
            mapState.currentLocation?.let { location ->
                scope.launch {
                    try {
                        val weather = WeatherService.api.getWeather(
                            lat = location.latitude,
                            lon = location.longitude,
                            apiKey = BuildConfig.WEATHER_API_KEY
                        )
                        weatherTemp = "${weather.main.temp.toInt()}°"
                        weatherEmoji = if (weather.weather.isNotEmpty()) {
                            getWeatherEmoji(weather.weather[0].main)
                        } else "🌤️"
                    } catch (e: Exception) {
                        // Hava durumu çekilemezse varsayılan değerler
                        weatherTemp = null
                        weatherEmoji = "🌤️"
                    }
                }
            }
        } else {
            // No network, clear weather data
            weatherTemp = null
        }
    }

    // Check permission on first launch
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            onPermissionGranted()
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Monitor permission changes
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            onPermissionGranted()
        }
    }

    // Show network dialog if no connection after check completes (only once per session)
    LaunchedEffect(mapState.networkCheckCompleted) {
        if (mapState.networkCheckCompleted && !mapState.isNetworkAvailable && !networkDialogShown) {
            showNetworkDialog = true
            networkDialogShown = true
        }
    }
    
    // Show GPS dialog if disabled after check completes (only once per session)
    LaunchedEffect(mapState.gpsCheckCompleted) {
        if (mapState.gpsCheckCompleted && !mapState.isGpsEnabled && !gpsDialogShown) {
            showGpsDialog = true
            gpsDialogShown = true
        }
    }
    
    // Show snackbar when network is lost while using the app
    LaunchedEffect(mapState.showNetworkLostSnackbar) {
        if (mapState.showNetworkLostSnackbar) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Internet connection lost",
                    duration = SnackbarDuration.Short
                )
                onDismissNetworkSnackbar()
            }
        }
    }
    
    // Show snackbar when GPS is disabled while using the app
    LaunchedEffect(mapState.showGpsDisabledSnackbar) {
        if (mapState.showGpsDisabledSnackbar) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Location services disabled",
                    duration = SnackbarDuration.Short
                )
                onDismissGpsSnackbar()
            }
        }
    }

    // Show network dialog
    if (showNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkDialog = false },
            title = { Text("No Internet Connection") },
            text = { 
                Text("No internet connection available. Weather and other online features will be unavailable. The app will continue to work in offline mode.")
            },
            confirmButton = {
                Button(
                    onClick = { showNetworkDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Show GPS dialog
    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text("Location Services Disabled") },
            text = { 
                Text("Your location services are disabled. To see your location on the map and save notes at the correct location, please enable location services.")
            },
            confirmButton = {
                Button(
                    onClick = { showGpsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Show error dialog
    if (mapState.error != null) {
        AlertDialog(
            onDismissRequest = onErrorDismiss,
            title = { Text("Error") },
            text = { Text(mapState.error) },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text("OK", color = AccentBlue)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Default location (Kırşehir Ahi Evran Üniversitesi) if no current location
        val defaultLocation = LatLng(39.1425, 34.1709)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                mapState.currentLocation ?: defaultLocation,
                15f
            )
        }

        // Update camera when location changes
        LaunchedEffect(mapState.currentLocation) {
            mapState.currentLocation?.let {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = mapState.hasLocationPermission,
                mapStyleOptions = if (MaterialTheme.colorScheme.background == Color(0xFF000000) ||
                                     MaterialTheme.colorScheme.background == Color(0xFF121212)) {
                    com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
                        context as android.content.Context,
                        com.example.gonote.R.raw.map_style_dark
                    )
                } else null
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                zoomGesturesEnabled = true
            ),
            onMapLongClick = { latLng ->
                // Long press on map to add note at that location
                onAddNoteClick(latLng)
            }
        ) {
            // Add RED markers for all notes with title labels
            mapState.notes.forEach { note ->
                val position = LatLng(note.latitude, note.longitude)

                Marker(
                    state = MarkerState(position = position),
                    title = note.title,  // Shows as balloon/info window above marker
                    snippet = null,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    onClick = {
                        selectedNote = note
                        true  // Show info window (balloon)
                    }
                )
            }
        }

        // My Location button
        if (mapState.hasLocationPermission) {
            FloatingActionButton(
                onClick = {
                    mapState.currentLocation?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(56.dp),
                containerColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Weather widget - transparent background, lower position (only show if network available)
        if (mapState.isNetworkAvailable) {
            weatherTemp?.let { temp ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 140.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = weatherEmoji, fontSize = 20.sp)
                        Text(
                            text = temp,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = LocalTextStyle.current.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }
            }
        }

        // Top bar with Stats and Settings buttons (original style)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Statistics button
            FloatingActionButton(
                onClick = onStatsClick,
                modifier = Modifier.size(56.dp),
                containerColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Statistics",
                    tint = AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Settings button
            FloatingActionButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(56.dp),
                containerColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Add Note button - BLUE
        FloatingActionButton(
            onClick = {
                val location = mapState.currentLocation ?: defaultLocation
                onAddNoteClick(location)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp),
            containerColor = AccentBlue,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // Loading indicator
        if (mapState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                color = AccentBlue
            )
        }
        
        // Snackbar host for status notifications (styled to match app theme)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                actionColor = AccentBlue,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    // Dialog for selected note (centered on screen)
    selectedNote?.let { selected ->
        // Find the current version of the note from mapState (for live updates)
        val currentNote = mapState.notes.find { it.id == selected.id } ?: selected

        NotePreviewDialog(
            note = currentNote,
            onDismiss = { selectedNote = null },
            onToggleFavorite = { onToggleFavorite(currentNote.id, !currentNote.isFavorite) },
            onEdit = {
                onMarkerClick(currentNote)
                selectedNote = null
            },
            onDelete = {
                onDeleteNote(currentNote)
                selectedNote = null
            }
        )
    }
}

@Composable
fun NotePreviewDialog(
    note: Note,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var offsetY by remember { mutableStateOf(0f) }
    val threshold = 200f
    val animatedOffsetY by animateDpAsState(
        targetValue = offsetY.dp,
        label = "swipe offset"
    )
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }

    // Custom dialog with Box overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        onDismiss()
                    },
                    onVerticalDrag = { _, _ -> }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .offset(y = animatedOffsetY)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY < -threshold) {
                                onDelete()
                            } else {
                                offsetY = 0f
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            // Only allow upward swipes (negative values)
                            val newOffset = offsetY + dragAmount
                            if (newOffset <= 0) {
                                offsetY = newOffset
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 450.dp, max = 600.dp)
                    .padding(20.dp)
            ) {
                // Title and Favorite button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = note.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            onToggleFavorite()
                        }
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (note.isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        if (note.locationName.isNotBlank()) {
                            Text(
                                text = note.locationName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (note.city.isNotBlank() && note.country.isNotBlank()) {
                            Text(
                                text = if (note.city != "Unknown City" && note.country != "Unknown") {
                                    "${note.city}, ${note.country}"
                                } else {
                                    String.format("%.4f, %.4f", note.latitude, note.longitude)
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = String.format("%.4f, %.4f", note.latitude, note.longitude),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photo Gallery (Horizontal scrollable) - Always visible
                PhotoGalleryPreview(
                    photos = note.photos,
                    onPhotoClick = { photoIndex ->
                        selectedPhotoIndex = photoIndex
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No description",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete hint
                Text(
                    text = "Swipe up to delete",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Edit button
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Note",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Share button
                OutlinedButton(
                    onClick = {
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                buildString {
                                    append("📍 ${note.title}\n\n")
                                    append("${note.content}\n\n")
                                    append("📌 Location: ${note.locationName}\n")
                                    append("🌍 ${note.city}, ${note.country}\n")
                                    append("🗓️ ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(note.timestamp)}")
                                }
                            )
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(shareIntent, "Share Note")
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Note",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }

    // Full-screen photo viewer
    selectedPhotoIndex?.let { index ->
        FullScreenPhotoViewer(
            photos = note.photos,
            initialIndex = index,
            onDismiss = { selectedPhotoIndex = null }
        )
    }
}

@Composable
fun PhotoGalleryPreview(
    photos: List<String>,
    onAddPhoto: (() -> Unit)? = null,
    onPhotoClick: ((Int) -> Unit)? = null
) {
    if (photos.isEmpty() && onAddPhoto == null) {
        // Show placeholder when no photos and can't add
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "No Photos",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No photos",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            items(photos.take(5).size) { index ->
                val photoPath = photos[index]
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = "Photo",
                    modifier = Modifier
                        .width(140.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (onPhotoClick != null) {
                                Modifier.clickable { onPhotoClick(index) }
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // Add photo button
            if (onAddPhoto != null && photos.size < 5) {
                item {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onAddPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Photo",
                                tint = AccentBlue,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add Photo",
                                fontSize = 12.sp,
                                color = AccentBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenPhotoViewer(
    photos: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )

    // Gesture states
    var dragOffsetY by remember { mutableStateOf(0f) }
    var isZoomed by remember { mutableStateOf(false) }
    var isDraggingVertically by remember { mutableStateOf(false) }
    val dismissThreshold = 200f

    // Smooth spring animation for offset
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offsetY"
    )

    // Smooth alpha animation for background (only fade on downward swipe)
    val backgroundAlpha by animateFloatAsState(
        targetValue = 1f - (dragOffsetY.coerceAtLeast(0f) / dismissThreshold).coerceIn(0f, 1f),
        animationSpec = tween(100),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .pointerInput(isZoomed) {
                if (!isZoomed) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalDragX = 0f
                        var totalDragY = 0f
                        var gestureDecided = false
                        isDraggingVertically = false
                        
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            
                            if (!gestureDecided && change.pressed) {
                                val dragX = change.position.x - change.previousPosition.x
                                val dragY = change.position.y - change.previousPosition.y
                                totalDragX += dragX
                                totalDragY += dragY
                                
                                // Decide gesture direction after some movement
                                if (abs(totalDragX) > 20f || abs(totalDragY) > 20f) {
                                    gestureDecided = true
                                    // If vertical movement is dominant and downward, handle dismiss
                                    isDraggingVertically = abs(totalDragY) > abs(totalDragX) && totalDragY > 0
                                }
                            }
                            
                            if (isDraggingVertically && change.pressed) {
                                val dragY = change.position.y - change.previousPosition.y
                                val newOffset = dragOffsetY + dragY
                                if (newOffset >= 0) {
                                    dragOffsetY = newOffset
                                }
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })
                        
                        // On release
                        if (isDraggingVertically) {
                            if (dragOffsetY > dismissThreshold) {
                                onDismiss()
                            } else {
                                dragOffsetY = 0f
                            }
                        }
                        isDraggingVertically = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // HorizontalPager for left/right navigation
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed && !isDraggingVertically,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = animatedOffsetY
                }
        ) { page ->
            ZoomablePhotoPage(
                photoPath = photos[page],
                onZoomChange = { zoomed ->
                    isZoomed = zoomed
                }
            )
        }

        // Page indicator (when multiple photos)
        if (photos.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photos.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ZoomablePhotoPage(
    photoPath: String,
    onZoomChange: (Boolean) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Notify zoom state changes (with threshold to avoid flickering)
    LaunchedEffect(scale) {
        onZoomChange(scale > 1.1f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Wait for first finger
                    awaitFirstDown(requireUnconsumed = false)
                    
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }
                        
                        // Only handle zoom when 2+ fingers are touching
                        if (pointerCount >= 2) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            
                            val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                            scale = newScale
                            
                            if (newScale > 1f) {
                                offsetX += panChange.x
                                offsetY += panChange.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                            
                            // Consume changes to prevent interference
                            event.changes.forEach { it.consume() }
                        } else if (pointerCount == 1 && scale > 1f) {
                            // Allow panning when zoomed in with single finger
                            val change = event.changes.first()
                            val panX = change.position.x - change.previousPosition.x
                            val panY = change.position.y - change.previousPosition.y
                            offsetX += panX
                            offsetY += panY
                            change.consume()
                        }
                        // Single finger without zoom: don't consume, let HorizontalPager handle
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(photoPath),
            contentDescription = "Photo",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
            contentScale = ContentScale.Fit
        )

        // Reset zoom button with animation
        AnimatedVisibility(
            visible = scale > 1f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                containerColor = Color.White.copy(alpha = 0.9f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Reset Zoom",
                    tint = Color.Black
                )
            }
        }
    }
}
