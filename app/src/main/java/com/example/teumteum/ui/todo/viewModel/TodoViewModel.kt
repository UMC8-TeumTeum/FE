package com.example.teumteum.ui.todo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.repository.TodoRepository
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    // 투두 등록
    fun registerTodo(request: RegisterTodoRequest) {
        viewModelScope.launch {
            val result = todoRepository.registerTodo(request)
            result.onSuccess {
                _registerSuccess.value = true
            }
            result.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "투두 등록에 실패했습니다."
            }
        }
    }
}