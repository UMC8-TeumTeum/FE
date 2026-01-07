package com.example.teumteum.utils

import com.kizitonwose.calendar.view.CalendarView
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