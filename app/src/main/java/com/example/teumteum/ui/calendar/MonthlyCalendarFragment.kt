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
import java.time.LocalDate
import java.time.YearMonth

class MonthlyCalendarFragment : Fragment() {

    private lateinit var binding: FragmentMonthlyCalendarBinding
    private lateinit var dateList: List<LocalDate?>
    private lateinit var selectedDate: LocalDate

    private var position: Int = 0
    private lateinit var onClickListener: IDateClickListener

    private var showDot: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthlyCalendarBinding.inflate(inflater, container, false)

        val baseDate = getSavedDateOrToday(requireContext())
        val startPosition = Int.MAX_VALUE / 2
        val monthOffset = position - startPosition
        val displayMonthDate = baseDate.plusMonths(monthOffset.toLong())

        selectedDate = getSavedDateOrToday(requireContext())
        setupCalendar(displayMonthDate)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDot = arguments?.getBoolean("showDot", true) ?: true
    }

    private fun setupCalendar(displayMonthDate: LocalDate) {
        val yearMonth = YearMonth.from(displayMonthDate)
        val firstDay = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstDayOfWeek = firstDay.dayOfWeek.value % 7

        val tempDateList = mutableListOf<LocalDate?>()

        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthLastDay = prevMonth.lengthOfMonth()
        for (i in firstDayOfWeek - 1 downTo 0) {
            tempDateList.add(prevMonth.atDay(prevMonthLastDay - i))
        }

        for (day in 1..daysInMonth) {
            tempDateList.add(yearMonth.atDay(day))
        }

        val nextMonth = yearMonth.plusMonths(1)
        while (tempDateList.size < 35) {
            tempDateList.add(nextMonth.atDay(tempDateList.size - daysInMonth - firstDayOfWeek + 1))
        }

        this.dateList = tempDateList
        renderCalendar(displayMonthDate)
    }

    private fun renderCalendar(displayMonthDate: LocalDate) {
        val inflater = LayoutInflater.from(context)
        val today = LocalDate.now()
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
                updateDayUi(requireContext(), dayText, date, selectedDate, today)

                if (date.monthValue != currentMonth) {
                    dayText.setTextColor(requireContext().getColor(R.color.teumteum_deactive))
                }

                dotView.visibility = if (date.isEqual(today) && showDot) View.VISIBLE else View.INVISIBLE

                dayText.setOnClickListener {
                    selectedDate = date
                    saveSelectedDate(requireContext(), date)
                    renderCalendar(displayMonthDate)
                    onClickListener.onClickDate(date)
                }
            }

            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(index / 7)
                columnSpec = GridLayout.spec(index % 7)
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(20, 8, 29, 8)
            }
            cellView.layoutParams = params

            binding.monthlyCalendarGrid.addView(cellView)
        }
    }

    companion object {
        fun newInstance(position: Int, onClickListener: IDateClickListener, showDot: Boolean = true): MonthlyCalendarFragment {
            val fragment = MonthlyCalendarFragment()
            fragment.position = position
            fragment.onClickListener = onClickListener
            fragment.arguments = Bundle().apply {
                putBoolean("showDot", showDot)
            }
            return fragment
        }
    }
}