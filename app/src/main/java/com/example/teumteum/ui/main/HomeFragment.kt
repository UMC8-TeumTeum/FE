package com.example.teumteum.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.teumteum.ui.calendar.viewModel.CalendarViewModel
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.ui.clock.ChartUtils
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

            val scheduleList = viewModel.scheduleList.value ?: emptyList()
            val sleepBlocks = scheduleList.filter { it.type == TimeType.SLEEP }

            TodoRegisterFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("sleepBlocks", ArrayList(sleepBlocks))
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

        setWeeklyCalendarViewPager()
        setMonthlyCalendarViewPager()
        setCalendarModeToggleListeners()
        showWeeklyCalendar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = TodoRVAdapter(parentFragmentManager, todolistItems) { id, toActive ->
            val status = if (toActive) AlarmStatus.ACTIVE else AlarmStatus.INACTIVE
            todoViewModel.patchAlarmStatus(
                AlarmStatusRequest(
                    todoId = id,
                    alarmStatus = status
                )
            )
        }
        binding.todolistRv.adapter = adapter

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        viewModel.getTodayScheduleIfNeeded()
        viewModel.getTeumTime()

        viewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        viewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        viewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

        viewModel.scheduleList.observe(viewLifecycleOwner) {
            updateTimeChart(isAM)
            updateIndicator(isAM)
        }

        binding.fabAddIv.post {
            applyBlurShadow(
                sourceView = binding.fabAddIv,
                targetImageView = binding.fabShadowIv
            )
        }

        ChartUtils.setupPieChart(binding.clockChart)

        val sleepBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)

        binding.clockChart.renderer = IconPieChartRenderer(
            binding.clockChart,
            binding.clockChart.animator,
            binding.clockChart.viewPortHandler,
            sleepBitmap
        )

        updateTimeChart(isAM)
        updateIndicator(isAM)

        binding.amPmTv.setOnClickListener {
            isAM = !isAM
            updateTimeChart(isAM)
            updateIndicator(isAM)
        }

        // 투두 등록 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_register", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            refreshTodolist()
        }

        // 투두 수정 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_edit", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            refreshTodolist()
        }

        // 투두 삭제 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("todo_delete", viewLifecycleOwner) { _, _ ->
            viewModel.refreshTodaySchedule()
            refreshTodolist()
        }

        todoViewModel.getTodoList(date)
        myHomeViewModel.getMyInfo()

        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.VISIBLE
    }

    // 주간 달력 연결
    private fun setWeeklyCalendarViewPager() {
        saveSelectedDate(today)
        val calendarAdapter = CalendarVPAdapter(requireActivity(), CalendarMode.WEEKLY,this)
        binding.homeWeeklyCalendarWeekVp.adapter = calendarAdapter

        val startPosition = Int.MAX_VALUE / 2
        binding.homeWeeklyCalendarWeekVp.setCurrentItem(startPosition, false)

        selectedDate = today
        binding.homeSelectedDateTv.text = dateFormat(today)

        binding.homeWeeklyCalendarWeekVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val weekOffset = position - startPosition

                // 오늘 날짜에서 weekOffset만큼 이동
                val referenceDate = today.plusWeeks(weekOffset.toLong())

                // 해당 주의 요일 (1: 월요일 ~ 7: 일요일)
                val dayOfWeekValue = referenceDate.dayOfWeek.value % 7

                val saturday = referenceDate.plusDays((6 - dayOfWeekValue).toLong())

                selectedDate = saturday
                binding.homeSelectedDateTv.text = dateFormat(saturday)
            }
        })

    }


    // 월간 달력 연결
    private fun setMonthlyCalendarViewPager() {
        saveSelectedDate(today)
        val calendarAdapter = CalendarVPAdapter(requireActivity(), CalendarMode.MONTHLY, this)
        binding.homeMonthlyCalendarMonthVp.adapter = calendarAdapter

        val startPosition = Int.MAX_VALUE / 2
        binding.homeMonthlyCalendarMonthVp.setCurrentItem(startPosition, false)

        binding.homeMonthlyCalendarMonthVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val monthOffset = position - startPosition
                val newSelectedDate = today.plusMonths(monthOffset.toLong())
                selectedDate = newSelectedDate
                binding.homeSelectedDateTv.text = dateFormat(newSelectedDate)
            }
        })
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


    private fun updateTimeChart(isAM: Boolean) {
        val blocks = viewModel.scheduleList.value ?: return
        val halfDayBlocks = ChartUtils.splitAndFillTimeBlocks(blocks, isAM)
        ChartUtils.setTimePieChartData(requireContext(), binding.clockChart, halfDayBlocks)
    }

    private fun updateIndicator(isAM: Boolean) {
        val leftView = binding.leftView
        val rightView = binding.rightView

        if (isAM) {
            //왼쪽이 막대, 오른쪽이 점
            leftView.layoutParams.width = dpToPx(28)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar)

            rightView.layoutParams.width = dpToPx(4)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            binding.amPmTv.text="AM"
        } else {
            //왼쪽이 점, 오른쪽이 막대
            leftView.layoutParams.width = dpToPx(4)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            rightView.layoutParams.width = dpToPx(28)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar)

            binding.amPmTv.text="PM"
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
        Log.d("TODO_LIST", "TODO_LIST: ${todoViewModel.todolistItems}")
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
            Toast.makeText(requireContext(), "투두리스트 조회 실패: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
