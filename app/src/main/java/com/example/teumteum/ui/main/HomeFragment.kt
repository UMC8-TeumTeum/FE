package com.example.teumteum.ui.main

import android.content.res.ColorStateList
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.AlarmStatusRequest
import com.example.teumteum.databinding.FragmentHomeBinding

import com.example.teumteum.ui.alarm.AlarmFragment
import com.example.teumteum.ui.activity.FillingActivity01Fragment
import com.example.teumteum.ui.todo.adapter.TodoRVAdapter
import com.example.teumteum.ui.todo.TodoRegisterFragment
import com.example.teumteum.ui.wish.WishlistFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.example.teumteum.databinding.ItemClockPageBinding
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.ClockHalf
import com.example.teumteum.ui.clock.ClockVPAdapter
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.main.data.TimeType
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.example.teumteum.utils.applyBlurShadow

import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder

import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.YearMonth

@AndroidEntryPoint
class HomeFragment : Fragment(), IDateClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var monthCalendar: CalendarView
    private lateinit var weekCalendar: WeekCalendarView

    private val today: LocalDate = LocalDate.now()
    private var selectedDate: LocalDate = today

    // 주 이동용 커서
    private var weekCursorDate: LocalDate = selectedDate

    private var isWeeklyMode: Boolean = false
    private var visibleMonth: YearMonth = YearMonth.now()

    private val headerFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

    private lateinit var adapter: TodoRVAdapter
    private var todolistItems: List<TodoListResult> = emptyList()

    private val viewModel: HomeViewModel by activityViewModels()
    private val todoViewModel: TodoViewModel by activityViewModels()
    private val myHomeViewModel: MyHomeViewModel by activityViewModels()

    private var isAM: Boolean = true

    private val TODO_SHEET_TAG = "TodoRegisterSheet"

    private lateinit var clockAdapter: ClockVPAdapter<ItemClockPageBinding>
    private lateinit var clockPageChangeCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        selectedDate = today
        weekCursorDate = selectedDate
        updateHeaderForCurrentMode()

        binding.homeCalendarPreviousDateIv.setOnClickListener {
            if (isWeeklyMode) {
                weekCursorDate = weekCursorDate.minusWeeks(1) // 커서만 이동
                weekCalendar.smoothScrollToDate(weekCursorDate)
//                repaintWeekMonths(weekCursorDate)
                updateHeaderForCurrentMode()
            } else {
                visibleMonth = visibleMonth.minusMonths(1)
                monthCalendar.smoothScrollToMonth(visibleMonth)
                updateHeaderForCurrentMode()
            }
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            if (isWeeklyMode) {
                weekCursorDate = weekCursorDate.plusWeeks(1) // 커서만 이동
                weekCalendar.smoothScrollToDate(weekCursorDate)
//                repaintWeekMonths(weekCursorDate)
                updateHeaderForCurrentMode()
            } else {
                visibleMonth = visibleMonth.plusMonths(1)
                monthCalendar.smoothScrollToMonth(visibleMonth)
                updateHeaderForCurrentMode()
            }
        }

        binding.fabAddIv.setOnClickListener {
            (parentFragmentManager.findFragmentByTag(TODO_SHEET_TAG) as? TodoRegisterFragment)?.let { sheet ->
                if (sheet.dialog?.isShowing == true) return@setOnClickListener
                sheet.dismissAllowingStateLoss() // 인스턴스 정리
            }

            val scheduleList = viewModel.scheduleList.value ?: emptyList()
            val sleepBlocks = scheduleList.filter { it.type == TimeType.SLEEP }

            TodoRegisterFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("sleepBlocks", ArrayList(sleepBlocks))
                    putString("defaultDate", selectedDate.toString())
                }
            }.show(parentFragmentManager, TODO_SHEET_TAG)
        }

        binding.btnLoadWishlistTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.bannerCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FillingActivity01Fragment())
                .addToBackStack(null)
                .commit()
        }

        binding.homeNotificationIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AlarmFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        monthCalendar = binding.homeCalenderView
        weekCalendar = binding.homeWeekCalenderView

        // 해당 라이브러리는 캘린더 범위를 무제한으로 설정할 수 없어 일단은 +-50년으로 설정...
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(50) // 50년 전
        val endMonth = currentMonth.plusYears(50)  // 50년 후
        val firstDayOfWeek = DayOfWeek.SUNDAY

        // 월 달력: 월 범위로 설정
        monthCalendar.setup(startMonth, endMonth, firstDayOfWeek)
        monthCalendar.scrollToMonth(currentMonth)

        // 주 달력: 날짜 범위로 설정
        val today = LocalDate.now()
        val startDate = today.minusYears(50)
        val endDate = today.plusYears(50)
        weekCalendar.setup(startDate, endDate, firstDayOfWeek)
        weekCalendar.scrollToDate(selectedDate)

        // 월 스크롤 리스너 (헤더 갱신용)
        monthCalendar.monthScrollListener = { month ->
            visibleMonth = month.yearMonth
            if (!isWeeklyMode) {
                updateHeaderForCurrentMode()
            }
        }

        // 주 스크롤 리스너 (헤더 갱신용)
        weekCalendar.weekScrollListener = { week ->
            weekCursorDate = week.days.first().date
            updateHeaderForCurrentMode()

            // 주 한 줄만 갱신
            weekCalendar.notifyWeekChanged(weekCursorDate)
        }

        binding.btnHomeWeeklyCalendar.setOnClickListener {
            if (!isWeeklyMode) switchToWeek()
        }

        binding.btnHomeMonthlyCalendar.setOnClickListener {
            if (isWeeklyMode) switchToMonth()
        }

        switchToWeek()

        setupWeekdayLabels(firstDayOfWeek)

        // 월별 DayBinder
        monthCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val tv = container.textView
                tv.text = day.date.dayOfMonth.toString()

                // 기본 스타일 초기화
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                // 이번 달 셀만 활성화, out-date는 비활성화/회색
                val isThisMonth = day.position == DayPosition.MonthDate
