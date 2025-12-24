package com.example.gonote.presentation.note

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonote.data.model.Note
import com.example.gonote.data.repository.NoteRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

data class NoteState(
    val title: String = "",
    val content: String = "",
    val isFavorite: Boolean = false,
    val photos: List<String> = emptyList(),
    val location: LatLng? = null,
    val locationName: String = "",
    val city: String = "",
    val country: String = "",
    val category: String = "Personal",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class NoteViewModel(
    private val repository: NoteRepository,
    private val context: Context
) : ViewModel() {

    private val _noteState = MutableStateFlow(NoteState())
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            _noteState.value = _noteState.value.copy(isLoading = true)
            try {
                val note = repository.getNoteById(noteId)
                note?.let {
                    _noteState.value = NoteState(
                        title = it.title,
                        content = it.content,
                        isFavorite = it.isFavorite,
                        photos = it.photos,
                        location = LatLng(it.latitude, it.longitude),
                        locationName = it.locationName,
                        city = it.city,
                        country = it.country,
                        category = it.category,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _noteState.value = _noteState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load note"
                )
            }
        }
    }

    fun setLocation(location: LatLng, customPlaceName: String? = null) {
        // Set location and start geocoding
        _noteState.value = _noteState.value.copy(
            location = location,
            locationName = customPlaceName ?: "",
            city = "",
            country = ""
        )
        // Sadece custom place name yoksa geocoding yap
        if (customPlaceName == null) {
            getAddressFromLocation(location)
        } else {
            // Custom place name varsa, sadece şehir ve ülke bilgisini al
            getAddressDetailsFromLocation(location)
        }
    }

    private fun getAddressFromLocation(location: LatLng) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    // API 33+ - Use new callback-based API
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            // featureName en spesifik bilgi (işletme, bina, POI adı)
                            val placeName = address.featureName
                                ?: address.thoroughfare  // sokak adı
                                ?: address.locality      // şehir adı
                                ?: "My Location"
                            _noteState.value = _noteState.value.copy(
                                locationName = placeName,
                                city = address.locality ?: address.subAdminArea ?: "Unknown City",
                                country = address.countryName ?: "Unknown"
                            )
                        } else {
                            setFallbackLocation(location)
                        }
                    }
                } else {
                    // API < 33 - Use deprecated synchronous API
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        // featureName en spesifik bilgi (işletme, bina, POI adı)
                        val placeName = address.featureName
                            ?: address.thoroughfare  // sokak adı
                            ?: address.locality      // şehir adı
                            ?: "My Location"
                        _noteState.value = _noteState.value.copy(
                            locationName = placeName,
                            city = address.locality ?: address.subAdminArea ?: "Unknown City",
                            country = address.countryName ?: "Unknown"
                        )
                    } else {
                        setFallbackLocation(location)
                    }
                }
            } catch (e: Exception) {
                // Geocoding failed, use coordinates as fallback
                setFallbackLocation(location)
            }
        }
    }

    private fun getAddressDetailsFromLocation(location: LatLng) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            _noteState.value = _noteState.value.copy(
                                city = address.locality ?: address.subAdminArea ?: "Unknown City",
                                country = address.countryName ?: "Unknown"
                            )
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        _noteState.value = _noteState.value.copy(
                            city = address.locality ?: address.subAdminArea ?: "Unknown City",
                            country = address.countryName ?: "Unknown"
                        )
                    }
                }
            } catch (e: Exception) {
                // Geocoding failed, keep existing locationName
            }
        }
    }

    private fun setFallbackLocation(location: LatLng) {
        _noteState.value = _noteState.value.copy(
            locationName = "My Location",
            city = String.format("%.4f, %.4f", location.latitude, location.longitude),
            country = ""
        )
    }

    fun updateTitle(title: String) {
        _noteState.value = _noteState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _noteState.value = _noteState.value.copy(content = content)
    }

    fun toggleFavorite() {
        _noteState.value = _noteState.value.copy(isFavorite = !_noteState.value.isFavorite)
    }

    fun updateCategory(category: String) {
        _noteState.value = _noteState.value.copy(category = category)
    }

    fun addPhoto(uri: Uri) {
        val currentPhotos = _noteState.value.photos
        if (currentPhotos.size >= 5) {
            _noteState.value = _noteState.value.copy(error = "Maximum 5 photos allowed")
            return
        }

        viewModelScope.launch {
            try {
                val photoPath = savePhotoToInternalStorage(uri)
                _noteState.value = _noteState.value.copy(
                    photos = currentPhotos + photoPath
                )
            } catch (e: Exception) {
                _noteState.value = _noteState.value.copy(
                    error = "Failed to add photo: ${e.message}"
                )
            }
        }
    }

    fun removePhoto(photoPath: String) {
        val currentPhotos = _noteState.value.photos
        _noteState.value = _noteState.value.copy(
            photos = currentPhotos.filter { it != photoPath }
        )

        // Delete physical file
        viewModelScope.launch {
            try {
                File(photoPath).delete()
            } catch (e: Exception) {
                // Ignore deletion errors
            }
        }
    }

    private fun savePhotoToInternalStorage(uri: Uri): String {
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }

        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val photoFile = File(photosDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            photoFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return photoFile.absolutePath
    }

    fun saveNote(userId: String, noteId: Long? = null) {
        viewModelScope.launch {
            _noteState.value = _noteState.value.copy(isLoading = true)

            val state = _noteState.value

            // Validation
            if (state.title.isBlank()) {
                _noteState.value = _noteState.value.copy(
                    isLoading = false,
                    error = "Title is required"
                )
                return@launch
            }

            if (state.location == null) {
                _noteState.value = _noteState.value.copy(
                    isLoading = false,
                    error = "Location is required"
                )
                return@launch
            }

            try {
                val note = Note(
                    id = noteId ?: 0,
                    title = state.title,
                    content = state.content,
                    latitude = state.location.latitude,
                    longitude = state.location.longitude,
                    locationName = state.locationName,
                    city = state.city,
                    country = state.country,
                    userId = userId,
                    isFavorite = state.isFavorite,
                    photos = state.photos,
                    category = state.category
                )

                if (noteId == null) {
                    repository.insertNote(note)
                } else {
                    repository.updateNote(note)
                }

                _noteState.value = _noteState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _noteState.value = _noteState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save note"
                )
            }
        }
    }

    fun clearError() {
        _noteState.value = _noteState.value.copy(error = null)
    }
}
