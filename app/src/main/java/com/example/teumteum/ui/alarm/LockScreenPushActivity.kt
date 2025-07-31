package com.example.teumteum.ui.lock

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.teumteum.data.PushAlarmItem
import com.example.teumteum.databinding.ActivityLockScreenPushBinding
import com.example.teumteum.ui.alarm.PushAlarmRVAdapter
import java.text.SimpleDateFormat
import java.util.*

class LockScreenPushActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenPushBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위에 Activity 띄우기
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        binding = ActivityLockScreenPushBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 시간과 날짜 표시
        val now = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)
        val dateFormat = SimpleDateFormat("M월 d일 E요일", Locale.KOREA)

        binding.timeTv.text = timeFormat.format(now)
        binding.dateTv.text = dateFormat.format(now)

        // 알림 리스트 표시
        val dummyList = mutableListOf(
            PushAlarmItem(1, "30분 뒤 투두가 시작돼요", null, "3분"),
            PushAlarmItem(2, "오늘의 투두를 알려드려요", "09:00 틈틈 회의\n13:00 치과 예약", "3시간"),
            PushAlarmItem(3, "친구와의 요청에 새로운 소식이 있어요", null, "3분"),
            PushAlarmItem(4, "새로운 팔로워가 있어요", null, "3분")
        )

        binding.pushalarmRv.adapter = PushAlarmRVAdapter(dummyList)
    }
}