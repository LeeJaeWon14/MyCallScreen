package com.jeepchief.mycallscreen.model.db.spaminfo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MCSpamInfoDao {
    @Query("SELECT * FROM MCSpamInfoEntity")
    fun getSpamInfo(): Flow<List<MCSpamInfoEntity>>

    @Delete
    suspend fun deleteSpamInfo(entity: MCSpamInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpamInfo(entity: MCSpamInfoEntity)

    @Query("SELECT * FROM MCSpamInfoEntity WHERE number LIKE :number")
    suspend fun checkNumber(number: String): MCSpamInfoEntity
}