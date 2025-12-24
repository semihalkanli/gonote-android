package com.example.gonote.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gonote.GoNoteApplication
import com.example.gonote.data.local.UserPreferences
import com.example.gonote.presentation.auth.AuthViewModel
import com.example.gonote.presentation.auth.AuthViewModelFactory
import com.example.gonote.presentation.auth.LoginScreen
import com.example.gonote.presentation.auth.RegisterScreen
import com.example.gonote.presentation.home.HomeScreen
import com.example.gonote.presentation.note.AddEditNoteScreen
import com.example.gonote.presentation.note.NoteViewModel
import com.example.gonote.presentation.note.NoteViewModelFactory
import com.example.gonote.presentation.stats.StatsScreen
import com.example.gonote.presentation.stats.StatsViewModel
import com.example.gonote.presentation.stats.StatsViewModelFactory
import com.example.gonote.presentation.settings.SettingsScreen
import com.example.gonote.presentation.notes.NotesListScreen
import com.example.gonote.presentation.favorites.FavoritesListScreen
import com.example.gonote.presentation.photos.PhotosGridScreen
import com.example.gonote.presentation.cities.CitiesListScreen
import com.example.gonote.presentation.cities.CityDetailsScreen
import com.example.gonote.presentation.activity.ActivityChartScreen
import com.example.gonote.presentation.admin.*
import com.example.gonote.presentation.map.FullScreenPhotoViewer
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    userPreferences: UserPreferences,
    startDestination: String
) {
    val context = LocalContext.current
    val application = context.applicationContext as GoNoteApplication

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userPreferences, application.repository)
    )
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            // Navigate to admin panel or home based on login type
            LaunchedEffect(authState.isLoggedIn, authState.isAdmin) {
                if (authState.isLoggedIn) {
                    if (authState.isAdmin) {
                        navController.navigate(Screen.AdminPanel.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }

            // Navigate to register when user not found - automatic redirect
            LaunchedEffect(authState.userNotFound) {
                if (authState.userNotFound) {
                    authViewModel.clearUserNotFoundFlag()
                    navController.navigate(Screen.Register.route)
                }
            }

            LoginScreen(
                authState = authState,
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onErrorDismiss = {
                    authViewModel.clearError()
                }
            )
        }

        composable(Screen.Register.route) {
            // Navigate to login when registration is successful
            LaunchedEffect(authState.isRegistered) {
                if (authState.isRegistered) {
                    authViewModel.clearRegisteredFlag()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                authState = authState,
                onRegisterClick = { email, password, confirmPassword ->
                    authViewModel.register(email, password, confirmPassword)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onErrorDismiss = {
                    authViewModel.clearError()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userPreferences = userPreferences,
                onAddNoteClick = { location ->
                    navController.navigate(
                        Screen.AddNote.createRoute(location.latitude, location.longitude)
                    )
                },
                onEditNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                },
                onStatsClick = {
                    navController.navigate(Screen.Statistics.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.AddNote.route,
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType },
                navArgument("longitude") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val noteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(application.repository, context)
            )

            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            val userId by userPreferences.userId.collectAsState(initial = null)

            LaunchedEffect(Unit) {
                noteViewModel.setLocation(LatLng(latitude, longitude))
            }

            val noteState by noteViewModel.noteState.collectAsState()

            AddEditNoteScreen(
                noteState = noteState,
                isEditMode = false,
                onTitleChange = { noteViewModel.updateTitle(it) },
                onContentChange = { noteViewModel.updateContent(it) },
                onFavoriteToggle = { noteViewModel.toggleFavorite() },
                onCategoryChange = { noteViewModel.updateCategory(it) },
                onAddPhoto = { noteViewModel.addPhoto(it) },
                onRemovePhoto = { noteViewModel.removePhoto(it) },
                onSaveClick = {
                    userId?.let { noteViewModel.saveNote(it) }
                },
                onBackClick = { navController.popBackStack() },
                onErrorDismiss = { noteViewModel.clearError() }
            )
        }

        composable(
            route = Screen.EditNote.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val noteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(application.repository, context)
            )

            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val userId by userPreferences.userId.collectAsState(initial = null)

            LaunchedEffect(noteId) {
                noteViewModel.loadNote(noteId)
            }

            val noteState by noteViewModel.noteState.collectAsState()

            AddEditNoteScreen(
                noteState = noteState,
                isEditMode = true,
                onTitleChange = { noteViewModel.updateTitle(it) },
                onContentChange = { noteViewModel.updateContent(it) },
                onFavoriteToggle = { noteViewModel.toggleFavorite() },
                onCategoryChange = { noteViewModel.updateCategory(it) },
                onAddPhoto = { noteViewModel.addPhoto(it) },
                onRemovePhoto = { noteViewModel.removePhoto(it) },
                onSaveClick = {
                    userId?.let { noteViewModel.saveNote(it, noteId) }
                },
                onBackClick = { navController.popBackStack() },
                onErrorDismiss = { noteViewModel.clearError() }
            )
        }

        composable(Screen.Statistics.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val statsViewModel: StatsViewModel = viewModel(
                factory = StatsViewModelFactory(application.repository)
            )

            val userId by userPreferences.userId.collectAsState(initial = null)

            LaunchedEffect(userId) {
                userId?.let { statsViewModel.loadStats(it) }
            }

            val statsState by statsViewModel.statsState.collectAsState()

            StatsScreen(
                statsState = statsState,
                onBackClick = { navController.popBackStack() },
                onNotesClick = {
                    navController.navigate(Screen.NotesList.route)
                },
                onFavoritesClick = {
                    navController.navigate(Screen.FavoritesList.route)
                },
                onPhotosClick = {
                    navController.navigate(Screen.PhotosGrid.route)
                },
                onCitiesClick = {
                    navController.navigate(Screen.CitiesList.route)
                },
                onActivityChartClick = {
                    navController.navigate(Screen.ActivityChart.route)
                }
            )
        }

        composable(Screen.NotesList.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId by userPreferences.userId.collectAsState(initial = null)

            var notes by remember { mutableStateOf<List<com.example.gonote.data.model.Note>>(emptyList()) }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    application.repository.getAllNotes(id).collect {
                        notes = it
                    }
                }
            }

            NotesListScreen(
                notes = notes,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        composable(Screen.FavoritesList.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId by userPreferences.userId.collectAsState(initial = null)

            var notes by remember { mutableStateOf<List<com.example.gonote.data.model.Note>>(emptyList()) }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    application.repository.getAllNotes(id).collect {
                        notes = it
                    }
                }
            }

            FavoritesListScreen(
                notes = notes,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        composable(Screen.PhotosGrid.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId by userPreferences.userId.collectAsState(initial = null)

            var notes by remember { mutableStateOf<List<com.example.gonote.data.model.Note>>(emptyList()) }
            var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }

            // Get all photos from all notes
            val allPhotos = remember(notes) { notes.flatMap { it.photos } }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    application.repository.getAllNotes(id).collect {
                        notes = it
                    }
                }
            }

            PhotosGridScreen(
                notes = notes,
                onBackClick = { navController.popBackStack() },
                onPhotoClick = { photoPath ->
                    // Find index of clicked photo in allPhotos list
                    val index = allPhotos.indexOf(photoPath)
                    if (index >= 0) {
                        selectedPhotoIndex = index
                    }
                }
            )

            selectedPhotoIndex?.let { index ->
                FullScreenPhotoViewer(
                    photos = allPhotos,
                    initialIndex = index,
                    onDismiss = { selectedPhotoIndex = null }
                )
            }
        }

        composable(Screen.CitiesList.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId by userPreferences.userId.collectAsState(initial = null)

            var notes by remember { mutableStateOf<List<com.example.gonote.data.model.Note>>(emptyList()) }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    application.repository.getAllNotes(id).collect {
                        notes = it
                    }
                }
            }

            CitiesListScreen(
                notes = notes,
                onBackClick = { navController.popBackStack() },
                onCityClick = { cityName ->
                    navController.navigate(Screen.CityDetails.createRoute(cityName))
                }
            )
        }

        composable(
            route = Screen.CityDetails.route,
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId by userPreferences.userId.collectAsState(initial = null)
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""

            var notes by remember { mutableStateOf<List<com.example.gonote.data.model.Note>>(emptyList()) }
            var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    application.repository.getAllNotes(id).collect {
                        notes = it
                    }
                }
            }

            // Get all photos from city's notes
            val cityNotes = remember(notes, cityName) { notes.filter { it.city == cityName } }
            val cityPhotos = remember(cityNotes) { cityNotes.flatMap { it.photos } }

            CityDetailsScreen(
                cityName = cityName,
                notes = notes,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                },
                onPhotoClick = { photoPath ->
                    // Find index of clicked photo in cityPhotos list
                    val index = cityPhotos.indexOf(photoPath)
                    if (index >= 0) {
                        selectedPhotoIndex = index
                    }
                }
            )

            selectedPhotoIndex?.let { index ->
                FullScreenPhotoViewer(
                    photos = cityPhotos,
                    initialIndex = index,
                    onDismiss = { selectedPhotoIndex = null }
                )
            }
        }

        composable(Screen.ActivityChart.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId by userPreferences.userId.collectAsState(initial = null)

            var notes by remember { mutableStateOf<List<com.example.gonote.data.model.Note>>(emptyList()) }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    application.repository.getAllNotes(id).collect {
                        notes = it
                    }
                }
            }

            ActivityChartScreen(
                notes = notes,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val userEmail by userPreferences.userEmail.collectAsState(initial = "")
            val isDarkMode by userPreferences.isDarkMode.collectAsState(initial = false)
            val autoDarkMode by userPreferences.autoDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            SettingsScreen(
                userEmail = userEmail ?: "Not logged in",
                isDarkMode = isDarkMode,
                autoDarkMode = autoDarkMode,
                onDarkModeToggle = { isDark ->
                    scope.launch {
                        userPreferences.setDarkMode(isDark)
                    }
                },
                onAutoDarkModeToggle = { isAuto ->
                    scope.launch {
                        userPreferences.setAutoDarkMode(isAuto)
                    }
                },
                onLogoutClick = {
                    scope.launch {
                        userPreferences.clearUserSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Admin Screens
        composable(Screen.AdminPanel.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val scope = rememberCoroutineScope()

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val dashboardState by adminViewModel.dashboardState.collectAsState()

            LaunchedEffect(Unit) {
                adminViewModel.loadDashboard()
            }

            AdminPanelScreen(
                dashboardState = dashboardState,
                onBackClick = {
                    scope.launch {
                        authViewModel.logout()
                        userPreferences.clearUserSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onUsersClick = {
                    navController.navigate(Screen.AdminUsers.route)
                },
                onStatsClick = {
                    navController.navigate(Screen.AdminStats.route)
                },
                onSystemClick = {
                    navController.navigate(Screen.AdminSystem.route)
                },
                onLogoutClick = {
                    scope.launch {
                        authViewModel.logout()
                        userPreferences.clearUserSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onAdminLogsClick = {
                    navController.navigate(Screen.AdminLogs.route)
                },
                onSendNotificationClick = {
                    navController.navigate(Screen.SendNotification.route)
                },
                onAllLoginHistoryClick = {
                    navController.navigate(Screen.AllLoginHistory.route)
                }
            )
        }

        composable(Screen.AdminUsers.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val usersState by adminViewModel.usersState.collectAsState()

            LaunchedEffect(Unit) {
                adminViewModel.loadUsers()
            }

            AdminUsersScreen(
                usersState = usersState,
                onBackClick = { navController.popBackStack() },
                onUserClick = { userId ->
                    navController.navigate(Screen.AdminUserDetail.createRoute(userId))
                },
                onDeleteUser = { userId ->
                    adminViewModel.deleteUser(userId, "admin@gonote.com")
                },
                onStatusChange = { userId, newStatus ->
                    adminViewModel.updateUserStatus(userId, newStatus, "admin@gonote.com")
                },
                onLoginHistoryClick = { userId ->
                    navController.navigate(Screen.UserLoginHistory.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val userNotes by adminViewModel.userNotes.collectAsState()

            LaunchedEffect(userId) {
                adminViewModel.loadUserNotes(userId)
            }

            AdminUserDetailScreen(
                userId = userId,
                userNotes = userNotes,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        composable(Screen.AdminStats.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val statsState by adminViewModel.statsState.collectAsState()

            LaunchedEffect(Unit) {
                adminViewModel.loadStats()
            }

            AdminStatsScreen(
                statsState = statsState,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminSystem.route) {
            AdminSystemScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminLogs.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val logsState by adminViewModel.logsState.collectAsState()

            LaunchedEffect(Unit) {
                adminViewModel.loadAdminLogs()
            }

            AdminLogsScreen(
                logsState = logsState,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SendNotification.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val usersState by adminViewModel.usersState.collectAsState()

            LaunchedEffect(Unit) {
                adminViewModel.loadUsers()
            }

            SendNotificationScreen(
                usersState = usersState,
                onBackClick = { navController.popBackStack() },
                onSendNotification = { userIds, title, message ->
                    adminViewModel.sendNotification(userIds, title, message, "admin@gonote.com")
                }
            )
        }

        composable(
            route = Screen.UserLoginHistory.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication
            val userId = backStackEntry.arguments?.getString("userId")

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val historyState by adminViewModel.loginHistoryState.collectAsState()

            LaunchedEffect(userId) {
                userId?.let { adminViewModel.loadLoginHistory(it) }
            }

            LoginHistoryScreen(
                historyState = historyState,
                userId = userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AllLoginHistory.route) {
            val context = LocalContext.current
            val application = context.applicationContext as GoNoteApplication

            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(application.repository, context)
            )

            val historyState by adminViewModel.loginHistoryState.collectAsState()

            LaunchedEffect(Unit) {
                adminViewModel.loadLoginHistory()
            }

            LoginHistoryScreen(
                historyState = historyState,
                userId = null,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
