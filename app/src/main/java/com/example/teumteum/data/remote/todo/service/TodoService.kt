package com.example.teumteum.data.remote.todo.service

import com.example.teumteum.data.remote.todo.model.EditTodoRequest
import com.example.teumteum.data.remote.todo.model.GetOnboardingReminders
import com.example.teumteum.data.remote.todo.model.GetTodoResult
import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.data.remote.todo.model.TodoResult
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TodoService {
    @POST("/api/home/todo")
    suspend fun registerTodo(@Body request: RegisterTodoRequest): Response<ApiResponse<TodoResult>>

    @GET("/api/home/todolist")
    suspend fun getTodoList(@Query("date") date: String): Response<ApiResponse<List<TodoListResult>>>

    @GET("/api/home/todo/{todoId}")
    suspend fun getTodo(@Path("todoId") todoId: Long): Response<ApiResponse<GetTodoResult>>

    @PUT("/api/home/todo/{todoId}")
    suspend fun editTodo(@Path("todoId") todoId: Long, @Body request: EditTodoRequest): Response<ApiResponse<TodoResult>>

    @DELETE("/api/home/todo/{todoId}")
    suspend fun deleteTodo(@Path("todoId") todoId: Long): Response<ApiResponse<Unit>>

    @GET("/api/home/todo/user-reminds")
    suspend fun getOnboardingReminders(): Response<ApiResponse<GetOnboardingReminders>>
}