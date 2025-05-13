package com.jeepchief.mycallscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterEntity
import com.jeepchief.mycallscreen.repository.MCSpamFilterRepository
import com.jeepchief.mycallscreen.repository.MCSpamRepository
import com.jeepchief.mycallscreen.util.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MCSpamFilterViewModel(private val repo: MCSpamFilterRepository): ViewModel() {
    val allSpamFilter: StateFlow<List<MCSpamFilterEntity>> = repo.allSpamFilter.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        listOf()
    )

    fun insert(entity: MCSpamFilterEntity) = viewModelScope.launch {
        repo.insert(entity)
    }

    fun delete(vararg number: String) = viewModelScope.launch {
        for (i in number.indices) {
            Logger.log("delete start")
            repo.delete(number[i]).also { Logger.log("delete result > $it") }
            Logger.log("delete end")
        }
    }

    fun checkFilterNumber(number: String) = viewModelScope.launch {
        repo.checkFilterNumber(number).collectLatest {

        }
    }
}

class MCSpamFilterViewModelFactory(private val repo: MCSpamFilterRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MCSpamFilterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MCSpamFilterViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}