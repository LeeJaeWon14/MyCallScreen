package com.jeepchief.mycallscreen.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterDao
import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterEntity
import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoDao
import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoEntity
import com.jeepchief.mycallscreen.model.db.spamlog.MCSpamLogDao
import com.jeepchief.mycallscreen.model.db.spamlog.MCSpamLogEntity

@Database(entities = [MCSpamInfoEntity::class, MCSpamLogEntity::class, MCSpamFilterEntity::class], version = 3, exportSchema = false)
abstract class MCRoomDatabase: RoomDatabase() {
    abstract fun getSpamInfoDao(): MCSpamInfoDao
    abstract fun getSpamLogDao(): MCSpamLogDao
    abstract fun getSpamFilterDao(): MCSpamFilterDao

    companion object {
        private var instance: MCRoomDatabase? = null
        fun getInstance(context: Context): MCRoomDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext,
                MCRoomDatabase::class.java,
                "MCSpam.db"
            )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}