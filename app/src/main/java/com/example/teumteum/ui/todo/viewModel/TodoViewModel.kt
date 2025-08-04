package com.example.teumteum.ui.todo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.entities.TodoList
import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.repository.TodoRepository
import com.example.teumteum.data.remote.wish.model.RegisterWishRequest
import com.example.teumteum.data.remote.wish.model.WishlistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _todolistItems = MutableLiveData<List<TodoList>>()
    val todolistItems: LiveData<List<TodoList>> get() = _todolistItems

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

    // 투두리스트 조회
    fun getTodoList(date: String) {
        viewModelScope.launch {
            val result = todoRepository.getTodoList(date)

            result.onSuccess { list ->
                _todolistItems.value = list
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "투두리스트 조회에 실패했습니다."
            }
        }
    }


}