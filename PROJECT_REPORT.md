# GoNote - Location-Based Note Taking App
## Mobile Application Development Course Project

**Student:** Semih Alkanlı  
**Date:** December 2025  
**Platform:** Android  

---

## TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Technologies Used](#2-technologies-used)
3. [Application Features](#3-application-features)
4. [Technical Architecture](#4-technical-architecture)
5. [Setup Instructions](#5-setup-instructions)
6. [Demo Accounts](#6-demo-accounts)
7. [API Keys](#7-api-keys)
8. [Challenges and Solutions](#8-challenges-and-solutions)
9. [Project Statistics](#9-project-statistics)
10. [Conclusion](#10-conclusion)

---

## 1. PROJECT OVERVIEW

### 1.1 Project Description

GoNote is a location-based note-taking application developed for Android. Users can create notes at specific locations during their travels and memories, and view these notes on an interactive map. Each note can include up to 5 photos and be organized into categories.

### 1.2 Project Goals

The goal of this project was to apply modern Android development techniques learned in the Mobile Application Development course and gain real-world application development experience. Specifically:

- Modern UI development with Jetpack Compose
- Google Maps integration
- Database operations with Room
- Photo management
- User authentication system
- Real-time network and GPS monitoring

### 1.3 Key Features

- Add notes on Google Maps
- Attach up to 5 photos per note
- 6 categories (Travel, Food, Work, Personal, Shopping, Other)
- Search and filter notes
- Favorite notes
- City-based grouping
- Statistics dashboard
- Dark mode support
- Admin panel
- **Real-time WiFi/Internet monitoring**
- **Real-time GPS/Location monitoring**
- **Smart connectivity management**

---

## 2. TECHNOLOGIES USED

### 2.1 Programming Language and Framework

**Kotlin**
- Official Android language
- Modern and concise syntax compared to Java
- Null safety for more secure code

**Jetpack Compose**
- Android's modern UI framework
- Build UI with Kotlin code instead of XML
- Faster development cycle

### 2.2 Main Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose | 2024.12.01 | Modern UI creation |
| Room Database | 2.6.1 | Database operations |
| Google Maps Compose | 5.0.4 | Map display |
| Google Play Services Maps | - | Location services |
| Google Places API | 3.3.0 | Location search |
| Coil | 2.7.0 | Image loading |
| DataStore | 1.1.1 | User preferences |
| Navigation Compose | 2.8.5 | Screen navigation |
| WorkManager | 2.9.0 | Background tasks |
| Kotlin Coroutines | - | Asynchronous operations and Flow |
| Retrofit | 2.9.0 | Weather API integration |
| KSP | - | Annotation processing |

### 2.3 External Services

**Google Maps API**
- Map display
- Marker placement
- Location services
- GPS status monitoring with automatic location updates

**OpenWeatherMap API**
- Real-time weather information
- Temperature display
- Automatic management with internet connection check
- Disabled in offline mode

---

## 3. APPLICATION FEATURES

### 3.1 Authentication System

**Login**
- Email and password authentication
- Admin login support
- Demo user login
- Input validation
- Session management

**User Management**
- User registration tracking
- Login history with IP address and device info
- User status management (Active, Inactive, Banned)
- Last login tracking

### 3.2 Map-Based Notes

**Interactive Map**
- Google Maps integration
- Current location display
- Note markers on map
- Marker clustering
- Custom marker icons
- Real-time GPS monitoring

**Note Creation**
- Tap on map to add note
- Title and content
- Category selection
- Location name and address
- City and country automatic detection
- Up to 5 photos per note

**Note Management**
- View all notes
- Search by title or content
- Filter by category
- City-based grouping
- Sort by date
- Mark as favorite

### 3.3 Photo Management

**Multiple Sources**
- Take photo with camera
- Select from gallery
- Up to 5 photos per note

**Photo Features**
- Photo preview
- Full-screen view
- Swipeable photo gallery
- Photo deletion
- Automatic storage management

### 3.4 Categories

Six predefined categories:
-  Travel
-  Food
-  Work
-  Personal
-  Shopping
-  Other

### 3.5 Statistics Dashboard

**User Statistics**
- Total notes count
- Total photos count
- Total cities visited
- Notes by category (pie chart)
- Recent activity timeline
- Favorite notes count

### 3.6 Real-Time Monitoring

**Network Monitor**
- WiFi connection status
- Internet connectivity check
- Real-time status updates
- Visual indicators
- Automatic feature adaptation (weather API)

**Location Monitor**
- GPS enabled/disabled status
- Real-time location updates
- Location permission handling
- Visual GPS status indicator
- Automatic map updates

### 3.7 Admin Panel

**Dashboard**
- Total users count
- Total notes count
- Total photos count
- System statistics

**User Management**
- View all users
- User statistics per user
- Login history
- IP address tracking
- Device information
- User status management (Ban/Unban)

**Demo Data**
- Load demo users for testing
- Automatic demo data generation
- Reset functionality

### 3.8 User Interface

**Modern Design**
- Material Design 3
- Dark mode support
- Smooth animations
- Responsive layout
- Bottom navigation
- Floating action buttons
- Dialog boxes
- Snackbar notifications

---

## 4. TECHNICAL ARCHITECTURE

### 4.1 Architecture Pattern

**MVVM (Model-View-ViewModel)**
- Separation of concerns
- Reactive UI with StateFlow
- ViewModel for business logic
- Repository pattern for data layer
- Dependency injection (manual)

### 4.2 Database Schema

The app uses Room Database with 6 tables:

**1. notes**
- id (Primary Key)
- title, content
- latitude, longitude
- locationName, city, country
- timestamp
- userId (Foreign Key)
- isFavorite
- photos (JSON array)
- category

**2. users**
- id (Primary Key)
- email (Unique)
- createdAt
- status (Active, Inactive, Banned)
- lastLoginAt, lastLoginIp

**3. login_history**
- id (Primary Key)
- userId (Foreign Key)
- email
- loginTimestamp
- ipAddress
- deviceInfo

**4. user_activity**
- id (Primary Key)
- userId (Foreign Key)
- action
- timestamp
- details

**5. note_photos**
- id (Primary Key)
- noteId (Foreign Key)
- photoPath
- timestamp

**6. favorite_notes**
- id (Primary Key)
- noteId (Foreign Key)
- userId (Foreign Key)
- timestamp

### 4.3 Project Structure

```
com.example.gonote/
├── data/
│   ├── admin/             # Admin configuration
│   ├── local/             # Room Database & UserPreferences
│   ├── model/             # Data models
│   ├── repository/        # Repository implementation
│   ├── monitor/           # Network & Location monitoring
│   ├── weather/           # Weather API service
│   └── DemoDataManager.kt # Demo data management
├── presentation/
│   ├── admin/             # Admin panel screens
│   ├── auth/              # Login/Register screens
│   ├── home/              # Home screen
│   ├── list/              # Notes list screen
│   ├── map/               # Map screen
│   ├── note/              # Note detail & add screens
│   ├── profile/           # Profile & settings
│   └── statistics/        # Statistics screen
├── navigation/            # Navigation setup
├── ui/
│   ├── components/        # Reusable UI components
│   └── theme/             # App theme & colors
├── util/                  # Utility functions
├── GoNoteApplication.kt   # Application class
└── MainActivity.kt        # Main activity

Total: 62 Kotlin files
```

### 4.4 Key Implementation Details

**State Management**
```kotlin
// ViewModel with StateFlow
class MapViewModel : ViewModel() {
    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()
}

// Compose UI observes state
@Composable
fun MapScreen(viewModel: MapViewModel) {
    val mapState by viewModel.mapState.collectAsState()
    // UI updates automatically when state changes
}
```

**Real-Time Monitoring**
```kotlin
// Network Monitor with Flow
class NetworkMonitor(context: Context) {
    val isNetworkAvailable: Flow<Boolean> = callbackFlow {
        // Monitor network changes
        // Emit updates to Flow
    }
}

// GPS Monitor with Flow
class LocationMonitor(context: Context) {
    val isGpsEnabled: Flow<Boolean> = callbackFlow {
        // Monitor GPS status changes
        // Emit updates to Flow
    }
}
```

**Database Operations**
```kotlin
// Repository with Flow for reactive data
interface GoNoteRepository {
    fun getAllNotes(userId: String): Flow<List<Note>>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(noteId: String)
    // More operations...
}
```

---

## 5. SETUP INSTRUCTIONS

### 5.1 Prerequisites

To build and run this project, you need:

1. **Android Studio**
   - Version: Android Studio Ladybug (2024.2.1) or newer
   - Download: https://developer.android.com/studio

2. **JDK**
   - Version: JDK 17 or higher
   - Usually bundled with Android Studio

3. **Android SDK**
   - Minimum SDK: API 24 (Android 7.0)
   - Target SDK: API 35 (Android 15)
   - Compile SDK: API 35

### 5.2 Build Steps

1. **Extract the Project**
   ```
   Unzip the GoNote.zip file to your desired location
   ```

2. **Open in Android Studio**
   ```
   1. Launch Android Studio
   2. Click "Open"
   3. Navigate to the extracted GoNote folder
   4. Click "OK"
   ```

3. **Sync Project**
   ```
   Android Studio will automatically:
   - Create local.properties with your SDK path
   - Download Gradle wrapper (if needed)
   - Download dependencies (~5-10 minutes first time)
   - Index the project
   ```

4. **Build the Project**
   ```
   Build → Make Project (Ctrl+F9 / Cmd+F9)
   
   Or using command line:
   
   Windows:
   gradlew.bat assembleDebug
   
   Mac/Linux:
   ./gradlew assembleDebug
   ```

5. **Run on Device/Emulator**
   ```
   1. Connect an Android device (USB debugging enabled)
      OR
      Create an Android Virtual Device (AVD) in Android Studio
   
   2. Click "Run" button (Shift+F10 / Ctrl+R)
   
   3. Select your device/emulator
   
   4. App will install and launch automatically
   ```

### 5.3 Expected Build Output

- **Build Time:** First build takes 5-10 minutes (downloads dependencies)
- **APK Location:** `app/build/outputs/apk/debug/app-debug.apk`
- **APK Size:** ~60-70 MB (includes demo photos and resources)

### 5.4 Troubleshooting

**Issue: SDK not found**
- Solution: Android Studio automatically creates `local.properties` with your SDK path

**Issue: Build fails with "AAPT error"**
- Solution: File → Invalidate Caches / Restart

**Issue: Gradle sync fails**
- Solution: Check your internet connection, Gradle will download dependencies

**Issue: Maps not showing**
- Solution: Google Maps API key is already included in AndroidManifest.xml

---

## 6. DEMO ACCOUNTS

The application includes pre-configured demo accounts for testing:

### 6.1 Regular User Account

**Email:** `semihalkanli@gmail.com`  
**Password:** `beyonder`

**Features:**
- Full access to note-taking features
- Pre-loaded with demo notes and photos
- Can create, edit, delete notes
- Can view statistics
- Can access profile settings

### 6.2 Admin Account

**Email:** `admin@gonote.com`  
**Password:** `beyonder`

**Features:**
- Access to admin panel
- View all users
- View user statistics
- View login history
- User management (ban/unban users)
- Load demo data
- Cannot create notes (admin-only account)

### 6.3 Demo Data

The demo user account comes pre-loaded with:
- **16 notes** across different Turkish cities
- **48 photos** from famous landmarks
- **6 categories** represented
- **Realistic timestamps** (spread over 2-3 months)

Landmarks included:
- Ayasofya, Blue Mosque (Istanbul)
- Cappadocia
- Pamukkale
- Ephesus
- Bosphorus
- And more...

---

## 7. API KEYS

The application uses two API keys, both are already configured in the source code for easy testing.

### 7.1 Google Maps API Key

**Location:** `app/src/main/AndroidManifest.xml` (Line 32)

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyCWCZgqPDzlR6wgJAzeOPQshkRaIUADCq4" />
```

**Key:** `AIzaSyCWCZgqPDzlR6wgJAzeOPQshkRaIUADCq4`

**Purpose:**
- Display Google Maps
- Place markers
- Get location information
- Geocoding (address lookup)

**Status:** Active and working

### 7.2 OpenWeatherMap API Key

**Location:** `app/src/main/java/com/example/gonote/presentation/map/MapScreen.kt` (Line 108)

```kotlin
val API_KEY = "1d3822fcdee91429b1baa14631813f72"
```

**Key:** `1d3822fcdee91429b1baa14631813f72`

**Purpose:**
- Fetch real-time weather data
- Display temperature on map screen
- Show weather emoji/icon

**Status:** Active and working

**Note:** Weather API is automatically disabled when there's no internet connection, preventing unnecessary API calls.

---

## 8. CHALLENGES AND SOLUTIONS

### 8.1 Challenge: Demo Data Loading on First Launch

**Problem:**
- App needed to show meaningful data for demo purposes
- Loading large amounts of data caused initial lag
- Photos needed to be created from drawable resources

**Solution:**
```kotlin
// Check if demo data already loaded
val noteCount = repository.getTotalNotesCount(DEMO_USER_ID).first()
if (noteCount > 0) {
    return  // Skip loading
}

// Load demo data only once
createAndInsertDemoNotes(DEMO_USER_ID)
userPreferences.setDemoDataLoaded(true)
```

**Result:** Demo data loads only once, subsequent launches are instant.

### 8.2 Challenge: Real-Time Network Monitoring

**Problem:**
- Need to detect WiFi/Internet connectivity in real-time
- Weather API should only work when online
- UI should reflect connection status instantly

**Solution:**
```kotlin
class NetworkMonitor(context: Context) {
    val isNetworkAvailable: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(...)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
```

**Result:** Real-time network status updates with Flow, automatic UI updates.

### 8.3 Challenge: GPS Status Monitoring

**Problem:**
- Need to detect when user turns GPS on/off
- Map should reflect GPS status in real-time
- Prompt user to enable GPS if disabled

**Solution:**
```kotlin
class LocationMonitor(context: Context) {
    val isGpsEnabled: Flow<Boolean> = callbackFlow {
        val locationManager = context.getSystemService(...)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isEnabled = locationManager.isProviderEnabled(...)
                trySend(isEnabled)
            }
        }
        context.registerReceiver(receiver, IntentFilter(...))
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
```

**Result:** Instant GPS status detection, automatic location updates when enabled.

### 8.4 Challenge: Photo Management

**Problem:**
- Users can take photos or select from gallery
- Multiple photos per note (up to 5)
- Photos need to be stored persistently
- Memory management for photo loading

**Solution:**
- Used Coil library for efficient image loading
- Stored photos in app's internal storage
- Used JSON array to store photo paths in database
- Implemented swipeable gallery for photo viewing

**Result:** Smooth photo experience with efficient memory usage.

### 8.5 Challenge: Admin Panel Implementation

**Problem:**
- Need separate admin access without affecting user experience
- Admin should see all users and their statistics
- Login history with IP and device tracking
- User management features

**Solution:**
```kotlin
object AdminConfig {
    const val ADMIN_USER_ID = "admin_001"
    const val ADMIN_EMAIL = "admin@gonote.com"
    private const val ADMIN_PASSWORD_HASH = "beyonder"
    
    fun validateCredentials(email: String, password: String): Boolean {
        return email == ADMIN_EMAIL && password == ADMIN_PASSWORD_HASH
    }
}
```

**Result:** Secure admin panel with comprehensive user management features.

### 8.6 Challenge: Dark Mode Implementation

**Problem:**
- Material3 theme customization
- Persistent dark mode preference
- Smooth theme switching

**Solution:**
```kotlin
// Save preference in DataStore
val isDarkMode: Flow<Boolean> = context.dataStore.data.map { 
    preferences[IS_DARK_MODE] ?: false 
}

// Apply theme in Compose
GoNoteTheme(darkTheme = isDarkMode) {
    // App content
}
```

**Result:** Smooth dark mode with persistent preference.

---

## 9. PROJECT STATISTICS

### 9.1 Code Statistics

- **Total Kotlin Files:** 62
- **Lines of Code:** ~6,500
- **Database Tables:** 6
- **Compose Screens:** 50+
- **ViewModels:** 8
- **Repositories:** 1 main + specialized services

### 9.2 Feature Statistics

- **Total Features:** 30+
- **External APIs:** 2 (Google Maps, OpenWeatherMap)
- **Categories:** 6
- **Demo Photos:** 51 high-quality images
- **Demo Notes:** 16 pre-loaded notes
- **Demo Users:** 20 for admin testing

### 9.3 Technical Statistics

- **Minimum Android Version:** 7.0 (API 24)
- **Target Android Version:** 15 (API 35)
- **Supported Devices:** Phones and tablets
- **Architecture:** MVVM
- **Reactive Programming:** Kotlin Flow and StateFlow
- **Dependency Injection:** Manual (Repository pattern)
- **Testing:** Unit tests included

### 9.4 Development Statistics

- **Development Time:** ~3-4 weeks
- **Major Dependencies:** 15+
- **Gradle Build Time:** 30-60 seconds (after initial setup)
- **APK Size:** ~60-70 MB
- **Final Project Size (Clean):** ~35 MB (without build artifacts)

---

## 10. CONCLUSION

### 10.1 Project Summary

GoNote is a fully functional location-based note-taking application that demonstrates modern Android development practices. The app successfully integrates:

- **Modern UI:** Built entirely with Jetpack Compose
- **Location Services:** Real-time GPS and map integration
- **Database:** Persistent local storage with Room
- **Real-Time Monitoring:** Network and GPS status tracking
- **External APIs:** Google Maps and OpenWeatherMap integration
- **User Management:** Authentication, session management, and admin panel
- **Clean Architecture:** MVVM pattern with clear separation of concerns

### 10.2 Learning Outcomes

Through this project, I gained hands-on experience with:

1. **Jetpack Compose:** Building complex UIs declaratively
2. **Google Maps API:** Integrating maps, markers, and location services
3. **Room Database:** Designing and implementing a multi-table database
4. **Kotlin Coroutines & Flow:** Asynchronous programming and reactive data streams
5. **MVVM Architecture:** Structuring Android apps for scalability
6. **API Integration:** Working with REST APIs (Retrofit)
7. **Real-Time Monitoring:** Implementing network and GPS monitoring
8. **State Management:** Managing app state with StateFlow
9. **Photo Management:** Handling camera and gallery integration
10. **Admin Features:** Building secure admin panels and user management

### 10.3 Future Improvements

Potential enhancements for future versions:

- Cloud sync (Firebase integration)
- Note sharing with other users
- Export notes to PDF
- Voice notes
- Note reminders based on location
- Social features (follow users, like notes)
- Search by location radius
- Offline maps support
- Multi-language support
- Widget for quick note creation

### 10.4 Acknowledgments

This project was developed as part of the Mobile Application Development course. Special thanks to:

- Course instructor for guidance and support
- Google for excellent Android documentation
- Android developer community for open-source libraries
- StackOverflow community for troubleshooting help

---

## APPENDIX

### A. File Structure Overview

```
GoNote/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/gonote/  (62 Kotlin files)
│   │   │   ├── res/                       (Resources & images)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                          (Unit tests)
│   │   └── androidTest/                   (Instrumentation tests)
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── PROJECT_REPORT.md                      (This file)
└── .gitignore
```

### B. Permissions Required

The app requires the following Android permissions:

- `INTERNET` - For API calls (maps, weather)
- `ACCESS_NETWORK_STATE` - For network monitoring
- `ACCESS_FINE_LOCATION` - For precise GPS location
- `ACCESS_COARSE_LOCATION` - For approximate location
- `CAMERA` - For taking photos
- `READ_MEDIA_IMAGES` - For selecting photos from gallery (Android 13+)
- `READ_EXTERNAL_STORAGE` - For selecting photos (Android 12 and below)
- `POST_NOTIFICATIONS` - For notification support

All permissions are handled with proper runtime permission requests.

### C. Contact Information

For questions or issues regarding this project:

**Student:** Semih Alkanlı  
**Email:** alkanlisemih@gmail.com  
**Project:** GoNote - Location-Based Note Taking App  
**Course:** Mobile Application Development  
**Date:** December 2025

---

**End of Report**

