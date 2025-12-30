package com.example.teumteum.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.kizitonwose.calendar.view.CalendarView

@SuppressLint("ClickableViewAccessibility")
fun CalendarView.disableScroll() {
    val blockScroll = View.OnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> true // 스크롤 차단
            else -> false // 날짜 선택 가능
        }
    }
    this.setOnTouchListener(blockScroll)
}