package com.example.gonote.data.local.dao

import androidx.room.*
import com.example.gonote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllNotesByUser(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE userId = :userId AND isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteNotes(userId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("UPDATE notes SET isFavorite = :isFavorite WHERE id = :noteId")
    suspend fun toggleFavorite(noteId: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId")
    fun getTotalNotesCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isFavorite = 1")
    fun getFavoriteNotesCount(userId: String): Flow<Int>

    // Admin queries
    @Query("SELECT DISTINCT userId FROM notes")
    fun getAllUserIds(): Flow<List<String>>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes")
    fun getTotalNotesCountAll(): Flow<Int>

    @Query("SELECT city, COUNT(*) as count FROM notes WHERE city != '' AND city != 'Unknown City' GROUP BY city ORDER BY count DESC")
    fun getCitiesWithNoteCount(): Flow<List<CityNoteCount>>

    @Query("SELECT category, COUNT(*) as count FROM notes GROUP BY category ORDER BY count DESC")
    fun getCategoriesWithCount(): Flow<List<CategoryCount>>

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotesByUserIdFlow(userId: String): Flow<List<NoteEntity>>

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotesByUser(userId: String)

    @Query("SELECT MAX(timestamp) FROM notes WHERE userId = :userId")
    suspend fun getLastActivityByUser(userId: String): Long?
}

data class CityNoteCount(
    val city: String,
    val count: Int
)

data class CategoryCount(
    val category: String,
    val count: Int
)
