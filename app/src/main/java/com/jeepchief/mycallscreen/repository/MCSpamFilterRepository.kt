package com.jeepchief.mycallscreen.repository

import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterDao
import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class MCSpamFilterRepository(private val mcSpamFilterDao: MCSpamFilterDao) {
    val allSpamFilter: Flow<List<MCSpamFilterEntity>> = mcSpamFilterDao.getSpamFilterInfo()

    suspend fun insert(entity: MCSpamFilterEntity) = mcSpamFilterDao.insertSpamFilter(entity)

    suspend fun delete(number: String) = mcSpamFilterDao.deleteSpamFilter(number)

    suspend fun checkFilterNumber(number: String) = flow {
        emit(mcSpamFilterDao.checkFilterNumber(number))
    }.flowOn(Dispatchers.IO)
}