package com.example.gonote.data.local.dao

import androidx.room.*
import com.example.gonote.data.local.entity.AdminActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminActivityLogDao {
    @Insert
    suspend fun insertLog(log: AdminActivityLog)

    @Query("SELECT * FROM admin_activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AdminActivityLog>>

    @Query("SELECT * FROM admin_activity_logs WHERE targetId = :targetId ORDER BY timestamp DESC")
    fun getLogsByTarget(targetId: String): Flow<List<AdminActivityLog>>

    @Query("SELECT * FROM admin_activity_logs WHERE actionType = :actionType ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByActionType(actionType: String, limit: Int = 100): Flow<List<AdminActivityLog>>

    @Query("SELECT * FROM admin_activity_logs WHERE adminEmail = :adminEmail ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByAdmin(adminEmail: String, limit: Int = 100): Flow<List<AdminActivityLog>>

    @Query("DELETE FROM admin_activity_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)

    @Query("SELECT COUNT(*) FROM admin_activity_logs")
    fun getTotalLogsCount(): Flow<Int>
}



