package com.jeepchief.mycallscreen.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import com.jeepchief.mycallscreen.MainActivity
import com.jeepchief.mycallscreen.R
import com.jeepchief.mycallscreen.model.Pref
import com.jeepchief.mycallscreen.model.db.MCRoomDatabase
import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterEntity
import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoEntity
import com.jeepchief.mycallscreen.model.db.spamlog.MCSpamLogEntity
import com.jeepchief.mycallscreen.repository.MCSpamFilterRepository
import com.jeepchief.mycallscreen.repository.MCSpamLogRepository
import com.jeepchief.mycallscreen.repository.MCSpamRepository
import com.jeepchief.mycallscreen.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

class MCScreenService: CallScreeningService() {
    private val CALL_SCREEN_CHANNEL_ID = "10101"
    private val spamRepository: MCSpamRepository by lazy {
        MCSpamRepository(MCRoomDatabase.getInstance(this).getSpamInfoDao())
    }
    private val spamLogRepository: MCSpamLogRepository by lazy {
        MCSpamLogRepository(MCRoomDatabase.getInstance(this).getSpamLogDao())
    }
    private val spamFilterRepository: MCSpamFilterRepository by lazy {
        MCSpamFilterRepository(MCRoomDatabase.getInstance(this).getSpamFilterDao())
    }
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

    }

    @SuppressLint("NewApi")
    override fun onScreenCall(callDetail: Call.Details) {
        Logger.log("onScreenCall()")

        // 발신 전화의 경우 리턴
        if(callDetail.callDirection == Call.Details.DIRECTION_OUTGOING) {
//            respondToCall(
//                callDetail,
//                CallResponse.Builder()
//                    .setDisallowCall(true)
//                    .build()
//            )

            return
        }
        if(!Pref.getBoolean(Pref.DISALLOW_CALL)) return

        // 수신 전화번호 가져오기
        val incomingNumber = callDetail.handle.schemeSpecificPart

        serviceScope.launch {
            Logger.log("spam login start")

            val spamEntity = checkSpamList(incomingNumber).also { Logger.log("spamEntity is $it") }
            val spamFilter = checkSpamFilter(incomingNumber).also { Logger.log("spamFilter is $it") }
            val responseBuilder = run {
                val number = spamEntity?.number ?: if(spamFilter) incomingNumber else return@run null
                val name = spamEntity?.name ?: if(spamFilter) "스팸필터링" else return@run null

                // 스팸기록 삽입
                Logger.log("inset spam log")
                spamLogRepository.insert(
                    MCSpamLogEntity(
                        name = name,
                        number = number,
                        time = System.currentTimeMillis()
                    )
                )

                // 알림 생성
                if(Pref.getBoolean(Pref.REJECT_NOTIFICATION)) {
                    Logger.log("create spam notification")
                    val notifyManager = getSystemService(NotificationManager::class.java)
                    val notificationID = Random.nextInt(10000)
                    createNotificationChannel(notifyManager)

                    val notify = NotificationCompat.Builder(this@MCScreenService, CALL_SCREEN_CHANNEL_ID)
                        .setContentTitle("스팸차단알림")
                        .setContentText("[스팸] ${number}(${name}) 전화를 차단하였습니다.")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true)
                        .setContentIntent(
                            PendingIntent.getActivity(
                                this@MCScreenService,
                                notificationID,
                                Intent(this@MCScreenService, MainActivity::class.java).apply {
                                    putExtra("isSpamLogShowing", true)
                                    putExtra("incomingNumber", number)
                                },
                                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                        .build()

                    notifyManager.notify(notificationID, notify)
                }

                // 스팸 목록에서 체크
                CallResponse.Builder()
                    .setDisallowCall(Pref.getBoolean(Pref.DISALLOW_CALL)) // 전화 거부
                    .setRejectCall(Pref.getBoolean(Pref.REJECT_CALL))   // 통화버튼 비활성화
                    .setSkipCallLog(Pref.getBoolean(Pref.SKIP_CALL_LOG)) // 통화기록 남김 여부
                    .setSkipNotification(Pref.getBoolean(Pref.SKIP_NOTIFICATION)) // 통화알림 남김 여부

            }

            // 전화 처리 결과 전달
            responseBuilder?.let {
                respondToCall(callDetail, it.build())
            } ?: respondToCall(callDetail, CallResponse.Builder().build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun checkSpamList(incomingNumber: String): MCSpamInfoEntity? {
        var result: MCSpamInfoEntity? = null
        spamRepository.checkNumber(incomingNumber).collectLatest {
            result = it
        }
        return result
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val notificationChannel = NotificationChannel(
            CALL_SCREEN_CHANNEL_ID,	// 채널을 구분할 ID
            "스팸차단알림",	// 채널 이름
            NotificationManager.IMPORTANCE_HIGH	// 채널의 중요도, 일반적으로 IMPORTANE_HIGH를 사용한다.
        ).apply {
            // 채널에 적용할 설정
            enableLights(true)
            enableVibration(true)
            description = "스팸차단을 알려줍니다."
        }
        // 채널 등록
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun checkSpamProperty(): Boolean {
        return Pref.getBoolean(Pref.DISALLOW_CALL) && Pref.getBoolean(Pref.REJECT_NOTIFICATION)
    }

    private suspend fun checkSpamFilter(number: String): Boolean {
        Logger.log("checkSpamFilter()")
        if(!Pref.getBoolean(Pref.SPAM_FILTER)) {
            return false
        }
        var result: MCSpamFilterEntity? = null

        spamFilterRepository.checkFilterNumber(number).collectLatest {
            result = it
        }
        return result != null
    }

}