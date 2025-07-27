package com.example.teumteum.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.teumteum.R
import java.time.LocalDate

fun saveSelectedDate(context: Context, date: LocalDate) {
    val sharedPref = context.getSharedPreferences("CALENDAR-APP", Context.MODE_PRIVATE)
    sharedPref.edit().putString("SELECTED-DATE", date.toString()).apply()
}

fun getSavedDateOrToday(context: Context): LocalDate {
    val sharedPref = context.getSharedPreferences("CALENDAR-APP", Context.MODE_PRIVATE)
    val saved = sharedPref.getString("SELECTED-DATE", null)
    return saved?.let { LocalDate.parse(it) } ?: LocalDate.now()
}

fun setSelectedDate(context: Context, textView: TextView) {
    textView.background = ContextCompat.getDrawable(context, R.drawable.bg_date_selected)
    textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.main_1))
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
}

fun setTodayStyle(context: Context, textView: TextView) {
    textView.background = ContextCompat.getDrawable(context, R.drawable.bg_date_selected)
    textView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teumteum_gray))
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
}

fun resetDateStyle(context: Context, textView: TextView) {
    textView.background = null
    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
    textView.setTypeface(null, Typeface.NORMAL)
}

fun updateDayUi(context: Context, textView: TextView, date: LocalDate, selectedDate: LocalDate, today: LocalDate) {
    when {
        date == selectedDate -> setSelectedDate(context, textView)
        date == today -> setTodayStyle(context, textView)
        else -> resetDateStyle(context, textView)
    }
}