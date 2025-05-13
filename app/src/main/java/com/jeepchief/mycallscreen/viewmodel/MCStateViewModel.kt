package com.jeepchief.mycallscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MCStateViewModel: ViewModel() {
    private val _selectedItem = MutableStateFlow(0)
    val selectedItem: StateFlow<Int> = _selectedItem
    fun setSelectedItem(value: Int) {
        _selectedItem.value = value
    }

    // SpamListScreen > 스팸 추가 Dialog 출력 State
    private val _isAddSpamDialogShowing = MutableStateFlow(false)
    val isAddSpamDialogShowing: StateFlow<Boolean> = _isAddSpamDialogShowing
    fun setIsAddSpamDialogShowing(value: Boolean) {
        _isAddSpamDialogShowing.value = value
    }

    // 앱 권한설정 State
    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted
    fun setIsPermissionGranted(value: Boolean) {
        _isPermissionGranted.value = value
    }

    // 전화기록 > 추가동작 Dialog 출력 State
    private val _isLogMenuDialogShowing = MutableStateFlow(false)
    val isLogMenuDialogShowing: StateFlow<Boolean> = _isLogMenuDialogShowing
    fun setIsLogMenuDialogShowing(value: Boolean) {
        _isLogMenuDialogShowing.value = value
    }

    // 스팸차단 알림 클릭 > 차단기록 State
    private val _isSpamLogShowing = MutableStateFlow(false)
    val isSpamLogShowing: StateFlow<Boolean> = _isSpamLogShowing
    fun setIsSpamLogShowing(value: Boolean) {
        _isSpamLogShowing.value = value
    }
    
    // 번호필터링 Dialog State
    private val _isSpamFilterDialogShowing = MutableStateFlow(false)
    val isSpamFilterDialogShowing: StateFlow<Boolean> = _isSpamFilterDialogShowing
    fun setIsSpamFilterDialogShowing(value: Boolean) {
        _isSpamFilterDialogShowing.value = value
    }
}