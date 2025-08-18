package com.example.teumteum.application

import android.util.Log
import com.example.teumteum.utils.NotificationHelper
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.messaging.FirebaseMessagingService as BaseFms

@AndroidEntryPoint
class MyFirebaseMessagingService : BaseFms() {

    @Inject lateinit var fcmRepository: com.example.teumteum.data.remote.alarm.FcmRepository
    @Inject lateinit var fcmTokenStore: com.example.teumteum.utils.FcmTokenStore
    @Inject lateinit var tokenProvider: com.example.teumteum.utils.TokenProvider

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "onNewToken: $token")
        fcmTokenStore.save(token)

        tokenProvider.getAccessToken()?.let {
            // 로그인 상태면 서버에 즉시 업로드
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                runCatching { fcmRepository.registerToken(token) }
                    .onFailure { e -> Log.e("FCM", "registerToken failed", e) }
            }
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        // 포그라운드에서 직접 표시
        val data = msg.data
        val title = data["title"] ?: msg.notification?.title ?: "알림"
        val body  = data["content"] ?: msg.notification?.body ?: ""

        NotificationHelper.show(
            context = applicationContext,
            title = title,
            body = body,
            extras = data // 알림 탭 시 인텐트로 전달해 라우팅에 사용
        )
    }
}
