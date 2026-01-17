package com.jeepchief.mycallscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jeepchief.mycallscreen.model.db.spamlog.MCSpamLogEntity
import com.jeepchief.mycallscreen.repository.MCSpamLogRepository
import com.jeepchief.mycallscreen.repository.MCSpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MCSpamLogViewModel(private val repo: MCSpamLogRepository): ViewModel() {
    val allSpamLog: StateFlow<List<MCSpamLogEntity>> = repo.allSpamLog.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )

    fun insert(entity: MCSpamLogEntity) = viewModelScope.launch {
        repo.insert(entity)
    }

//    fun getSpamLogWithNum(number: String): StateFlow<List<MCSpamLogEntity>> = repo.spamLogWithNum(number).collect

    private val _spamLogWithNum = MutableStateFlow(listOf<MCSpamLogEntity>())
    val spamLogWithNum: StateFlow<List<MCSpamLogEntity>> = _spamLogWithNum
    fun getSpamLogWithNum(number: String) = viewModelScope.launch {
        _spamLogWithNum.value = repo.spamLogWithNum(number)
    }
}

class MCSpamLogViewModelFactory(private val repo: MCSpamLogRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MCSpamLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MCSpamLogViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}