package com.example.teumteum.ui.calendar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

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