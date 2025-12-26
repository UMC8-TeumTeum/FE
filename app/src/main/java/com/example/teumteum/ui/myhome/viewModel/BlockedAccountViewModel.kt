package com.example.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.mypage.repository.BlockedAccountRepository
import com.example.teumteum.ui.myhome.data.BlockedAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedAccountViewModel @Inject constructor(
    private val repository: BlockedAccountRepository
) : ViewModel() {

    private val _blockedAccountList = MutableLiveData<List<BlockedAccount>>()
    val blockedAccountList: LiveData<List<BlockedAccount>> = _blockedAccountList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getBlockedAccounts() {
        viewModelScope.launch {
            try {
                val list = repository.fetchBlockedAccounts()
                Log.d("BlockedAccounts", "목록 갯수: ${list.size}")
                _blockedAccountList.value = list
            } catch (e: Exception) {
                Log.d("BlockedAccounts", "예외: ${e.message}")
                _error.value = e.message ?: "차단 목록 조회 실패"
            }
        }
    }
}
