package com.jeepchief.mycallscreen.model

import android.content.Context
import android.content.SharedPreferences
import com.jeepchief.mycallscreen.MCApplication

object Pref {
    private val PREF_NAME = "MCS_Preference"
    private val preference: SharedPreferences by lazy { MCApplication.getAppContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }

    const val DISALLOW_CALL = "setDisallowCall"
    const val REJECT_CALL = "setRejectCall"
    const val SKIP_CALL_LOG = "setSkipCallLog"
    const val SKIP_NOTIFICATION = "setSkipNotification"
    const val REJECT_NOTIFICATION = "setRejectNotification"
    const val SPAM_FILTER = "setSpamFilter"

    fun getString(id: String?) : String? = preference.getString(id, "")

    fun getBoolean(id: String?) : Boolean = preference.getBoolean(id, false)

    fun setValue(id: String?, value: String) : Boolean =
        preference.edit()
            .putString(id, value)
            .commit()

    fun setValue(id: String?, value: Boolean) : Boolean =
        preference.edit()
            .putBoolean(id, value)
            .commit()

    fun removeValue(id: String?) : Boolean =
        preference.edit()
            .remove(id)
            .commit()

}