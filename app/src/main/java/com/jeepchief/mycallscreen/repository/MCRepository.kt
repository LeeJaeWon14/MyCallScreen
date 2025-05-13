package com.jeepchief.mycallscreen.repository

import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoDao
import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MCSpamRepository(private val mcSpamInfoDao: MCSpamInfoDao) {
    val allSpamInfo: Flow<List<MCSpamInfoEntity>> = mcSpamInfoDao.getSpamInfo()

    suspend fun insert(entity: MCSpamInfoEntity) = mcSpamInfoDao.insertSpamInfo(entity)

    suspend fun delete(entity: MCSpamInfoEntity) = mcSpamInfoDao.deleteSpamInfo(entity)

    suspend fun checkNumber(number: String): Flow<MCSpamInfoEntity> = flow {
        emit(mcSpamInfoDao.checkNumber(number))
    }.flowOn(Dispatchers.IO)
}