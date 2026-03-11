package com.umc.teumteum.data.remote.alarm.repository

import android.util.Log
import com.umc.teumteum.data.remote.alarm.model.NotificationsPage
import com.umc.teumteum.data.remote.alarm.model.ReadNotificationResult
import com.umc.teumteum.data.remote.alarm.service.NotificationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val service: NotificationService
) {
    suspend fun getNotifications(page: Int, size: Int): Result<NotificationsPage> = runCatching {
        val response = service.getNotifications(page, size)
        Log.d("Notifications", "response = ${response.body()}")

        if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code()}")
        val body = response.body() ?: throw IllegalStateException("Empty body")

        if (!body.isSuccess || body.code != "NOTIFICATION2001") {
            throw IllegalStateException(body.message)
        }
        body.result ?: throw IllegalStateException("Empty result")
    }

    suspend fun readNotification(notificationId: Long): Result<ReadNotificationResult> = runCatching {
        val response = service.readNotification(notificationId)
        Log.d("Notification", "response = ${response.body()}")

        if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code()}")
        val body = response.body() ?: throw IllegalStateException("Empty body")

        if (!body.isSuccess || body.code != "NOTIFICATION2000") {
            throw IllegalStateException(body.message)
        }
        body.result ?: throw IllegalStateException("Empty result")
    }
}