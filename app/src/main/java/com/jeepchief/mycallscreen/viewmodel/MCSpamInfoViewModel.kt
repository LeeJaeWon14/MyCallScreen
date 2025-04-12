package com.jeepchief.mycallscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoEntity
import com.jeepchief.mycallscreen.repository.MCSpamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MCSpamViewModel(private val repo: MCSpamRepository): ViewModel() {
    val allSpamInfo: StateFlow<List<MCSpamInfoEntity>> = repo.allSpamInfo.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )

    fun insert(entity: MCSpamInfoEntity) = viewModelScope.launch {
        repo.insert(entity)
    }

    fun delete(number: String) = viewModelScope.launch {
        repo.checkNumber(number).collectLatest { result ->
            repo.delete(result)
        }
    }

    private val _checkedNumber = MutableStateFlow<MCSpamInfoEntity>(MCSpamInfoEntity())
    val checkedNumber: StateFlow<MCSpamInfoEntity> = _checkedNumber
    fun checkNumber(number: String) = viewModelScope.launch {
        repo.checkNumber(number).collectLatest { result ->
            _checkedNumber.value = result
        }
    }

//    fun checkDistinctNumber(number: String): Boolean {
//        repo.checkNumber(number).collect {
//
//        }
//    }
}

class MCSpamViewModelFactory(private val repo: MCSpamRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MCSpamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MCSpamViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}