package com.jeepchief.mycallscreen.model.db.spamfilter

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MCSpamFilterDao {
    @Query("SELECT * FROM MCSpamFilterEntity")
    fun getSpamFilterInfo(): Flow<List<MCSpamFilterEntity>>

    @Query("DELETE FROM MCSpamFilterEntity WHERE filter_number LIKE :number")
    suspend fun deleteSpamFilter(number: String) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpamFilter(entity: MCSpamFilterEntity)

//    @Query("SELECT * FROM MCSpamFilterEntity WHERE 'number' LIKE :number")
    @Query("SELECT * FROM MCSpamFilterEntity WHERE instr(:number, filter_number) > 0")
    suspend fun checkFilterNumber(number: String): MCSpamFilterEntity?
}