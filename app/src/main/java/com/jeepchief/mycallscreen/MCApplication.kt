package com.jeepchief.mycallscreen

import android.app.Application
import android.content.Context

class MCApplication: Application() {

    companion object {
        private lateinit var context: Context
        fun getAppContext(): Context = MCApplication.context
    }

    override fun onCreate() {
        super.onCreate()

        MCApplication.context = applicationContext
    }
}