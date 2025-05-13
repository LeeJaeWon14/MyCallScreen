package com.jeepchief.mycallscreen.viewmodel

import android.content.Context
import android.provider.CallLog
import androidx.lifecycle.ViewModel
import com.jeepchief.mycallscreen.data.CallLogVO
import com.jeepchief.mycallscreen.data.CallType

class MCViewModel: ViewModel() {
    fun getCallLog(context: Context): List<CallLogVO> {
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DEFAULT_SORT_ORDER
        )

        val list = mutableListOf<CallLogVO>()
        while (cursor?.moveToNext() == true) {
            val type = when (cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))) {
                1 -> CallType.INCOMING // "수신"
                2 -> CallType.OUTGOING // "발신"
                3 -> CallType.MISSED // "부i재중"
                5 -> CallType.REJECT // "거절"
                6 -> CallType.SPAM // "차단"
                else -> CallType.UNSPECIFIED // "미지정"
            }
//            val name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)) ?: "미등록번호"
            val name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))?.ifBlank { "미등록번호" } ?: "미등록번호"
            val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            val duration = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
            list.add(
                CallLogVO(
                    type = type,
                    name = name,
                    number = number,
                    duration = duration
                )
            )
        }

        cursor?.close()
        return list
    }
}