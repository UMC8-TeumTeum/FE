package com.example.teumteum.data.remote.friend.service

import com.example.teumteum.utils.ApiResponse
import com.example.teumteum.data.remote.friend.model.*
import retrofit2.Response  //  이거 추가!
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendService {

    @GET("/api/users/search")
    suspend fun searchUserByNickname(
        @Query("keyword") nickname: String
    ): Response<ApiResponse<List<FriendSearchResult>>>

    @GET("/api/friends/{userId}/profile")
    suspend fun getFriendProfile(
        @Path("userId") userId: Int
    ): Response<ApiResponse<FriendProfileResult>>

    @GET("/api/teums/request/received")
    suspend fun getReceivedTeumRequests(): Response<ApiResponse<TeumReceivedResult>>

    @POST("/api/teums/requests")
    suspend fun sendTeumRequest(@Body body: TeumRequest): Response<ApiResponse<TeumRequestResult>>

    @PATCH("/api/teums/response/{responseId}/status")
    suspend fun patchTeumStatus(
        @Path("responseId") responseId: Int,
        @Body request: TeumStatusRequest
    ): Response<ApiResponse<TeumStatusResult>>

}
