# GoNote - Location-Based Note Taking App

A modern Android application that allows users to create notes at specific locations during their travels and memories. Built with Jetpack Compose and modern Android development practices.

## Features

- **Location-Based Notes**: Create notes on Google Maps with precise location tracking
- **Photo Management**: Attach up to 5 photos per note
- **Categories**: Organize notes into 6 categories (Travel, Food, Work, Personal, Shopping, Other)
- **Search & Filter**: Find notes quickly with search and category filters
- **Statistics Dashboard**: View insights about your notes and travels
- **Dark Mode**: Full dark mode support
- **Admin Panel**: User management and system statistics
- **Real-Time Monitoring**: WiFi/Internet and GPS status monitoring
- **City Grouping**: Automatically group notes by city

## Technologies

- **Kotlin** - Official Android programming language
- **Jetpack Compose** - Modern declarative UI framework
- **Room Database** - Local data persistence with 6 tables
- **Google Maps API** - Map integration and location services
- **OpenWeatherMap API** - Real-time weather information
- **Coil** - Efficient image loading
- **Kotlin Coroutines & Flow** - Asynchronous programming and reactive data streams
- **MVVM Architecture** - Clean separation of concerns

## Architecture

```
com.example.gonote/
├── data/                  # Data layer
│   ├── admin/            # Admin configuration
│   ├── local/            # Room Database & DAOs
│   ├── model/            # Data models
│   ├── repository/       # Repository pattern
│   ├── monitor/          # Network & GPS monitoring
│   └── weather/          # Weather API service
├── presentation/         # UI layer (Jetpack Compose)
│   ├── admin/           # Admin panel screens
│   ├── auth/            # Login/Register screens
│   ├── map/             # Map screen with markers
│   ├── note/            # Note detail & creation
│   └── ...
├── navigation/          # Navigation setup
├── ui/                  # UI components & theme
└── util/                # Utility functions
```

## Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or higher
- Android SDK:
  - Minimum SDK: API 24 (Android 7.0)
  - Target SDK: API 35 (Android 15)
- Google Maps API Key
- OpenWeatherMap API Key

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/semihalkanli/gonote-android.git
cd gonote-android
```

### 2. Configure API Keys

1. Copy the template file:
   ```bash
   cp local.properties.template local.properties
   ```

2. Edit `local.properties` and add your API keys:
   ```properties
   sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

   MAPS_API_KEY=your_google_maps_api_key_here
   WEATHER_API_KEY=your_openweathermap_api_key_here

   ADMIN_EMAIL=admin@gonote.com
   ADMIN_PASSWORD=your_secure_admin_password_here
   ```

### 3. Get API Keys

#### Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/google/maps-apis)
2. Create a new project or select an existing one
3. Enable **Maps SDK for Android**
4. Create Credentials → API Key
5. **Restrict the key** (recommended):
   - Application restrictions: Android apps
   - Add package name: `com.example.gonote`
   - Add SHA-1 fingerprint from your keystore:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

#### OpenWeatherMap API Key

1. Go to [OpenWeatherMap](https://openweathermap.org/api)
2. Sign up for a free account
3. Navigate to API Keys section
4. Copy your API key

### 4. Build and Run

1. Open the project in Android Studio
2. Sync Gradle: `File → Sync Project with Gradle Files`
3. Run on device or emulator

Or use command line:
```bash
# Windows
gradlew.bat assembleDebug

# Mac/Linux
./gradlew assembleDebug
```

## Demo Accounts

### Regular User
- Email: `semihalkanli@gmail.com`
- Password: `beyonder`

Features: Full access to note-taking, map, statistics, and profile features.

### Admin User
- Email: (from `local.properties`)
- Password: (from `local.properties`)

Features: Admin panel, user management, system statistics.

## Project Structure

- **Total Kotlin Files**: 62
- **Lines of Code**: ~6,500
- **Database Tables**: 6
- **Compose Screens**: 50+
- **ViewModels**: 8
- **External APIs**: 2

## Building APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires keystore configuration)
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Permissions

The app requires the following permissions:

- `INTERNET` - API calls (maps, weather)
- `ACCESS_NETWORK_STATE` - Network monitoring
- `ACCESS_FINE_LOCATION` - Precise GPS location
- `ACCESS_COARSE_LOCATION` - Approximate location
- `CAMERA` - Taking photos
- `READ_MEDIA_IMAGES` - Gallery access (Android 13+)
- `READ_EXTERNAL_STORAGE` - Gallery access (Android 12 and below)
- `POST_NOTIFICATIONS` - Notification support

All permissions are handled with proper runtime permission requests.

## Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose | 2024.12.01 | Modern UI |
| Room Database | 2.6.1 | Local persistence |
| Google Maps Compose | 5.0.4 | Map display |
| Google Places API | 3.3.0 | Location search |
| Coil | 2.7.0 | Image loading |
| DataStore | 1.1.1 | Preferences |
| Navigation Compose | 2.8.5 | Navigation |
| Retrofit | 2.9.0 | Weather API |
| WorkManager | 2.9.0 | Background tasks |

## Security

- API keys are stored in `local.properties` (not committed to version control)
- Keys are accessed via `BuildConfig` at compile time
- Template file (`local.properties.template`) provides setup guidance
- Admin credentials are configurable per environment

## Documentation

For detailed project documentation, see [PROJECT_REPORT.md](PROJECT_REPORT.md)

## License

This project was developed as part of a Mobile Application Development course.

## Author

**Semih Alkanli**
- Email: alkanlisemih@gmail.com
- GitHub: [@semihalkanli](https://github.com/semihalkanli)

## Acknowledgments

- Course instructor for guidance and support
- Google for excellent Android documentation
- Android developer community for open-source libraries
