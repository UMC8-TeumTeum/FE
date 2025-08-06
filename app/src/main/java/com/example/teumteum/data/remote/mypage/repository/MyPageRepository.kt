package com.example.teumteum.data.remote.mypage.repository

import android.util.Log
import com.example.teumteum.data.remote.mypage.model.MyInfoResponse
import com.example.teumteum.data.remote.mypage.service.MyPageService
import com.example.teumteum.utils.handleApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepository @Inject constructor(
    private val myPageService: MyPageService
){
    //마이페이지 조회
    suspend fun getMyInfo(): Result<MyInfoResponse> = runCatching {
        val response = myPageService.getMyInfo()
        Log.d("MyInfo", "response = ${response.body()}")
        handleApiResponse(response)
    }
}