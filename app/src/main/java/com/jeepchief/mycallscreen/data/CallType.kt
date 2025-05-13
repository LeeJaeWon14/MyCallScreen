package com.jeepchief.mycallscreen.data

enum class CallType(val desc: String) {
    INCOMING ("수신"),
    OUTGOING ("발신"),
    MISSED ("부재중"),
    REJECT ("거절"),
    SPAM ("차단"),
    UNSPECIFIED ("미지정")
}