package com.umc.teumteum.ui.friend

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumScheduleDetailResult
import com.umc.teumteum.databinding.FragmentFriendPromiseBinding
import com.umc.teumteum.ui.friend.adapter.TeumEventAdapter
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.umc.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.umc.teumteum.utils.weekdayShortKorean
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class FriendPromiseFragment : Fragment() {

    private var _binding: FragmentFriendPromiseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()
    private val myHomeViewModel: MyHomeViewModel by activityViewModels()

    private val today = LocalDate.now()
    private var selectedDate: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.now()

    private val headerFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
    private val serverFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val eventDates = hashSetOf<LocalDate>()
    private var lastRequestedMonth: YearMonth? = null

    private lateinit var eventAdapter: TeumEventAdapter
    private var lastClickedScheduleId: Int = -1

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

        myHomeViewModel.nickname.observe(viewLifecycleOwner) { myNick ->
            binding.tvName.text = "${myNick ?: "닉네임"}님의"
        }

        calendarView = binding.calendarView
        calendarView.visibility = View.VISIBLE

        setupRecyclerView()
        setupCalendar()
        setupHeader()
        setupWeekdayLabels()
        setupCalendarNavigation()

        visibleMonth = YearMonth.now()
        lastRequestedMonth = null
        fetchDotDates()

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.scheduledDotDates.observe(viewLifecycleOwner) { dates ->
            eventDates.clear()
            eventDates.addAll(dates)
            calendarView.notifyMonthChanged(visibleMonth)
        }

        // 리스트 데이터 관찰: 비면 숨기고, 있으면 보이기
        viewModel.scheduledTeumList.observe(viewLifecycleOwner) { list ->
            eventAdapter.updateData(list)
            binding.rvEventList.visibility = if (list.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.teumScheduleDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                val isPast = viewModel.isPastSchedule.value ?: false
                showPromiseDetailBottomSheet(it, lastClickedScheduleId, isPast)
            }
        }

        // 진입 시 오늘 데이터 로드
        onDateSelected(selectedDate)
    }

    private fun setupHeader() {
        binding.selectedDateTv.text = visibleMonth.format(headerFormatter)
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(50)
        val endMonth = currentMonth.plusYears(50)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        visibleMonth = currentMonth

        calendarView.monthScrollListener = { month ->
            visibleMonth = month.yearMonth
            setupHeader()

            binding.rvEventList.visibility = View.GONE
            eventAdapter.updateData(emptyList())

            // dot 날짜 조회
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

                (dot.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                    params.topMargin = dpToPx(3)
                    dot.layoutParams = params
                }

                tv.text = day.date.dayOfMonth.toString()
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                val isThisMonth = day.position == DayPosition.MonthDate

                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isThisMonth) R.color.text_primary else R.color.teumteum_deactive
                    )
                )

                dot.visibility =
                    if (eventDates.contains(day.date) && isThisMonth) View.VISIBLE else View.GONE

                if (day.date == today) {
                    tv.background = circleFill(
                        fillColor = ContextCompat.getColor(requireContext(), R.color.teumteum_gray)
                    )
                }

                if (day.date == selectedDate && isThisMonth) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    tv.background =
                        circleFill(ContextCompat.getColor(requireContext(), R.color.main_1))
                }

                container.view.setOnClickListener {
                    if (!isThisMonth) return@setOnClickListener

                    val old = selectedDate
                    selectedDate = day.date

                    calendarView.notifyDateChanged(old)
                    calendarView.notifyDateChanged(selectedDate)

                    updateHeader()

                    // 날짜 선택하면 리스트 다시 보이게 (데이터는 observe에서 세팅됨)
                    binding.rvEventList.visibility = View.VISIBLE
                    onDateSelected(selectedDate)
                }
            }
        }
    }

    private fun updateHeader() {
        val ym = YearMonth.from(selectedDate)
        binding.selectedDateTv.text = ym.format(headerFormatter)
    }

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

    private fun setupRecyclerView() {
        eventAdapter = TeumEventAdapter(emptyList()) { scheduleId ->
            lastClickedScheduleId = scheduleId
            viewModel.fetchTeumScheduleDetail(scheduleId)
        }
        binding.rvEventList.adapter = eventAdapter

        binding.rvEventList.isNestedScrollingEnabled = false
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

    private fun showPromiseDetailBottomSheet(
        detail: TeumScheduleDetailResult,
        scheduleId: Int,
        isPast: Boolean
    ) {
        val bottomSheet = BottomSheetPromiseDetailFragment(
            detail = detail,
            scheduleId = scheduleId,
            isPast = isPast
        )
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }

    private fun circleFill(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendar_day_tv)
        val dotView: View = view.findViewById(R.id.dot_view)
    }

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월"
    }

    private fun formatDateForApi(date: LocalDate): String =
        date.format(serverFormatter)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
