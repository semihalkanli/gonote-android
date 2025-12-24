package com.example.gonote.data.local.dao

import androidx.room.*
import com.example.gonote.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId IS NULL ORDER BY sentAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId IS NULL")
    suspend fun markAllAsReadForUser(userId: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE (userId = :userId OR userId IS NULL) AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: Long)

    @Query("DELETE FROM notifications WHERE sentAt < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)

    @Query("SELECT * FROM notifications ORDER BY sentAt DESC LIMIT :limit")
    fun getAllNotifications(limit: Int = 100): Flow<List<NotificationEntity>>
}



