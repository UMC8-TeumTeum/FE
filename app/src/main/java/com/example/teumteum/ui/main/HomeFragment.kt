package com.example.teumteum.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.AlarmStatusRequest
import com.example.teumteum.databinding.FragmentHomeBinding
import com.example.teumteum.ui.calendar.CalendarMode

import com.example.teumteum.ui.alarm.AlarmFragment
import com.example.teumteum.ui.calendar.CalendarVPAdapter
import com.example.teumteum.ui.activity.FillingActivity01Fragment
import com.example.teumteum.ui.todo.adapter.TodoRVAdapter
import com.example.teumteum.ui.todo.TodoRegisterFragment
import com.example.teumteum.ui.wish.WishlistFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.ClockHalf
import com.example.teumteum.ui.clock.ClockVPAdapter
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.main.data.TimeType
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.example.teumteum.utils.applyBlurShadow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), IDateClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val today: LocalDate = LocalDate.now()
    private lateinit var selectedDate: LocalDate

    private lateinit var adapter: TodoRVAdapter
    private var todolistItems: List<TodoListResult> = emptyList()

    private val viewModel: HomeViewModel by activityViewModels()
    private val todoViewModel: TodoViewModel by activityViewModels()
    private val myHomeViewModel: MyHomeViewModel by activityViewModels()

    private var isAM: Boolean = true

    private val TODO_SHEET_TAG = "TodoRegisterSheet"

    private lateinit var clockAdapter: ClockVPAdapter

    // 콜백 필드
    private lateinit var clockPageChangeCallback: ViewPager2.OnPageChangeCallback
    private lateinit var weeklyPageChangeCallback: ViewPager2.OnPageChangeCallback
    private lateinit var monthlyPageChangeCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        selectedDate = today

        binding.homeCalendarPreviousDateIv.setOnClickListener {
            if (binding.homeWeeklyCalendarWeekVp.isVisible) {
                val currentItem = binding.homeWeeklyCalendarWeekVp.currentItem
                binding.homeWeeklyCalendarWeekVp.setCurrentItem(currentItem - 1, true)
            } else {
                val currentItem = binding.homeMonthlyCalendarMonthVp.currentItem
                binding.homeMonthlyCalendarMonthVp.setCurrentItem(currentItem - 1, true)
            }
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            if (binding.homeWeeklyCalendarWeekVp.isVisible) {
                val currentItem = binding.homeWeeklyCalendarWeekVp.currentItem
                binding.homeWeeklyCalendarWeekVp.setCurrentItem(currentItem + 1, true)
            } else {
                val currentItem = binding.homeMonthlyCalendarMonthVp.currentItem
                binding.homeMonthlyCalendarMonthVp.setCurrentItem(currentItem + 1, true)
            }
        }

        binding.fabAddIv.setOnClickListener {
            (parentFragmentManager.findFragmentByTag(TODO_SHEET_TAG) as? TodoRegisterFragment)?.let { sheet ->
                if (sheet.dialog?.isShowing == true) return@setOnClickListener
                sheet.dismissAllowingStateLoss() // 인스턴스 정리
            }

            TodoRegisterFragment().show(parentFragmentManager, TODO_SHEET_TAG)
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

        setWeeklyCalendarViewPager()
        setMonthlyCalendarViewPager()
        setCalendarModeToggleListeners()
        showWeeklyCalendar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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
            refreshTodolist()
        }

        // 투두 수정 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_edit_home", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            refreshTodolist()
        }

        // 투두 삭제 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_delete_home", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
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

    private inline fun withBinding(block: FragmentHomeBinding.() -> Unit) {
        val b = _binding ?: return
        block(b)
    }

    private fun setupClockPager() = withBinding {
        clockAdapter = ClockVPAdapter { chart, half ->
            ChartUtils.setupPieChart(chart)
            val sleepBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)
            chart.renderer = IconPieChartRenderer(chart, chart.animator, chart.viewPortHandler, sleepBitmap)

            // AM/PM 데이터 바인딩
            val blocks = viewModel.scheduleList.value.orEmpty()
            val halfBlocks = ChartUtils.splitAndFillTimeBlocks(blocks, half == ClockHalf.AM)
            ChartUtils.setTimePieChartData(requireContext(), chart, halfBlocks)
        }

        binding.clockPager.adapter = clockAdapter
        binding.clockPager.offscreenPageLimit = 1

        val amPos = clockAdapter.positionOf(ClockHalf.AM) // 0
        val pmPos = clockAdapter.positionOf(ClockHalf.PM) // 1

        binding.clockPager.setCurrentItem(amPos, false)

        clockPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val b = _binding ?: return
                b.apply { updateIndicator(position == amPos) }
            }
        }
        clockPager.registerOnPageChangeCallback(clockPageChangeCallback)

        amPmTv.setOnClickListener {
            val b = _binding ?: return@setOnClickListener
            val next = if (b.clockPager.currentItem == amPos) pmPos else amPos
            b.clockPager.setCurrentItem(next, true)
        }

        updateIndicator(binding.clockPager.currentItem == amPos)
    }

    // 주간 달력 연결
    private fun setWeeklyCalendarViewPager() = withBinding {
        saveSelectedDate(today)
        val calendarAdapter = CalendarVPAdapter(requireActivity(), CalendarMode.WEEKLY,this@HomeFragment)
        binding.homeWeeklyCalendarWeekVp.adapter = calendarAdapter

        val startPosition = Int.MAX_VALUE / 2
        binding.homeWeeklyCalendarWeekVp.setCurrentItem(startPosition, false)

        selectedDate = today
        binding.homeSelectedDateTv.text = dateFormat(today)

        weeklyPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val b = _binding ?: return
                val weekOffset = position - startPosition
                val referenceDate = today.plusWeeks(weekOffset.toLong())
                val dayOfWeekValue = referenceDate.dayOfWeek.value % 7
                val saturday = referenceDate.plusDays((6 - dayOfWeekValue).toLong())

                selectedDate = saturday
                b.homeSelectedDateTv.text = dateFormat(saturday)
            }
        }
        homeWeeklyCalendarWeekVp.registerOnPageChangeCallback(weeklyPageChangeCallback)

    }

    // 월간 달력 연결
    private fun setMonthlyCalendarViewPager() = withBinding {
        saveSelectedDate(today)
        val calendarAdapter = CalendarVPAdapter(requireActivity(), CalendarMode.MONTHLY, this@HomeFragment)
        binding.homeMonthlyCalendarMonthVp.adapter = calendarAdapter

        val startPosition = Int.MAX_VALUE / 2
        binding.homeMonthlyCalendarMonthVp.setCurrentItem(startPosition, false)

        monthlyPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val b = _binding ?: return
                val monthOffset = position - startPosition
                val newSelectedDate = today.plusMonths(monthOffset.toLong())
                selectedDate = newSelectedDate
                b.homeSelectedDateTv.text = dateFormat(newSelectedDate)
            }
        }
        homeMonthlyCalendarMonthVp.registerOnPageChangeCallback(monthlyPageChangeCallback)
    }

    // 주간/월간 토글 버튼 클릭 이벤트 설정
    private fun setCalendarModeToggleListeners() {
        binding.btnHomeWeeklyCalendar.setOnClickListener {
            updateCalendarToggle(true)
            showWeeklyCalendar()
        }

        binding.btnHomeMonthlyCalendar.setOnClickListener {
            updateCalendarToggle(false)
            showMonthlyCalendar()
        }
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

    // 주간 달력 표시
    private fun showWeeklyCalendar() {
        binding.homeWeeklyCalendarWeekVp.visibility = View.VISIBLE
        binding.homeMonthlyCalendarMonthVp.visibility = View.GONE
    }

    // 월간 달력 표시
    private fun showMonthlyCalendar() {
        binding.homeWeeklyCalendarWeekVp.visibility = View.GONE
        binding.homeMonthlyCalendarMonthVp.visibility = View.VISIBLE
    }

    private fun saveSelectedDate(date: LocalDate) {
        val sharedPref = requireContext().getSharedPreferences("CALENDAR-APP", AppCompatActivity.MODE_PRIVATE)
        sharedPref.edit().putString("SELECTED-DATE", date.toString()).apply()
    }

    private fun dateFormat(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        return date.format(formatter)
    }

    override fun onClickDate(date: LocalDate) {
        selectedDate = date
        saveSelectedDate(date)
        binding.homeSelectedDateTv.text = dateFormat(date)

        // 클릭된 날짜의 투두리스트 조회
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
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        todoViewModel.getTodoList(date = today)
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

        todoViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Log.e("HOME_FRAGMENT", error.toString())
        }
    }

    override fun onDestroyView() {
        _binding?.let { b ->
            runCatching {
                if (this::clockPageChangeCallback.isInitialized) {
                    b.clockPager.unregisterOnPageChangeCallback(clockPageChangeCallback)
                }
                if (this::weeklyPageChangeCallback.isInitialized) {
                    b.homeWeeklyCalendarWeekVp.unregisterOnPageChangeCallback(weeklyPageChangeCallback)
                }
                if (this::monthlyPageChangeCallback.isInitialized) {
                    b.homeMonthlyCalendarMonthVp.unregisterOnPageChangeCallback(monthlyPageChangeCallback)
                }
            }
        }
        _binding = null
        super.onDestroyView()
    }
}
