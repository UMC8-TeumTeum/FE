package com.example.teumteum.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentWeeklyCalendarBinding
import com.example.teumteum.ui.calendar.viewModel.CalendarViewModel
import com.example.teumteum.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class WeeklyCalendarFragment : Fragment() {

    private lateinit var binding: FragmentWeeklyCalendarBinding
    private lateinit var textViewList: List<TextView>
    private lateinit var dotViewList: List<View>
    private lateinit var dates: List<LocalDate>

    private var position: Int = 0
    private lateinit var onClickListener: IDateClickListener
    private val todayPosition = Int.MAX_VALUE / 2

    private val viewModel: CalendarViewModel by activityViewModels()
    private var scheduleMap: Map<String, Boolean> = emptyMap() // "yyyy-MM-dd" -> hasSchedule
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeeklyCalendarBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newDate = calculateNewDate()
        calculateDatesOfWeek(newDate)

        val baseMonth = newDate.monthValue
        setOneWeekDateIntoTextView(baseMonth)

        ensureSelectedDate()
        resetUi(baseMonth)

        // 주간 범위로 캘린더 호출
        val startDate = dates.first().format(dateFormatter)
        val endDate = dates.last().format(dateFormatter)
        viewModel.getCalendar(startDate, endDate)

        setupObservers()
        refreshWeek()
    }

    override fun onResume() {
        super.onResume()
        resetUi(calculateNewDate().monthValue)

        // 투두 등록 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_register_calendar", viewLifecycleOwner) { _, _ ->
            refreshWeek()
        }

        // 투두 삭제 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_delete_calendar", viewLifecycleOwner) { _, _ ->
            refreshWeek()
        }
    }

    private fun ensureSelectedDate() {
        if (!firstShownInProcess) {
            saveSelectedDate(requireContext(), LocalDate.now())
            firstShownInProcess = true
        }
    }

    override fun onPause() {
        super.onPause()
        resetUi(calculateNewDate().monthValue)
    }

    private fun initViews() {
        with(binding) {
            textViewList = listOf(tv1, tv2, tv3, tv4, tv5, tv6, tv7)
            dotViewList = listOf(dot1, dot2, dot3, dot4, dot5, dot6, dot7)
        }
    }

    private fun calculateNewDate(): LocalDate {
        val curDate = LocalDate.now()
        return when {
            position < todayPosition -> curDate.minusDays(((todayPosition - position) * 7).toLong())
            position > todayPosition -> curDate.plusDays(((position - todayPosition) * 7).toLong())
            else -> curDate
        }
    }

    private fun calculateDatesOfWeek(today: LocalDate) {
        val dayOfWeek = today.dayOfWeek.value % 7
        val startOfWeek = today.minusDays(dayOfWeek.toLong())
        dates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    private fun setOneWeekDateIntoTextView(baseMonth: Int) {
        for (i in textViewList.indices) {
            val date = dates[i]
            val textView = textViewList[i]

            textView.text = date.dayOfMonth.toString()

            textView.setTextColor(
                if (date.monthValue == baseMonth)
                    requireContext().getColor(R.color.text_primary)
                else
                    requireContext().getColor(R.color.teumteum_deactive)
            )

            textView.setOnClickListener {
                // 선택 날짜를 저장
                saveSelectedDate(requireContext(), date)

                // 저장된 값 기준으로 전체 UI 리셋
                resetUi(baseMonth)

                onClickListener.onClickDate(date)
            }
        }
    }

    // 스타일 관리
    private fun resetUi(baseMonth: Int) {
        val today = LocalDate.now()
        val selected = getSavedDateOrToday(requireContext())

        for (i in textViewList.indices) {
            val date = dates[i]
            val textView = textViewList[i]

            when {
                date == selected -> setSelectedDate(requireContext(), textView) // 선택된 날짜 우선
                date == today -> setTodayStyle(requireContext(), textView)      // 선택된 날짜가 아니면 오늘 표시
                else -> resetDateStyle(requireContext(), textView)
            }
        }
    }

    companion object {
        // 프로세스가 재시작되면 false로 초기화
        private var firstShownInProcess: Boolean = false

        fun newInstance(
            position: Int,
            onClickListener: IDateClickListener
        ): WeeklyCalendarFragment {
            val fragment = WeeklyCalendarFragment()
            fragment.position = position
            fragment.onClickListener = onClickListener
            return fragment
        }
    }

    private fun setupObservers() {
        viewModel.calendarData.observe(viewLifecycleOwner) { list ->

            // 일정 리스트 관찰 → 날짜-일정여부 맵으로 변환 → 도트 적용
            scheduleMap = list.associate { it.date to it.hasSchedule }
            applyDots()
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Log.d("WEEKLY_CALENDAR_FRAGMENT", it)
            }
        }
    }

    private fun applyDots() {
        for (i in dates.indices) {
            val key = dates[i].format(dateFormatter)
            val has = scheduleMap[key] == true
            dotViewList[i].visibility = if (has) View.VISIBLE else View.GONE
        }
    }

    private fun refreshWeek() {
        val newDate = calculateNewDate()
        calculateDatesOfWeek(newDate)

        val baseMonth = newDate.monthValue
        setOneWeekDateIntoTextView(baseMonth)

        resetUi(baseMonth)

        val startDate = dates.first().format(dateFormatter)
        val endDate = dates.last().format(dateFormatter)

        // 해당 주 데이터 재조회
        viewModel.getCalendar(startDate, endDate)

        applyDots()
    }
}