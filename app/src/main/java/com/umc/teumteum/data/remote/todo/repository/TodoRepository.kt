package com.umc.teumteum.data.remote.todo.repository

import android.util.Log
import com.umc.teumteum.data.remote.todo.model.AlarmStatusRequest
import com.umc.teumteum.data.remote.todo.model.EditTodoRequest
import com.umc.teumteum.data.remote.todo.model.GetOnboardingReminders
import com.umc.teumteum.data.remote.todo.model.GetTodoResult
import com.umc.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.umc.teumteum.data.remote.todo.model.TodoListResult
import com.umc.teumteum.data.remote.todo.model.TodoResult
import com.umc.teumteum.data.remote.todo.service.TodoService
import com.umc.teumteum.utils.handleApiResponse
import com.umc.teumteum.utils.handleApiResponseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoService: TodoService
) {

    // 투두 등록
    suspend fun registerTodo(request: RegisterTodoRequest): Result<TodoResult> = runCatching {
        val response = todoService.registerTodo(request)
        Log.d("RegisterTodo", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 투두리스트 조회
    suspend fun getTodoList(date: String): Result<List<TodoListResult>> = runCatching {
        val response = todoService.getTodoList(date)
        Log.d("GetTodoList", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 특정 투두 조회
    suspend fun getTodo(todoId: Long): Result<GetTodoResult> = runCatching {
        val response = todoService.getTodo(todoId)
        Log.d("GetTodo", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 투두 수정
    suspend fun editTodo(todoId: Long, request: EditTodoRequest): Result<TodoResult> = runCatching {
        val response = todoService.editTodo(todoId, request)
        Log.d("EditTodo", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 특정 투두 삭제
    suspend fun deleteTodo(todoId: Long): Result<Unit> = runCatching {
        val response = todoService.deleteTodo(todoId)
        Log.d("DeleteTodo", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }

    // 온보딩의 리마인드 알림 정보 조회
    suspend fun getOnboardingReminders(): Result<GetOnboardingReminders> = runCatching {
        val response = todoService.getOnboardingReminders()
        Log.d("GetOnboardingReminders", "response = ${response.body()}")
        handleApiResponse(response)
    }

    // 투두 알림 on/off
    suspend fun updateAlarmStatus(request: AlarmStatusRequest): Result<Unit> = runCatching {
        val response = todoService.updateAlarmStatus(request)
        Log.d("PatchAlarmStatus", "response = ${response.body()}")
        handleApiResponseUnit(response)
    }
}