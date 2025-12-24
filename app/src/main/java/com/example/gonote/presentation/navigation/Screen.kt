package com.example.gonote.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object AddNote : Screen("add_note/{latitude}/{longitude}") {
        fun createRoute(latitude: Double, longitude: Double) = "add_note/$latitude/$longitude"
    }
    data object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "edit_note/$noteId"
    }
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
    data object NotesList : Screen("notes_list")
    data object FavoritesList : Screen("favorites_list")
    data object PhotosGrid : Screen("photos_grid")
    data object CitiesList : Screen("cities_list")
    data object CityDetails : Screen("city_details/{cityName}") {
        fun createRoute(cityName: String) = "city_details/$cityName"
    }
    data object ActivityChart : Screen("activity_chart")
    
    // Admin screens
    data object AdminPanel : Screen("admin_panel")
    data object AdminUsers : Screen("admin_users")
    data object AdminUserDetail : Screen("admin_user_detail/{userId}") {
        fun createRoute(userId: String) = "admin_user_detail/$userId"
    }
    data object AdminStats : Screen("admin_stats")
    data object AdminSystem : Screen("admin_system")
    data object AdminLogs : Screen("admin_logs")
    data object SendNotification : Screen("send_notification")
    data object AdminEditNote : Screen("admin_edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "admin_edit_note/$noteId"
    }
    data object UserLoginHistory : Screen("user_login_history/{userId}") {
        fun createRoute(userId: String) = "user_login_history/$userId"
    }
    data object AllLoginHistory : Screen("all_login_history")
}
