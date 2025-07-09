package com.example.teumteum.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.teumteum.ui.calendar.CalendarMode
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.calendar.WeeklyCalendarFragment

class CalendarVPAdapter(
    fragmentActivity: FragmentActivity,
    private val mode: CalendarMode,
    private val onClickListener: IDateClickListener,
): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        return when (mode) {
            CalendarMode.WEEKLY -> WeeklyCalendarFragment.newInstance(position, onClickListener)
            CalendarMode.MONTHLY -> MonthlyCalendarFragment.newInstance(position, onClickListener)
        }
    }
}