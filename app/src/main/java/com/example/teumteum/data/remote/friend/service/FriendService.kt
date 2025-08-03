package com.example.teumteum.data.remote.friend.service

import com.example.teumteum.data.remote.friend.model.FriendProfileResponse
import com.example.teumteum.data.remote.friend.model.FriendSearchResponse
import com.example.teumteum.data.remote.friend.model.TeumReceivedResponse
import com.example.teumteum.data.remote.friend.model.TeumRequest
import com.example.teumteum.data.remote.friend.model.TeumResponse
import retrofit2.Response  // ✅ 올바른 Response import
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query  // ✅ Retrofit용 Query import

interface FriendService {
    @GET("/api/users/search")
    suspend fun searchUserByNickname(@Query("keyword") nickname: String): Response<FriendSearchResponse>

    @GET("/api/friends/{userId}/profile")
    suspend fun getFriendProfile(@Path("userId") userId: Int): Response<FriendProfileResponse>

    @GET("/api/teums/request/received")
    suspend fun getReceivedTeumRequests(): Response<TeumReceivedResponse>

    @POST("/api/teums/requests")
    suspend fun sendTeumRequest(@Body body: TeumRequest): Response<TeumResponse>
}
