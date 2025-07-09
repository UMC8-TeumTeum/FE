package com.example.teumteum.ui.calendar

import org.threeten.bp.LocalDate

interface IDateClickListener {
    fun onClickDate(date: LocalDate)
}