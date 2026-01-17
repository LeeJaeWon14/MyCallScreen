package com.jeepchief.mycallscreen.repository

import com.jeepchief.mycallscreen.model.db.spamlog.MCSpamLogDao
import com.jeepchief.mycallscreen.model.db.spamlog.MCSpamLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MCSpamLogRepository(private val mcSpamLogDao: MCSpamLogDao) {
    val allSpamLog: Flow<List<MCSpamLogEntity>> = mcSpamLogDao.getAllSpamLog()

    suspend fun insert(entity: MCSpamLogEntity) = mcSpamLogDao.insertSpamLog(entity)

//    suspend fun getSpamLogWithNumber(number: String): Flow<List<MCSpamLogEntity>> = flow {
//        emit(mcSpamLogDao.getSpamLogWithNumber(number))
//    }.flowOn(Dispatchers.IO)

    suspend fun spamLogWithNum(number: String) = mcSpamLogDao.getSpamLogWithNumber(number)
}