package com.jeepchief.mycallscreen.model.db.spamlog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MCSpamLogDao {
    @Query("SELECT * FROM MCSpamLogEntity")
    fun getAllSpamLog(): Flow<List<MCSpamLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpamLog(entity: MCSpamLogEntity)

    @Query("SELECT * FROM MCSpamLogEntity WHERE number LIKE :number")
    suspend fun getSpamLogWithNumber(number: String): List<MCSpamLogEntity>
}