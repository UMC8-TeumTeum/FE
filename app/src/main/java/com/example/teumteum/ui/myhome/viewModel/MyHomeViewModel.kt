package com.example.teumteum.ui.myhome.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.mypage.repository.MyPageRepository
import com.example.teumteum.data.remote.todo.model.TodoListResult
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

    // 최근 투두 조회
    private val _recentTodos = MutableLiveData<List<TodoListResult>>()
    val recentTodos: LiveData<List<TodoListResult>> get() = _recentTodos

    fun fetchRecentTodos(date: String) {
        viewModelScope.launch {
            repository.getRecentTodos(date)
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
}