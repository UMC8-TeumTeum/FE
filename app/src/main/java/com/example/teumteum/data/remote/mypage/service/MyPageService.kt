package com.example.teumteum.data.remote.mypage.service

import com.example.teumteum.data.remote.mypage.model.MyInfoResponse
import com.example.teumteum.data.remote.mypage.model.ProfileUpdateRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.PresignedResponse
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query


interface MyPageService {

    @GET("/api/users/mypage")
    suspend fun getMyInfo(): Response<ApiResponse<MyInfoResponse>>

    @GET("/api/home/todolist")
    suspend fun getRecentTodos(@Query("date") date: String): Response<ApiResponse<List<TodoListResult>>>

    @POST("/api/users/mypage/profile-image/presigned-url")
    suspend fun requestPresignedUrl(@Body request: PresignedRequest): Response<ApiResponse<PresignedResponse>>

    @POST("/api/users/mypage/profile-image")
    suspend fun postProfileImage(@Body request: ProfileImageRequest) : Response<ApiResponse<Unit>>

    @PATCH("/api/users/mypage/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest) : Response<ApiResponse<Unit>>
}