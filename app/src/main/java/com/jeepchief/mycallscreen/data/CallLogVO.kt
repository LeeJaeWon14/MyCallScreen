package com.jeepchief.mycallscreen.data

import com.jeepchief.mycallscreen.data.CallType

data class CallLogVO(
    val type: CallType = CallType.UNSPECIFIED,
    val name: String = "",       // 이름
    val number: String = "",     // 전화번호
    val duration: Int = 0,       // 통화시간
    val time: Long = 0L          // 시간
)