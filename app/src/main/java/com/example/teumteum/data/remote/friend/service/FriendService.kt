package com.example.teumteum.data.remote.friend.service

import com.example.teumteum.utils.ApiResponse
import com.example.teumteum.data.remote.friend.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    suspend fun sendTeumRequest(
        @Body body: TeumRequest
    ): Response<ApiResponse<TeumRequestResult>>

    @PATCH("/api/teums/response/{responseId}/status")
    suspend fun patchTeumStatus(
        @Path("responseId") responseId: Int,
        @Body request: TeumStatusRequest
    ): Response<ApiResponse<TeumStatusResult>>

    @POST("/api/friends/{userId}/follow")
    suspend fun followUser(
        @Path("userId") userId: Int
    ): Response<ApiResponse<Unit>>

    @GET("/api/friends/followings")
    suspend fun getFollowings(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<FollowingPageResult>>

    @GET("/api/teums/scheduled/calendar")
    suspend fun getScheduledTeumCalendar(
        @Query("month") month: String
    ): Response<ApiResponse<List<String>>>

    @GET("/api/teums/scheduled")
    suspend fun getScheduledTeums(
        @Query("date") date: String
    ): Response<ApiResponse<List<TeumScheduledResult>>>

    @GET("/api/teums/scheduled/{scheduleId}")
    suspend fun getScheduleDetail(
        @Path("scheduleId") scheduleId: Int
    ): Response<ApiResponse<TeumScheduleDetailResult>>

    @PATCH("/api/teums/scheduled/{scheduleId}/cancel")
    suspend fun cancelTeumSchedule(
        @Path("scheduleId") scheduleId: Int
    ): Response<ApiResponse<CancelTeumResult>>

    @DELETE("/api/friends/{userId}/follow")
    suspend fun unfollow(
        @Path("userId") userId: Long
    ): Response<ApiResponse<String>>

    // 즐겨찾기 설정/해제
    @PATCH("/api/friends/{userId}/favorite")
    suspend fun setFavorite(
        @Path("userId") userId: Int,
        @Body body: FavoriteRequest
    ): Response<ApiResponse<FavoriteResult>>

    // 팔로워 목록 (페이지)
    @GET("/api/friends/followers")
    suspend fun getFollowers(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<FollowerPageResult>>

    // 틈 요청 읽음 처리
    @PATCH("/api/teums/request/{responseId}/read")
    suspend fun readTeumRequest(
        @Path("responseId") responseId: Int
    ): Response<ApiResponse<Int>>

    // 맞팔로우 목록 조회
    @GET("/api/friends/mutuals")
    suspend fun getMutualFriends(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("excludeUserId") excludeUserId: Int? = null
    ): Response<ApiResponse<MutualFriendResult>>

    // 가능한 시간 조회
    @POST("/api/teums/available-time")
    suspend fun getPossibleTime(
        @Body request: PossibleTimeRequest
    ): Response<ApiResponse<PossibleTimeResult>>

    // 틈 요청 재전송
    @POST("/api/teums/request/{parentRequestId}/resend")
    suspend fun resendTeumRequest(
        @Path("parentRequestId") parentRequestId: Int,
        @Body request: ResendTeumRequest
    ): Response<ApiResponse<ResendTeumResult>>

    // 친구의 빈틈 시간 조회
    @GET("/api/friends/{userId}/teum-time")
    suspend fun getFriendTeumTime(
        @Path("userId") userId: Int): Response<ApiResponse<TeumTimeResult>>


}
