package com.example.gonote.data.repository

import com.example.gonote.data.local.dao.AdminActivityLogDao
import com.example.gonote.data.local.dao.CategoryCount
import com.example.gonote.data.local.dao.CityNoteCount
import com.example.gonote.data.local.dao.LoginHistoryDao
import com.example.gonote.data.local.dao.NoteDao
import com.example.gonote.data.local.dao.NotificationDao
import com.example.gonote.data.local.dao.PhotoDao
import com.example.gonote.data.local.dao.UserDao
import com.example.gonote.data.local.entity.AdminActivityLog
import com.example.gonote.data.local.entity.LoginHistory
import com.example.gonote.data.local.entity.NoteEntity
import com.example.gonote.data.local.entity.NotificationEntity
import com.example.gonote.data.local.entity.PhotoEntity
import com.example.gonote.data.local.entity.UserEntity
import com.example.gonote.data.local.entity.UserStatus
import com.example.gonote.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(
    private val noteDao: NoteDao,
    private val photoDao: PhotoDao,
    private val userDao: UserDao,
    private val adminActivityLogDao: AdminActivityLogDao,
    private val loginHistoryDao: LoginHistoryDao,
    private val notificationDao: NotificationDao
) {
    fun getAllNotes(userId: String): Flow<List<Note>> {
        return noteDao.getAllNotesByUser(userId).map { noteEntities ->
            noteEntities.map { noteEntity ->
                val photos = photoDao.getPhotosByNoteIdSync(noteEntity.id)
                noteEntity.toNote(photos.map { it.photoPath })
            }
        }
    }

    fun getFavoriteNotes(userId: String): Flow<List<Note>> {
        return noteDao.getFavoriteNotes(userId).map { noteEntities ->
            noteEntities.map { noteEntity ->
                val photos = photoDao.getPhotosByNoteIdSync(noteEntity.id)
                noteEntity.toNote(photos.map { it.photoPath })
            }
        }
    }

    suspend fun getNoteById(noteId: Long): Note? {
        val noteEntity = noteDao.getNoteById(noteId) ?: return null
        val photos = photoDao.getPhotosByNoteIdSync(noteId)
        return noteEntity.toNote(photos.map { it.photoPath })
    }

    suspend fun insertNote(note: Note): Long {
        val noteId = noteDao.insertNote(note.toEntity())
        if (note.photos.isNotEmpty()) {
            val photoEntities = note.photos.map {
                PhotoEntity(noteId = noteId, photoPath = it)
            }
            photoDao.insertPhotos(photoEntities)
        }
        return noteId
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
        // Delete old photos and insert new ones
        photoDao.deletePhotosByNoteId(note.id)
        if (note.photos.isNotEmpty()) {
            val photoEntities = note.photos.map {
                PhotoEntity(noteId = note.id, photoPath = it)
            }
            photoDao.insertPhotos(photoEntities)
        }
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
        // Photos will be deleted automatically via CASCADE
    }

    suspend fun toggleFavorite(noteId: Long, isFavorite: Boolean) {
        noteDao.toggleFavorite(noteId, isFavorite)
    }

    fun getTotalNotesCount(userId: String): Flow<Int> {
        return noteDao.getTotalNotesCount(userId)
    }

    fun getFavoriteNotesCount(userId: String): Flow<Int> {
        return noteDao.getFavoriteNotesCount(userId)
    }

    fun getPhotosCount(userId: String): Flow<Int> {
        return photoDao.getPhotosCountByUser(userId)
    }

    // Admin functions
    fun getAllUserIds(): Flow<List<String>> {
        return noteDao.getAllUserIds()
    }

    fun getAllNotesAdmin(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { noteEntities ->
            noteEntities.map { noteEntity ->
                val photos = photoDao.getPhotosByNoteIdSync(noteEntity.id)
                noteEntity.toNote(photos.map { it.photoPath })
            }
        }
    }

    fun getTotalNotesCountAll(): Flow<Int> {
        return noteDao.getTotalNotesCountAll()
    }

    fun getTotalPhotosCountAll(): Flow<Int> {
        return photoDao.getTotalPhotosCount()
    }

    fun getCitiesWithNoteCount(): Flow<List<CityNoteCount>> {
        return noteDao.getCitiesWithNoteCount()
    }

    fun getCategoriesWithCount(): Flow<List<CategoryCount>> {
        return noteDao.getCategoriesWithCount()
    }

    fun getNotesByUserIdFlow(userId: String): Flow<List<Note>> {
        return noteDao.getNotesByUserIdFlow(userId).map { noteEntities ->
            noteEntities.map { noteEntity ->
                val photos = photoDao.getPhotosByNoteIdSync(noteEntity.id)
                noteEntity.toNote(photos.map { it.photoPath })
            }
        }
    }

    suspend fun deleteAllNotesByUser(userId: String) {
        photoDao.deleteAllPhotosByUser(userId)
        noteDao.deleteAllNotesByUser(userId)
    }

    suspend fun getLastActivityByUser(userId: String): Long? {
        return noteDao.getLastActivityByUser(userId)
    }

    // User functions
    suspend fun registerUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    fun getTotalUsersCount(): Flow<Int> {
        return userDao.getTotalUsersCount()
    }

    suspend fun deleteUserById(userId: String) {
        photoDao.deleteAllPhotosByUser(userId)
        noteDao.deleteAllNotesByUser(userId)
        userDao.deleteUserById(userId)
    }

    // Mapper functions
    private fun NoteEntity.toNote(photos: List<String>): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            city = city,
            country = country,
            timestamp = timestamp,
            userId = userId,
            isFavorite = isFavorite,
            photos = photos,
            category = category
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            city = city,
            country = country,
            timestamp = timestamp,
            userId = userId,
            isFavorite = isFavorite,
            category = category
        )
    }

    // ========== USER STATUS MANAGEMENT ==========
    suspend fun updateUserStatus(userId: String, status: UserStatus) {
        userDao.updateUserStatus(userId, status)
    }

    fun getUsersByStatus(status: UserStatus): Flow<List<UserEntity>> {
        return userDao.getUsersByStatus(status)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    // ========== ADMIN ACTIVITY LOGGING ==========
    suspend fun logAdminAction(
        adminEmail: String,
        actionType: String,
        targetId: String,
        targetType: String,
        details: String = ""
    ) {
        val log = AdminActivityLog(
            adminEmail = adminEmail,
            actionType = actionType,
            targetId = targetId,
            targetType = targetType,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        adminActivityLogDao.insertLog(log)
    }

    fun getAdminLogs(limit: Int = 100): Flow<List<AdminActivityLog>> {
        return adminActivityLogDao.getRecentLogs(limit)
    }

    fun getLogsByTarget(targetId: String): Flow<List<AdminActivityLog>> {
        return adminActivityLogDao.getLogsByTarget(targetId)
    }

    fun getLogsByActionType(actionType: String, limit: Int = 100): Flow<List<AdminActivityLog>> {
        return adminActivityLogDao.getLogsByActionType(actionType, limit)
    }

    // ========== LOGIN HISTORY ==========
    suspend fun recordLoginHistory(
        userId: String,
        email: String,
        ipAddress: String,
        deviceInfo: String
    ) {
        val history = LoginHistory(
            userId = userId,
            email = email,
            loginAt = System.currentTimeMillis(),
            ipAddress = ipAddress,
            deviceInfo = deviceInfo
        )
        loginHistoryDao.insertLoginHistory(history)
    }

    suspend fun updateLastLogin(userId: String, ipAddress: String) {
        val timestamp = System.currentTimeMillis()
        userDao.updateLastLogin(userId, timestamp, ipAddress)
    }

    fun getLoginHistory(userId: String, limit: Int = 20): Flow<List<LoginHistory>> {
        return loginHistoryDao.getLoginHistory(userId, limit)
    }

    fun getAllLoginHistory(limit: Int = 100): Flow<List<LoginHistory>> {
        return loginHistoryDao.getAllLoginHistory(limit)
    }

    suspend fun getLastLogin(userId: String): LoginHistory? {
        return loginHistoryDao.getLastLogin(userId)
    }

    // ========== NOTIFICATIONS ==========
    suspend fun sendNotification(userId: String?, title: String, message: String) {
        val notification = NotificationEntity(
            userId = userId,
            title = title,
            message = message,
            sentAt = System.currentTimeMillis(),
            isRead = false
        )
        notificationDao.insertNotification(notification)
    }

    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForUser(userId)
    }

    suspend fun markNotificationAsRead(notificationId: Long) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        notificationDao.markAllAsReadForUser(userId)
    }

    fun getUnreadNotificationsCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    suspend fun deleteNotification(notificationId: Long) {
        notificationDao.deleteNotification(notificationId)
    }

    fun getAllNotifications(limit: Int = 100): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications(limit)
    }

    // ========== NOTE EDITING BY ADMIN ==========
    suspend fun updateNoteByAdmin(noteId: Long, title: String, content: String, category: String) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            val updatedNote = note.copy(
                title = title,
                content = content,
                category = category
            )
            noteDao.updateNote(updatedNote)
        }
    }
}
