package com.umc.teumteum.data.remote.alarm.service


import com.umc.teumteum.data.remote.alarm.model.NotificationsPage
import com.umc.teumteum.data.remote.alarm.model.ReadNotificationResult
import com.umc.teumteum.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationService {

    // 알림 목록 조회 (무한 스크롤)
    @GET("/api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<NotificationsPage>>

    // 알림 읽음 처리
    @PATCH("/api/notifications/{notificationId}/read")
    suspend fun readNotification(@Path("notificationId") notificationId: Long): Response<ApiResponse<ReadNotificationResult>>
}
