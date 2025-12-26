package com.example.teumteum.data.remote.mypage.service

import com.example.teumteum.data.remote.mypage.model.BlockedUserResponse
import retrofit2.Response
import retrofit2.http.GET

interface BlockService {

    // 차단한 유저 목록 조회
    @GET("/api/blocks")
    suspend fun getBlockedUsers(): Response<BlockedUserResponse>
}
