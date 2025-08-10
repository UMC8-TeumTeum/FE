package com.example.teumteum.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMonthlyCalendarBinding
import com.example.teumteum.ui.calendar.viewModel.CalendarViewModel
import com.example.teumteum.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MonthlyCalendarFragment : Fragment() {

    private lateinit var binding: FragmentMonthlyCalendarBinding
    private lateinit var dateList: List<LocalDate?>
    private lateinit var selectedDate: LocalDate
    private var dotDates: List<LocalDate> = emptyList()

    private var position: Int = 0
    private lateinit var onClickListener: IDateClickListener

    private var showDot: Boolean = true

    private val viewModel: CalendarViewModel by activityViewModels()
    private var scheduleMap: Map<String, Boolean> = emptyMap() // "yyyy-MM-dd" -> hasSchedule
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showDot = arguments?.getBoolean("showDot", true) ?: true
        selectedDate = getSavedDateOrToday(requireContext())
    }

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

        setupCalendar(displayMonthDate)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        refreshMonth()
    }

    override fun onResume() {
        super.onResume()
        refreshMonth()
    }

    private fun setupCalendar(displayMonthDate: LocalDate) {
        val yearMonth = YearMonth.from(displayMonthDate)

        val firstDay = yearMonth.atDay(1)
        val lastDay  = yearMonth.atEndOfMonth()

        fun dow0Sun(d: LocalDate) = d.dayOfWeek.value % 7

        // 시작일: 해당 달 1일이 속한 주의 '일요일'
        val startDate = firstDay.minusDays(dow0Sun(firstDay).toLong())

        // 종료일: 해당 달 말일이 속한 주의 '토요일'
        val endDate = lastDay.plusDays((6 - dow0Sun(lastDay)).toLong())

        // startDate ~ endDate 까지 채우기
        val temp = mutableListOf<LocalDate>()
        var cur = startDate
        while (!cur.isAfter(endDate)) {
            temp.add(cur)
            cur = cur.plusDays(1)
        }

        this.dateList = temp
        renderCalendar(displayMonthDate)
    }

    private fun renderCalendar(displayMonthDate: LocalDate) {
        val inflater = LayoutInflater.from(context)
        val today = LocalDate.now()
        val currentMonth = displayMonthDate.monthValue

        binding.monthlyCalendarGrid.removeAllViews()
        binding.monthlyCalendarGrid.columnCount = 7
        binding.monthlyCalendarGrid.rowCount = dateList.size / 7

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

                val key = date.format(dateFormatter)
                val has = scheduleMap[key] == true
                dotView.visibility = if (has) View.VISIBLE else View.INVISIBLE

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
        ): MonthlyCalendarFragment {
            val fragment = MonthlyCalendarFragment()
            fragment.position = position
            fragment.onClickListener = onClickListener
            fragment.dotDates = dotDates
            fragment.arguments = Bundle().apply {
                putBoolean("showDot", showDot)
            }
            return fragment
        }
    }

    private fun setupObservers() {
        viewModel.calendarData.observe(viewLifecycleOwner) { list ->
            // 날짜-일정여부 Map으로 변환
            scheduleMap = list.associate { it.date.take(10) to it.hasSchedule }
            renderCalendar(getDisplayMonthDate())
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun getDisplayMonthDate(): LocalDate {
        val baseDate = getSavedDateOrToday(requireContext())
        val startPosition = Int.MAX_VALUE / 2
        val monthOffset = position - startPosition
        return baseDate.plusMonths(monthOffset.toLong())
    }

    private fun refreshMonth() {
        // 현재 월 달력 범위로 서버 조회
        val startDate = dateList.first()!!.format(dateFormatter)
        val endDate = dateList.last()!!.format(dateFormatter)
        viewModel.getCalendar(startDate, endDate)
    }

}
