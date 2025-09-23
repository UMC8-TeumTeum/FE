package com.example.teumteum.ui.friend

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendTeumRequestBinding
import com.example.teumteum.ui.calendar.FriendMonthlyCalendarFragment
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.friend.adapter.TeumRequestAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
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
import java.util.Locale

@AndroidEntryPoint
class FriendTeumRequestFragment : Fragment() {

    private var _binding: FragmentFriendTeumRequestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private var selectedDate: LocalDate = LocalDate.now()
    private val today: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.now()

    private val headerFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")

    private lateinit var adapter: TeumRequestAdapter
    private lateinit var calendarView: CalendarView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendTeumRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        calendarView = binding.calendarView
        calendarView.isVisible = true

        setupCalendar()
        setupHeader()
        setupWeekdayLabels()
        setupNavigationButtons()
        setupRecyclerView()

        // 어댑터 초기화
        adapter = TeumRequestAdapter()
        binding.requestHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.requestHistoryRecyclerView.adapter = adapter
        binding.requestHistoryRecyclerView.visibility = View.GONE // 처음엔 숨김

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        //  점 LiveData 관찰 → 달력 다시 그림
        viewModel.requestDotDates.observe(viewLifecycleOwner) {
            calendarView.notifyCalendarChanged()
        }

        // 날짜별 요청 리스트 관찰
        viewModel.teumRequestsByDate.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.requestHistoryRecyclerView.visibility = View.GONE
            } else {
                binding.requestHistoryRecyclerView.visibility = View.VISIBLE
                adapter.submitList(list) // TeumRequestAdapter가 TeumRequestDateResult를 바로 받게 수정
            }
        }

        // 최초 표시 월에 대한 API 호출
        fetchDotsForCurrentMonth()

        // 진입 시 오늘 데이터 로드
        onDateSelected(selectedDate)
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
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val tv = container.textView
                tv.text = day.date.dayOfMonth.toString()

                // 기본 스타일 초기화
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

    private fun setupNavigationButtons() {
        binding.calendarPreviousDateIv.setOnClickListener {
            calendarView.smoothScrollToMonth(visibleMonth.minusMonths(1))
        }

        binding.calendarNextDateIv.setOnClickListener {
            calendarView.smoothScrollToMonth(visibleMonth.plusMonths(1))
        }
    }

    private fun setupRecyclerView() {
        adapter = TeumRequestAdapter()
        binding.requestHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.requestHistoryRecyclerView.adapter = adapter
        binding.requestHistoryRecyclerView.isVisible = false
    }

    //  현재 표시 월에 대해 ‘틈 요청 날짜 리스트’ API 호출
    private fun fetchDotsForCurrentMonth() {
        val monthStr = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.KOREA))
        viewModel.fetchRequestTeumDates(monthStr)
    }

    private fun onDateSelected(date: LocalDate) {
        selectedDate = date
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        viewModel.loadTeumRequestsByDate(dateStr)
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
        super.onDestroyView()
        _binding = null
    }

}
