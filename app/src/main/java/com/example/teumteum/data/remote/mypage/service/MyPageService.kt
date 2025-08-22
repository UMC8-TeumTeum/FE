package com.example.teumteum.data.remote.mypage.service

import com.example.teumteum.data.remote.friend.model.PublicTodoResult
import com.example.teumteum.data.remote.mypage.model.MyInfoResponse
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface MyPageService {

    @GET("/api/users/mypage")
    suspend fun getMyInfo(): Response<ApiResponse<MyInfoResponse>>

    @GET("/api/home/todolist")
    suspend fun getRecentTodos(@Query("date") date: String): Response<ApiResponse<List<TodoListResult>>>
}