package com.jeepchief.mycallscreen.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtil {
    fun convertSecToMin(time: Int): String {
        val min = time / 60
        val sec = time % 60
        return "${min}분 ${sec}초"
    }

    fun convertMillToDate(time: Long): String = SimpleDateFormat("yyyy년 MM월 dd일 HH시mm분", Locale.KOREA).format(Date(time))
}