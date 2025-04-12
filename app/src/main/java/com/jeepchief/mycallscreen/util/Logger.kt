package com.jeepchief.mycallscreen.util

import android.util.Log

object Logger {
    private const val TAG = "MCS"
    fun log(message: String) = Log.d(TAG, message)
}