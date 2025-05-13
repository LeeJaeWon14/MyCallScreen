package com.jeepchief.mycallscreen.model.db.spamfilter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MCSpamFilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "filter_number")
    val filterNumber: String
)