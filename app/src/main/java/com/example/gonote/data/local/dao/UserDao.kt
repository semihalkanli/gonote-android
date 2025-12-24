package com.example.gonote.data.local.dao

import androidx.room.*
import com.example.gonote.data.local.entity.UserEntity
import com.example.gonote.data.local.entity.UserStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUsersCount(): Flow<Int>

    // User Status Management
    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: UserStatus)

    @Query("UPDATE users SET lastLoginAt = :timestamp, lastLoginIp = :ip WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long, ip: String)

    @Query("SELECT * FROM users WHERE status = :status ORDER BY createdAt DESC")
    fun getUsersByStatus(status: UserStatus): Flow<List<UserEntity>>

    @Update
    suspend fun updateUser(user: UserEntity)
}
