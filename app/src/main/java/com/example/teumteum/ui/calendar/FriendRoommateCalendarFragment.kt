package com.example.teumteum.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMonthlyCalendarBinding
import com.example.teumteum.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class FriendRoommateCalendarFragment : Fragment() {

    private lateinit var binding: FragmentMonthlyCalendarBinding
    private lateinit var dateList: List<LocalDate?>
    private var selectedDate: LocalDate? = null
    private var dotDates: List<LocalDate> = emptyList()

    private var position: Int = 0
    private lateinit var onClickListener: IDateClickListener
    private var showDot: Boolean = true
    private var today: LocalDate = LocalDate.now()

    private lateinit var displayMonthDate: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()

        showDot = args.getBoolean("showDot", true)
        displayMonthDate = args.getSerializable("displayDate") as LocalDate
        today = args.getSerializable("today") as LocalDate
        selectedDate = args.getSerializable("selectedDate") as? LocalDate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthlyCalendarBinding.inflate(inflater, container, false)
        setupCalendar(displayMonthDate)
        return binding.root
    }

    private fun setupCalendar(displayMonthDate: LocalDate) {
        val yearMonth = YearMonth.from(displayMonthDate)
        val firstDay = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7

        val tempDateList = mutableListOf<LocalDate?>()

        // 이전 달
        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthLastDay = prevMonth.lengthOfMonth()
        for (i in firstDayOfWeek - 1 downTo 0) {
            tempDateList.add(prevMonth.atDay(prevMonthLastDay - i))
        }

        // 이번 달
        for (day in 1..daysInMonth) {
            tempDateList.add(yearMonth.atDay(day))
        }

        // 다음 달
        val nextMonth = yearMonth.plusMonths(1)
        while (tempDateList.size < 35) {
            tempDateList.add(nextMonth.atDay(tempDateList.size - daysInMonth - firstDayOfWeek + 1))
        }

        this.dateList = tempDateList
        renderCalendar(displayMonthDate)
    }

    private fun renderCalendar(displayMonthDate: LocalDate) {
        val inflater = LayoutInflater.from(context)
        val currentMonth = displayMonthDate.monthValue

        binding.monthlyCalendarGrid.removeAllViews()
        binding.monthlyCalendarGrid.columnCount = 7

        dateList.forEachIndexed { index, date ->
            val cellView = inflater.inflate(R.layout.item_day_cell, binding.monthlyCalendarGrid, false)
            val dayText = cellView.findViewById<TextView>(R.id.day_text)
            val dotView = cellView.findViewById<View>(R.id.dot_view)

            if (date == null) {
                dayText.text = ""
                dayText.background = null
                dayText.setTextColor(resources.getColor(R.color.transparent, null))
                dotView.visibility = View.INVISIBLE
            } else {
                dayText.text = date.dayOfMonth.toString()

                // 오늘 날짜는 회색 원
                if (date.isEqual(today)) {
                    dayText.background = requireContext().getDrawable(R.drawable.bg_circle_gray)
                    dayText.setTextColor(requireContext().getColor(R.color.text_primary))
                }

                if (selectedDate != null && date.isEqual(selectedDate)) {
                    // 선택된 날짜 → 보라색 원
                    dayText.background = requireContext().getDrawable(R.drawable.bg_circle_purple)
                    dayText.setTextColor(requireContext().getColor(R.color.white))
                } else if (date.isEqual(today)) {
                    // 선택이 아니면 오늘 날짜 → 회색 원
                    dayText.background = requireContext().getDrawable(R.drawable.bg_circle_gray)
                    dayText.setTextColor(requireContext().getColor(R.color.white))
                }

                // dot 표시
                dotView.visibility =
                    if (showDot && dotDates.any { it.isEqual(date) }) View.VISIBLE else View.INVISIBLE

                // 날짜 클릭 이벤트
                dayText.setOnClickListener {
                    selectedDate = date
                    renderCalendar(displayMonthDate)
                    onClickListener.onClickDate(date)
                }
            }

            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(index / 7)
                columnSpec = GridLayout.spec(index % 7)
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(0, 6, 1, 6)
            }
            cellView.layoutParams = params
            binding.monthlyCalendarGrid.addView(cellView)
        }
    }

    companion object {
        fun newInstance(
            position: Int,
            onClickListener: IDateClickListener,
            showDot: Boolean = true,
            dotDates: List<LocalDate> = emptyList()
        ): FriendRoommateCalendarFragment {
            return FriendRoommateCalendarFragment().also { fragment ->
                fragment.position = position
                fragment.onClickListener = onClickListener
                fragment.dotDates = dotDates
                fragment.arguments = Bundle().apply {
                    putBoolean("showDot", showDot)
                }
            }
        }
    }
}
