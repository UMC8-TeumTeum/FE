package com.umc.teumteum.utils

import com.kizitonwose.calendar.view.CalendarView
import java.time.DayOfWeek
import java.time.YearMonth

// 캘린더 월 이동 공통 로직
fun moveCalendarMonth(
    monthStateRef: YearMonth,
    delta: Long,
    calendarView: CalendarView,
): YearMonth {
    val newMonth = monthStateRef.plusMonths(delta)
    calendarView.smoothScrollToMonth(newMonth)
    return newMonth
}

fun weekdayShortKorean(dow: DayOfWeek): String = when (dow) {
    DayOfWeek.SUNDAY -> "일"
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
}