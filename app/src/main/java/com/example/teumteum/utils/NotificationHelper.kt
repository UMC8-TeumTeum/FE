package com.example.teumteum.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.teumteum.R
import com.example.teumteum.ui.main.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "default"
    private const val CHANNEL_NAME = "일반 알림"

    private fun ensureChannel(context: Context) {
        val ch = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(ch)
    }

    private fun canPostNotifications(context: Context): Boolean {
        // 1) TIRAMISU 이상: 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        // 2) 앱 알림 차단 여부(시스템 설정)
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun show(context: Context, title: String, body: String, extras: Map<String, String>) {
        ensureChannel(context)

        if (!canPostNotifications(context)) {
            // 필요하면 로그/토스트/내부 이벤트 기록
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            extras.forEach { (k, v) -> putExtra(k, v) }
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_teum_mini_logo_sv)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (se: SecurityException) {
            // 사용자가 권한을 거부했거나 정책상 막힌 경우
            // 필요 시 로깅/가이드 처리
        }
    }
}