//                container.view.isEnabled = isThisMonth
//                container.view.isClickable = isThisMonth

                // 회색 텍스트 적용
                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isThisMonth) R.color.text_primary else R.color.teumteum_deactive
                    )
                )

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

                    // 월/주 양쪽 동기 갱신
                    monthCalendar.notifyDateChanged(old)
                    monthCalendar.notifyDateChanged(selectedDate)
                    weekCalendar.notifyDateChanged(old)
                    weekCalendar.notifyDateChanged(selectedDate)

                    // 주간 뷰도 해당 날짜 주로 맞춰두기
                    weekCursorDate = selectedDate
                    weekCalendar.scrollToDate(selectedDate)

                    updateHeaderForCurrentMode()
                    onDateSelected(selectedDate)
                }
            }
        }

        // 주별 DayBinder
        weekCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {

            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: WeekDay) {
                val tv = container.textView
                tv.text = day.date.dayOfMonth.toString()
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                val isInactive = isInactiveWeekly(day.date)
                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isInactive) R.color.teumteum_deactive else R.color.text_primary
                    )
                )

                if (day.date == today) {
                    tv.background = circleFill(
                        ContextCompat.getColor(requireContext(), R.color.teumteum_gray)
                    )
                }
                if (day.date == selectedDate) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.main_1))
                }

                container.view.setOnClickListener {
                    if (day.date == selectedDate) return@setOnClickListener

                    val old = selectedDate
                    selectedDate = day.date
                    weekCursorDate = day.date
                    visibleMonth = YearMonth.from(selectedDate)

                    weekCalendar.notifyDateChanged(old)
                    weekCalendar.notifyDateChanged(selectedDate)
                    monthCalendar.notifyDateChanged(old)
                    monthCalendar.notifyDateChanged(selectedDate)

                    // 클릭한 날짜의 월로 헤더 직접 갱신
                    val newHeaderMonth = YearMonth.from(selectedDate)
                    binding.homeSelectedDateTv.text = newHeaderMonth.format(headerFormatter)

                    onDateSelected(selectedDate)
                }
            }
        }

        adapter = TodoRVAdapter(parentFragmentManager,
            todolistItems,
            { id, toActive ->
                val status = if (toActive) AlarmStatus.ACTIVE else AlarmStatus.INACTIVE
                todoViewModel.patchAlarmStatus(
                    AlarmStatusRequest(
                        todoId = id,
                        alarmStatus = status
                    )
                )
            })
        binding.todolistRv.adapter = adapter

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        viewModel.getTodayScheduleIfNeeded()
        viewModel.getTeumTime()
        viewModel.refreshTodaySchedule()

        binding.fabAddIv.post {
            val binding = _binding ?: return@post
            applyBlurShadow(
                sourceView = binding.fabAddIv,
                targetImageView = binding.fabShadowIv
            )
        }

        setupClockPager()

        binding.amPmTv.setOnClickListener {
            val amPos = clockAdapter.positionOf(ClockHalf.AM)
            val pmPos = clockAdapter.positionOf(ClockHalf.PM)
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

        // 투두 등록 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_register_home", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            viewModel.getTeumTime()
            refreshTodolist()
        }

        // 투두 수정 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_edit_home", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            viewModel.getTeumTime()
            refreshTodolist()
        }

        // 투두 삭제 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_delete_home", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            viewModel.getTeumTime()
            refreshTodolist()
        }

        todoViewModel.getTodoList(date)
        myHomeViewModel.getMyInfo()

        viewModel.scheduleList.observe(viewLifecycleOwner) {
            clockAdapter.refreshAll()
            val amPos = clockAdapter.positionOf(ClockHalf.AM)
            isAM = (binding.clockPager.currentItem == amPos)
            updateIndicator(isAM)
        }

        // 누적 시간 표시
        viewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        viewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        viewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.VISIBLE
    }

    private fun weekContains(target: LocalDate, weekStart: LocalDate): Boolean {
        val weekEnd = weekStart.plusDays(6)
        return !target.isBefore(weekStart) && !target.isAfter(weekEnd)
    }

    // 주별 날짜 헤더
    private fun headerMonthOfDisplayedWeek(): YearMonth {
        val weekStart = startOfWeekSunday(weekCursorDate)
        val weekEnd = weekStart.plusDays(6)

        val startYm = YearMonth.from(weekStart)
        val endYm = YearMonth.from(weekEnd)
        val todayYm = YearMonth.from(today)

        // 1. 주 안에 같은 달이 있는 경우
        if (startYm == endYm) return startYm

        // 2. 주 안에 두 달이 겹치는 경우 or 토요일 기준
        return if (todayYm == startYm) startYm else endYm
    }

    private fun startOfWeekSunday(d: LocalDate): LocalDate {
        val sun0 = d.dayOfWeek.value % 7
        return d.minusDays(sun0.toLong())
    }

    //주 시작 (일요일)
    private fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value % 7).toLong())

    // 주 끝 (토요일)
    private fun weekEnd(d: LocalDate): LocalDate = weekStart(d).plusDays(6)

    // 이 'day'가 속한 주에서, 회색 비교에 쓸 '기준 월'
    private fun baseMonthForDay(d: LocalDate): YearMonth {
        val startYearMonth = YearMonth.from(weekStart(d))
        val endYearMonth = YearMonth.from(weekEnd(d))
        val todayYearMonth = YearMonth.from(today)

        return when {
            startYearMonth == endYearMonth -> startYearMonth // 한 달짜리 주
            startYearMonth == todayYearMonth || endYearMonth == todayYearMonth -> todayYearMonth // 경계 주 + 오늘의 달 포함 → 오늘의 달
            else -> endYearMonth // 그 외 경계 주 → 토요일 달
        }
    }

    // 주별 회색 여부
    private fun isInactiveWeekly(day: LocalDate): Boolean {
        return YearMonth.from(day) != baseMonthForDay(day)
    }

    private fun updateHeaderForCurrentMode() {
        val ym = if (isWeeklyMode) headerMonthOfDisplayedWeek() else visibleMonth
        binding.homeSelectedDateTv.text = ym.format(headerFormatter)
    }

    // 요일 텍스트 설정 (일~토)
    private fun setupWeekdayLabels(firstDayOfWeek: DayOfWeek) {
        val container = binding.calendarWeekdaysRow
        container.removeAllViews()

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

    private fun setupClockPager() {
        clockAdapter = ClockVPAdapter(
            inflate = ItemClockPageBinding::inflate,
            chartOf = { it.clockChart },
            onBindPage = { chart, half ->
                ChartUtils.setupPieChart(chart)
                val sleepBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)
                chart.renderer = IconPieChartRenderer(chart, chart.animator, chart.viewPortHandler, sleepBitmap)

                // AM/PM 데이터 바인딩
                val blocks = viewModel.scheduleList.value.orEmpty()
                val halfBlocks = ChartUtils.splitAndFillTimeBlocks(blocks, half == ClockHalf.AM)
                ChartUtils.setTimePieChartData(requireContext(), chart, halfBlocks)
            }
        )

        binding.clockPager.adapter = clockAdapter
        binding.clockPager.offscreenPageLimit = 1

        val amPos = clockAdapter.positionOf(ClockHalf.AM) // 0
        val pmPos = clockAdapter.positionOf(ClockHalf.PM) // 1

        binding.clockPager.setCurrentItem(amPos, false)

        binding.clockPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicator(position == amPos)
            }
        })

        binding.amPmTv.setOnClickListener {
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

        updateIndicator(binding.clockPager.currentItem == amPos)
    }

    private fun switchToMonth() {
        isWeeklyMode = false

        // 주 → 월 토글
        binding.homeWeekCalenderView.visibility = View.GONE
        binding.homeCalenderView.visibility = View.VISIBLE

        // 선택일의 달로 이동 + 헤더 갱신
        visibleMonth = YearMonth.from(selectedDate)
        monthCalendar.scrollToMonth(visibleMonth)
        updateHeaderForCurrentMode()

        updateCalendarToggle(false)
        monthCalendar.notifyCalendarChanged()
    }

    private fun switchToWeek() {
        isWeeklyMode = true

        // 월 → 주 토글
        binding.homeCalenderView.visibility = View.GONE
        binding.homeWeekCalenderView.visibility = View.VISIBLE

        weekCursorDate = selectedDate
        weekCalendar.scrollToDate(weekCursorDate) // 선택일이 포함된 주로 이동
        updateHeaderForCurrentMode()

        updateCalendarToggle(true)
        weekCalendar.notifyCalendarChanged()
    }

    // 주간/월간 버튼 이미지 변경
    private fun updateCalendarToggle(isWeekly: Boolean) {
        if (isWeekly) {
            binding.btnHomeWeeklyCalendar.apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.text_primary))
                setTextColor(ContextCompat.getColor(context, R.color.white))
                strokeWidth = 0
            }
            binding.btnHomeMonthlyCalendar.apply {
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                setTextColor(ContextCompat.getColor(context, R.color.teumteum_deactive))
                strokeWidth = 2
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teumteum_line))
            }
        } else {
            binding.btnHomeMonthlyCalendar.apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.text_primary))
                setTextColor(ContextCompat.getColor(context, R.color.white))
                strokeWidth = 0
            }
            binding.btnHomeWeeklyCalendar.apply {
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                setTextColor(ContextCompat.getColor(context, R.color.teumteum_deactive))
                strokeWidth = 2
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teumteum_line))
            }
        }
    }

    private fun saveSelectedDate(date: LocalDate) {
        val sharedPref = requireContext().getSharedPreferences("CALENDAR-APP", AppCompatActivity.MODE_PRIVATE)
        sharedPref.edit().putString("SELECTED-DATE", date.toString()).apply()
    }

    override fun onClickDate(date: LocalDate) {
        val old = selectedDate
        selectedDate = date
        saveSelectedDate(date)

        // 두 달력 모두 갱신
        monthCalendar.notifyDateChanged(old)
        monthCalendar.notifyDateChanged(selectedDate)
        weekCalendar.notifyDateChanged(old)
        weekCalendar.notifyDateChanged(selectedDate)

        if (isWeeklyMode) {
            weekCursorDate = date
            weekCalendar.scrollToDate(weekCursorDate)
        } else {
            visibleMonth = YearMonth.from(selectedDate)
            monthCalendar.scrollToMonth(visibleMonth)
        }

        updateHeaderForCurrentMode()
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        todoViewModel.getTodoList(dateStr)
    }

    private fun onDateSelected(date: LocalDate) {
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        todoViewModel.getTodoList(dateStr)
    }

    private fun updateIndicator(isAM: Boolean) {
        val leftView = binding.leftView
        val rightView = binding.rightView

        if (isAM) {
            leftView.layoutParams.width = dpToPx(28)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar)

            rightView.layoutParams.width = dpToPx(4)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            binding.amPmTv.text = "AM"
        } else {
            leftView.layoutParams.width = dpToPx(4)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            rightView.layoutParams.width = dpToPx(28)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar)

            binding.amPmTv.text = "PM"
        }

        leftView.requestLayout()
        rightView.requestLayout()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // 지금까지 채운 빈틈시간 표시
    private fun updateTeumTime() {
        val days = viewModel.teumTimeDays.value ?: 0
        val hours = viewModel.teumTimeHours.value ?: 0
        val minutes = viewModel.teumTimeMinutes.value ?: 0
        binding.homeContentTimeTv.text = "${days}일 ${hours}시간 ${minutes}분"
    }

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월"
    }

    private fun refreshTodolist() {
        val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        todoViewModel.getTodoList(dateStr)
    }

    private fun setupObservers() {
        todoViewModel.todolistItems.observe(viewLifecycleOwner) { itemList ->
            todolistItems = itemList

            if (itemList.isEmpty()) {
                binding.todolistRv.visibility = View.GONE
            } else {
                binding.todolistRv.visibility = View.VISIBLE
                adapter.updateList(itemList)
            }
        }
    }

    // 채운 동그라미 배경
    private fun circleFill(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
        }
    }

    // DayView의 뷰 홀더
    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendar_day_tv)
    }

    override fun onDestroyView() {
        _binding?.let { b ->
            runCatching {
                if (this::clockPageChangeCallback.isInitialized) {
                    b.clockPager.unregisterOnPageChangeCallback(clockPageChangeCallback)
                }
            }
        }
        _binding = null
        super.onDestroyView()
    }
}
