package com.example.gonote.data.local.dao

import androidx.room.*
import com.example.gonote.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE noteId = :noteId ORDER BY timestamp ASC")
    fun getPhotosByNoteId(noteId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE noteId = :noteId ORDER BY timestamp ASC")
    suspend fun getPhotosByNoteIdSync(noteId: Long): List<PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE noteId = :noteId")
    suspend fun deletePhotosByNoteId(noteId: Long)

    @Query("SELECT COUNT(*) FROM photos WHERE noteId IN (SELECT id FROM notes WHERE userId = :userId)")
    fun getPhotosCountByUser(userId: String): Flow<Int>

    // Admin queries
    @Query("SELECT COUNT(*) FROM photos")
    fun getTotalPhotosCount(): Flow<Int>

    @Query("DELETE FROM photos WHERE noteId IN (SELECT id FROM notes WHERE userId = :userId)")
    suspend fun deleteAllPhotosByUser(userId: String)
}
