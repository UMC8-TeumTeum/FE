package com.umc.teumteum.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.umc.teumteum.ui.alarm.LockScreenPushActivity

class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_ON) {
            Log.d("ScreenOnReceiver", "화면 켜짐 감지됨. 잠금화면 Activity 실행")
            val i = Intent(context, LockScreenPushActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(i)
        }
    }
}