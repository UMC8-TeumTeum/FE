package com.example.teumteum.ui.todo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teumteum.data.remote.todo.model.EditTodoRequest
import com.example.teumteum.data.remote.todo.model.GetOnboardingReminders
import com.example.teumteum.data.remote.todo.model.GetTodoResult
import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.data.remote.todo.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _todo = MutableLiveData<GetTodoResult>()
    val todo: LiveData<GetTodoResult> = _todo

    private val _todolistItems = MutableLiveData<List<TodoListResult>>()
    val todolistItems: LiveData<List<TodoListResult>> get() = _todolistItems

    private val _reminders = MutableLiveData<GetOnboardingReminders>()
    val reminders: LiveData<GetOnboardingReminders> = _reminders

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _editSuccess = MutableLiveData<Boolean>()
    val editSuccess: LiveData<Boolean> get() = _editSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> get() = _deleteSuccess

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

    // 특정 투두 조회
    fun getTodo(todoId: Long) {
        viewModelScope.launch {
            val result = todoRepository.getTodo(todoId)

            result.onSuccess {
                _todo.value = it
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "투두리스트 조회에 실패했습니다."
            }
        }
    }

    // 투두 편집
    fun editTodo(todoId: Long, request: EditTodoRequest) {
        viewModelScope.launch {
            val result = todoRepository.editTodo(todoId, request)
            result.onSuccess {
                _editSuccess.value = true
            }
            result.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "투두 등록에 실패했습니다."
            }
        }
    }

    // 투두 삭제
    fun deleteTodo(todoId: Long) {
        viewModelScope.launch {
            val result = todoRepository.deleteTodo(todoId)
            result.onSuccess {
                _deleteSuccess.value = true
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "투두 삭제에 실패했습니다."
            }
        }
    }

    // 온보딩의 리마인드 알림 조회
    fun getOnboardingReminders() {
        viewModelScope.launch {
            val result = todoRepository.getOnboardingReminders()

            result.onSuccess {
                _reminders.value = it
            }.onFailure { e ->
                _errorMessage.value = e.localizedMessage ?: "리마인드 알림 조회에 실패했습니다."
            }
        }
    }

}