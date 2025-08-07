package com.example.teumteum.data.remote.mypage.service

import com.example.teumteum.data.remote.mypage.model.MyInfoResponse
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET


interface MyPageService {

    @GET("/api/users/mypage")
    suspend fun getMyInfo(): Response<ApiResponse<MyInfoResponse>>
}