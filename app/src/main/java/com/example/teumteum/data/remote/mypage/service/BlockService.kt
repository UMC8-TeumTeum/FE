package com.example.teumteum.data.remote.mypage.service

import com.example.teumteum.data.remote.mypage.model.BlockedUserResponse
import com.example.teumteum.data.remote.mypage.model.UnBlockResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface BlockService {

    // 차단한 유저 목록 조회
    @GET("/api/blocks")
    suspend fun getBlockedUsers(): Response<BlockedUserResponse>

    // 차단 해제
    @DELETE("/api/blocks/{userId}")
    suspend fun unblockUser(@Path("userId") userId: Long): Response<UnBlockResponse>
}
