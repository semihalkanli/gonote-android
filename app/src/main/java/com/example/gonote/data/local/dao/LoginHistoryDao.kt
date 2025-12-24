package com.example.gonote.data.local.dao

import androidx.room.*
import com.example.gonote.data.local.entity.LoginHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginHistoryDao {
    @Insert
    suspend fun insertLoginHistory(history: LoginHistory)

    @Query("SELECT * FROM login_history WHERE userId = :userId ORDER BY loginAt DESC LIMIT :limit")
    fun getLoginHistory(userId: String, limit: Int = 20): Flow<List<LoginHistory>>

    @Query("SELECT * FROM login_history ORDER BY loginAt DESC LIMIT :limit")
    fun getAllLoginHistory(limit: Int = 100): Flow<List<LoginHistory>>

    @Query("SELECT * FROM login_history WHERE userId = :userId ORDER BY loginAt DESC LIMIT 1")
    suspend fun getLastLogin(userId: String): LoginHistory?

    @Query("DELETE FROM login_history WHERE loginAt < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)

    @Query("SELECT COUNT(DISTINCT userId) FROM login_history WHERE loginAt > :timestamp")
    fun getActiveUsersCount(timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM login_history WHERE userId = :userId")
    fun getUserLoginCount(userId: String): Flow<Int>
}



