package com.umc.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.teumteum.data.remote.mypage.model.PublicTodoResponse
import com.umc.teumteum.data.remote.mypage.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyHomeViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    private val _nickname = MutableLiveData<String?>()
    val nickname: LiveData<String?> = _nickname

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    private val _field = MutableLiveData<String?>()
    val field: LiveData<String?> = _field

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    private val _socialType = MutableLiveData<String?>()
    val socialType: LiveData<String?> = _socialType

    //내 정보가 이미 조회되었는지 확인
    var isLoaded = false
        private set

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /** 내 정보 조회 */
    fun getMyInfo() {
        viewModelScope.launch {
            repository.getMyInfo()
                .onSuccess { result ->
                    _nickname.value = result.nickname
                    _field.value = result.field
                    _profileImageUrl.value = result.profileImageUrl
                    isLoaded = true
                }
                .onFailure {
                    _error.value = "내 정보 조회 실패: ${it.message}"
                    Log.d("MyInfo", _error.value.toString() )
                }
        }
    }

    fun getMySocialInfo() {
        viewModelScope.launch {
            repository.getMySocialInfo()
                .onSuccess { result ->
                    _email.value = result.email
                    _socialType.value = result.socialType
                }
                .onFailure {
                    _error.value = "내 소셜 정보 조회 실패: ${it.message}"
                    Log.d("MySocialInfo", _error.value.toString() )
                }
        }
    }

    // 최근 투두 조회
    private val _recentTodos = MutableLiveData<List<PublicTodoResponse>>()
    val recentTodos: LiveData<List<PublicTodoResponse>> get() = _recentTodos

    fun fetchRecentTodos() {
        viewModelScope.launch {
            repository.getMyPublicTodos()
                .onSuccess { list ->
                    // 0개면 UI에서 카드 컨테이너 숨기도록 empty 리스트 그대로 전달
                    _recentTodos.value = list
                }
                .onFailure {
                    _recentTodos.value = emptyList()
                    _error.value = "최근 투두리스트 조회 실패: ${it.message}"
                }
        }
    }

    sealed class DeleteUserState {
        object Idle : DeleteUserState()
        object Loading : DeleteUserState()
        object Success : DeleteUserState()
        data class Error(val message: String) : DeleteUserState()
    }

    private val _deleteUserState = MutableLiveData<DeleteUserState>(DeleteUserState.Idle)
    val deleteUserState: LiveData<DeleteUserState> = _deleteUserState

    fun deleteUser() {
        // 중복 호출 방지
        if (_deleteUserState.value is DeleteUserState.Loading) return

        viewModelScope.launch {
            _deleteUserState.value = DeleteUserState.Loading

            repository.deleteUser()
                .onSuccess {
                    _deleteUserState.value = DeleteUserState.Success
                }
                .onFailure { e ->
                    _deleteUserState.value =
                        DeleteUserState.Error(e.message ?: "회원탈퇴 중 문제가 발생했어요.")
                }
        }
    }

    // (옵션) 화면에서 한번 처리한 뒤 상태 초기화용
    fun resetDeleteUserState() {
        _deleteUserState.value = DeleteUserState.Idle
    }

}