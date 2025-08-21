package com.example.teumteum.data.remote.alarm.service


import com.example.teumteum.data.remote.alarm.dto.NotificationsPage
import com.example.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationService {

    // 알림 목록 조회 (무한 스크롤)
    @GET("/api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<NotificationsPage>>
}
