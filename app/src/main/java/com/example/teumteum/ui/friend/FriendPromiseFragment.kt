package com.example.teumteum.ui.friend

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumScheduleDetailResult
import com.example.teumteum.databinding.FragmentFriendPromiseBinding
import com.example.teumteum.ui.alarm.AlarmNavigator
import com.example.teumteum.ui.friend.adapter.TeumEventAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class FriendPromiseFragment : Fragment() {

    private var _binding: FragmentFriendPromiseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()
    private val myHomeViewModel: MyHomeViewModel by viewModels()

    private val today = LocalDate.now()
    private var selectedDate: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.now()

    // 헤더는 "yyyy년 M월"
    private val headerFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
    // 서버 요청은 "yyyy-MM-dd"
    private val serverFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 일정 있는 날짜들 캐시
    private val eventDates = hashSetOf<LocalDate>()
    // 중복 조회 방지용
    private var lastRequestedMonth: YearMonth? = null

    private lateinit var eventAdapter: TeumEventAdapter
    private var lastClickedScheduleId: Int = -1 //  클릭한 스케줄 ID 저장

    private lateinit var calendarView: CalendarView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendPromiseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        // 1) 번들로 전달받은 내 닉네임/프로필 표시
        myHomeViewModel.nickname.observe(viewLifecycleOwner) { myNick ->
            binding.tvName.text = "${myNick ?: "닉네임"}님의"
        }

        // 아직 조회 안 했으면 조회
        if (!myHomeViewModel.isLoaded) {
            myHomeViewModel.getMyInfo()
        }

        // 2) 알림에서 전달된 date로 초기 날짜 세팅
        val argDateStr = arguments?.getString(AlarmNavigator.ARG_TARGET_DATE)
        val initialDate = runCatching {
            argDateStr?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
        }.getOrNull()

        Log.d("FriendPromiseFragment", "ARG_TARGET_DATE=$argDateStr, initialDate=$initialDate")

        if (initialDate != null) {
            selectedDate = initialDate
            visibleMonth = YearMonth.from(initialDate)
        }

        calendarView = binding.calendarView
        calendarView.visibility = View.VISIBLE

        setupCalendar()
        setupHeader()
        setupWeekdayLabels()
        setupRecyclerView()
        setupCalendarNavigation()

        // 최초 가시 월 기준으로 한 번 조회
        lastRequestedMonth = null
        // arguments에서 friendUserId 읽은 뒤에 호출
        fetchDotDates()

        // 알림 화면에서 진입: 틈 날짜 자동 선택
        initialDate?.let { target ->
            val oldSelected = today
            val targetMonth = YearMonth.from(target)

            selectedDate = target
            visibleMonth = targetMonth

            calendarView.scrollToMonth(targetMonth)

            // 달 바인딩
            calendarView.notifyMonthChanged(targetMonth)
            calendarView.notifyDateChanged(oldSelected)
            calendarView.notifyDateChanged(selectedDate)

            updateHeader()
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.scheduledDotDates.observe(viewLifecycleOwner) { dates: List<LocalDate> ->
            eventDates.clear()
            eventDates.addAll(dates)

            // 현재 보이는 달만 부분 리바인딩 (불필요한 전체 갱신 방지)
            calendarView.notifyMonthChanged(visibleMonth)
        }

        viewModel.scheduledTeumList.observe(viewLifecycleOwner) { list ->
            eventAdapter.updateData(list)
        }

        viewModel.teumScheduleDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                val isPast = viewModel.isPastSchedule.value ?: false
                showPromiseDetailBottomSheet(it, lastClickedScheduleId, isPast) //  스케줄 ID 함께 전달
            }
        }

        onDateSelected(selectedDate)
    }

    private fun setupHeader() {
        binding.selectedDateTv.text = visibleMonth.format(headerFormatter)
    }

    private fun setupCalendar() {
        // 해당 라이브러리는 캘린더 범위를 무제한으로 설정할 수 없어 일단은 +-50년으로 설정...
        val currentMonth = YearMonth.from(selectedDate)
        val startMonth = currentMonth.minusYears(50) // 50년 전
        val endMonth = currentMonth.plusYears(50)  // 50년 후
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        visibleMonth = currentMonth

        calendarView.monthScrollListener = { month ->
            visibleMonth = month.yearMonth
            setupHeader()

            // 같은 달로의 반복 호출 방지
            if (lastRequestedMonth != visibleMonth) {
                lastRequestedMonth = visibleMonth
                fetchDotDates()
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

    private fun setupRecyclerView() {
        eventAdapter = TeumEventAdapter(emptyList()) { scheduleId ->
            lastClickedScheduleId = scheduleId //  클릭한 ID 저장
            viewModel.fetchTeumScheduleDetail(scheduleId)
        }
        binding.rvEventList.adapter = eventAdapter
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
        viewModel.fetchScheduledTeumList(formatDateForApi(date))
    }

    private fun fetchDotDates() {
        val monthStr = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.KOREA))
        viewModel.fetchScheduledTeumDates(monthStr)
    }

    //  바텀시트에 detail + scheduleId 넘기기
    private fun showPromiseDetailBottomSheet(
        detail: TeumScheduleDetailResult,
        scheduleId: Int,
        isPast: Boolean
    ) {
        val bottomSheet = PromiseDetailBottomSheet(
            detail = detail,
            scheduleId = scheduleId,
            isPast = isPast
        )
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
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
    }

    // LocalDate -> 서버 전송용 yyyy-MM-dd 문자열
    private fun formatDateForApi(date: LocalDate): String =
        date.format(serverFormatter)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
