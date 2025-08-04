package com.example.teumteum.ui.friend.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.AppUserManager
import com.example.teumteum.data.remote.friend.model.*
import com.example.teumteum.data.remote.friend.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: FriendRepository
) : ViewModel() {

    //  공통 메시지
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    //  1. 사용자 검색
    private val _searchResults = MutableLiveData<List<FriendSearchResult>>()
    val searchResults: LiveData<List<FriendSearchResult>> get() = _searchResults

    private val _recentKeywords = MutableLiveData<List<String>>(emptyList())
    val recentKeywords: LiveData<List<String>> get() = _recentKeywords

    fun searchUser(keyword: String) {
        viewModelScope.launch {
            repository.searchUser(keyword)
                .onSuccess { result ->
                    val myId = AppUserManager.userId
                    val filtered = result.filter { it.userId != myId }

                    when {
                        result.any { it.userId == myId } && filtered.isEmpty() -> {
                            _errorMessage.value = "존재하지 않는 사용자입니다."
                            _searchResults.value = emptyList()
                        }
                        filtered.isEmpty() -> {
                            _errorMessage.value = "존재하지 않는 사용자입니다."
                            _searchResults.value = emptyList()
                        }
                        else -> {
                            _searchResults.value = filtered
                            _successMessage.value = "사용자 조회 성공"
                            Log.d("VIEWMODEL", "성공 메시지 emit됨")
                        }
                    }
                }
                .onFailure { e ->
                    _searchResults.value = emptyList()
                    _errorMessage.value = "사용자 검색 실패 (${e.message})"
                }
        }
    }


    fun addRecentKeyword(keyword: String) {
        val updated = _recentKeywords.value.orEmpty() + keyword
        _recentKeywords.value = updated
    }

    fun removeLastKeyword() {
        val current = _recentKeywords.value.orEmpty()
        _recentKeywords.value = current.dropLast(1)
    }

    //  2. 틈 요청 조회
    private val _receivedTeums = MutableLiveData<List<TeumReceivedItem>>()
    val receivedTeums: LiveData<List<TeumReceivedItem>> get() = _receivedTeums

    fun getTeumRequests() {
        viewModelScope.launch {
            repository.getReceivedTeums()
                .onSuccess { result ->
                    _receivedTeums.value = result
                    _successMessage.value = "틈 요청 조회 성공 (총 ${result.size}개)"
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "요청 또는 응답에 대한 권한이 없습니다."
                        else -> "틈 요청 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }

    //  3. 친구 프로필
    private val _friendProfile = MutableLiveData<FriendProfileResult>()
    val friendProfile: LiveData<FriendProfileResult> get() = _friendProfile

    fun getFriendProfile(userId: Int) {
        viewModelScope.launch {
            repository.getFriendProfile(userId)
                .onSuccess { result ->
                    _friendProfile.value = result
                    _successMessage.value = "친구 프로필 조회 성공"
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("FRIEND4002") == true -> "자기 자신의 프로필은 조회할 수 없습니다."
                        e.message?.contains("FRIEND4040") == true -> "존재하지 않는 유저입니다."
                        else -> "친구 프로필 조회 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }

    //  4. 틈 요청 보내기
    fun sendTeumRequest(request: TeumRequest) {
        viewModelScope.launch {
            repository.sendTeumRequest(request)
                .onSuccess { teumId ->
                    _successMessage.value = "틈 요청이 성공적으로 생성되었습니다. (id: $teumId)"
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("TEUM4030") == true -> "요청 또는 응답에 대한 권한이 없습니다."
                        e.message?.contains("COMMON400") == true -> "잘못된 요청입니다."
                        e.message?.contains("TEUM4091") == true -> "자기 자신에게 틈 요청을 보낼 수 없습니다."
                        else -> "틈 요청 실패 (${e.message})"
                    }
                    _errorMessage.value = msg
                }
        }
    }
}
