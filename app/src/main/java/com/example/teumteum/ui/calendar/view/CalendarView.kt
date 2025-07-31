package com.example.teumteum.ui.calendar.view

import com.example.teumteum.data.remote.calendar.dto.CalendarResult

interface CalendarView {
    fun onGetCalendarSuccess(code: String, calendar: List<CalendarResult>)
    fun onGetCalendarFailure(code: String, message: String? = null)
}