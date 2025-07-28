package com.example.teumteum.utils

import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoField

fun combineDateTime(dateTextView: TextView, timeTextView: TextView): String {
        val dateStr = dateTextView.text.toString()
        val timeStr = timeTextView.text.toString()
        val combinedStr = "$dateStr $timeStr"

        val formatter = DateTimeFormatter.ofPattern("M월 d일 '('E')' a h:mm", Locale.KOREAN)
        val parsed = formatter.parse(combinedStr)

        val year = LocalDate.now().year
        val month = parsed.get(ChronoField.MONTH_OF_YEAR)
        val day = parsed.get(ChronoField.DAY_OF_MONTH)
        val time = LocalTime.from(parsed)

        val dateTime = LocalDateTime.of(year, month, day, time.hour, time.minute)
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
}