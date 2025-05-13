package com.jeepchief.mycallscreen.model.db.spamlog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MCSpamLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "number")
    var number: String = "",

    @ColumnInfo(name = "time")
    var time: Long = 0L
)