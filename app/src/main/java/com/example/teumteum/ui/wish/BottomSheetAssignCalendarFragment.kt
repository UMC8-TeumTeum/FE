package com.example.teumteum.ui.wish

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetAssignCalendarBinding
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class BottomSheetAssignCalendarFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAssignCalendarBinding? = null
    private val binding get() = _binding!!

    private val today: LocalDate = LocalDate.now()
    private var selectedDate: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.now()

    // 헤더는 "yyyy년 M월"
    private val headerFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
    // 서버 요청은 "yyyy-MM-dd"
    private val serverFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")

    // 중복 호출 방지용 캐시: 마지막으로 서버에 요청했던 [시작일, 종료일]
    private var lastRequestedRange: Pair<LocalDate, LocalDate>? = null

    // 일정 있는 날짜들 캐시
    private val eventDates = hashSetOf<LocalDate>()

    // 중복 조회 방지용
    private var lastRequestedMonth: YearMonth? = null

    private lateinit var calendarView: CalendarView

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 부모에서 넘긴 날짜가 있으면 그걸 선택 날짜로 초기화
        val initial = arguments?.getString(ARG_INITIAL_DATE)
        if (!initial.isNullOrBlank()) {
            runCatching { LocalDate.parse(initial) }
                .getOrNull()
                ?.let { selectedDate = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAssignCalendarBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = binding.calendarView

        setupCalendar()
        setupWeekdayLabels()
        setupCalendarNavigation()
        setupObservers()

        visibleMonth = YearMonth.from(selectedDate)
        lastRequestedMonth = visibleMonth

        // 선택된 날짜가 속한 달로 이동
        calendarView.scrollToMonth(visibleMonth)
        setupHeader()

        // 선택 원(보라색) 갱신
        calendarView.post { calendarView.notifyDateChanged(selectedDate) }

        view.post {
            calendarView.findFirstVisibleMonth()?.let { requestForMonth(it) }
        }

        binding.applyBtn.setOnClickListener {
            val displayText = formatWithKoreanWeekday(selectedDate)
            val serverDate = selectedDate.format(serverFormatter)

            parentFragmentManager.setFragmentResult(
                "assign_date_result",
                Bundle().apply {
                    putString("assign_date_display", displayText)
                    putString("assign_date_server", serverDate)
                }
            )
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val screenHeight = resources.displayMetrics.heightPixels
                val desiredHeight = (screenHeight * 0.81).toInt()

                it.layoutParams.height = desiredHeight
                it.requestLayout()

                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = desiredHeight
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun setupHeader() {
        binding.selectedDateTv.text = visibleMonth.format(headerFormatter)
    }

    private fun setupCalendar() {
        // 해당 라이브러리는 캘린더 범위를 무제한으로 설정할 수 없어 일단은 +-50년으로 설정...
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(50) // 50년 전
        val endMonth = currentMonth.plusYears(50)  // 50년 후
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        calendarView.monthScrollListener = { month ->
            visibleMonth = month.yearMonth
            setupHeader()

            // 월 범위로 캘린더 점 데이터 조회
            if (lastRequestedMonth != visibleMonth) {
                lastRequestedMonth = visibleMonth
                requestForMonth(month)
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val tv = container.textView
                val dot = container.dotView

                // dot 간격 설정
                (dot.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                    params.topMargin = dpToPx(3)
                    dot.layoutParams = params
                }

                // 기본 스타일 초기화
                tv.text = day.date.dayOfMonth.toString()
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                // 이번 달 셀만 활성화, out-date는 비활성화/회색
                val isThisMonth = day.position == DayPosition.MonthDate

                // 회색 텍스트 적용
                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isThisMonth) R.color.text_primary else R.color.teumteum_deactive
                    )
                )

                // 일정 점 표시
                dot.visibility = if (eventDates.contains(day.date) && isThisMonth) View.VISIBLE else View.GONE

                // 오늘 표시
                if (day.date == today) {
                    tv.background = circleFill(
                        fillColor = ContextCompat.getColor(requireContext(), R.color.teumteum_gray)
                    )
                }

                // 날짜 선택
                if (day.date == selectedDate && isThisMonth) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.main_1))
                }

                // 클릭으로 선택 처리
                container.view.setOnClickListener {
                    if (!isThisMonth) return@setOnClickListener  // 전환/선택 방지

                    val old = selectedDate
                    selectedDate = day.date

                    // 월 갱신
                    calendarView.notifyDateChanged(old)
                    calendarView.notifyDateChanged(selectedDate)

                    updateHeader()
                    onDateSelected(selectedDate)
                }
            }
        }
    }

    private fun updateHeader() {
        val ym = YearMonth.from(selectedDate)
        visibleMonth = ym
        binding.selectedDateTv.text = ym.format(headerFormatter)
    }

    // 요일 텍스트 설정 (일~토)
    private fun setupWeekdayLabels() {
        val container = binding.calendarWeekdaysRow
        container.removeAllViews()

        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val days = (0..6).map { firstDayOfWeek.plus(it.toLong()) }
        days.forEach { dow ->
            val tv = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                text = weekdayShortKorean(dow)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }
            container.addView(tv)
        }
    }

    private fun weekdayShortKorean(dow: DayOfWeek): String = when (dow) {
        DayOfWeek.SUNDAY -> "일"
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
    }

    // 요일 포함 포맷터
    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    private fun formatWithKoreanWeekday(date: LocalDate): String {
        val base = date.format(displayFormatter) // "yy.MM.dd"
        val dow = when (date.dayOfWeek) {
            DayOfWeek.SUNDAY    -> "일"
            DayOfWeek.MONDAY    -> "월"
            DayOfWeek.TUESDAY   -> "화"
            DayOfWeek.WEDNESDAY -> "수"
            DayOfWeek.THURSDAY  -> "목"
            DayOfWeek.FRIDAY    -> "금"
            DayOfWeek.SATURDAY  -> "토"
        }
        return "$base($dow)"
    }

    // 월 범위 요청 함수 (캘린더 조회용)
    private fun requestForMonth(month: CalendarMonth) {
        val start = month.weekDays.first().first().date
        val end = month.weekDays.last().last().date
        val range = start to end
        if (lastRequestedRange == range) return
        lastRequestedRange = range

        homeViewModel.getCalendar(start.format(serverFormatter), end.format(serverFormatter))
    }

    private fun setupCalendarNavigation() {
        binding.calendarPreviousDateIv.setOnClickListener {
            calendarView.smoothScrollToMonth(visibleMonth.minusMonths(1))
        }

        binding.calendarNextDateIv.setOnClickListener {
            calendarView.smoothScrollToMonth(visibleMonth.plusMonths(1))
        }
    }

    private fun onDateSelected(date: LocalDate) {
        selectedDate = date
    }

    // 채운 동그라미 배경
    private fun circleFill(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // DayView의 뷰 홀더
    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendar_day_tv)
        val dotView: View = view.findViewById(R.id.dot_view)
    }

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월"
        private const val ARG_INITIAL_DATE = "arg_initial_date"

        // 부모에서 날짜 넘겨서 띄우기
        fun newInstance(initialDateServer: String): BottomSheetAssignCalendarFragment {
            return BottomSheetAssignCalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INITIAL_DATE, initialDateServer)
                }
            }
        }
    }

    private fun setupObservers() {
        homeViewModel.calendarData.observe(viewLifecycleOwner) { items ->
            eventDates.clear()
            items.forEach { ev ->
                if (ev.hasSchedule) {
                    runCatching { LocalDate.parse(ev.date) }
                        .getOrNull()
                        ?.let { d -> eventDates += d }
                }
            }
            calendarView.notifyCalendarChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}