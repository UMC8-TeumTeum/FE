package com.example.teumteum.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentHomeBinding
import com.example.teumteum.ui.calendar.CalendarMode

import com.example.teumteum.ui.alarm.AlarmFragment
import com.example.teumteum.ui.calendar.CalendarVPAdapter
import com.example.teumteum.ui.filling.FillingActivity01Fragment
import com.example.teumteum.ui.todo.adapter.TodoRVAdapter
import com.example.teumteum.ui.todo.TodoRegisterFragment
import com.example.teumteum.ui.wish.WishlistFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.teumteum.data.TimeBlock
import com.example.teumteum.data.TimeType
import com.example.teumteum.data.entities.TodoList
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.todo.view.GetTodoListView
import com.example.teumteum.utils.applyBlurShadow

class HomeFragment : Fragment(), IDateClickListener, GetTodoListView {

    lateinit var binding: FragmentHomeBinding

    private val today: LocalDate = LocalDate.now()
    private lateinit var selectedDate: LocalDate

    private lateinit var adapter: TodoRVAdapter

    private val fullDaySchedule = listOf(
        TimeBlock(0, 360, TimeType.SLEEP),   // 00:00 ~ 06:00
        TimeBlock(360, 580, TimeType.TODO),  // 06:00 ~ 09:40
        TimeBlock(720, 860, TimeType.TODO),  // 12:00 ~ 14:20 (기존)
        TimeBlock(810, 900, TimeType.TODO),  // 13:30 ~ 15:00 (중복 테스트용)
        TimeBlock(900, 930, TimeType.EMPTY), // 15:00 ~ 15:30
        TimeBlock(930, 1050, TimeType.TODO), // 15:30 ~ 17:30
        TimeBlock(1110, 1200, TimeType.TODO),// 18:30 ~ 20:00
        TimeBlock(1200, 1320, TimeType.EMPTY),// 20:00 ~ 22:00
        TimeBlock(1320, 1440, TimeType.SLEEP)// 22:00 ~ 24:00
    )

    private var isAM: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

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
            val bottomSheet = TodoRegisterFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
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

//        adapter = TodoRVAdapter(parentFragmentManager, todoDummyList)
//        binding.todolistRv.adapter = adapter

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
//            refreshTodolist()
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.VISIBLE
    }

    /** 주간 달력 연결 */
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


    /** 월간 달력 연결 */
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

    /** 주간/월간 토글 버튼 클릭 이벤트 설정 */
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

    /** 주간/월간 버튼 이미지 변경 */
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

    /** 주간 달력 표시 */
    private fun showWeeklyCalendar() {
        binding.homeWeeklyCalendarWeekVp.visibility = View.VISIBLE
        binding.homeMonthlyCalendarMonthVp.visibility = View.GONE
    }

    /** 월간 달력 표시 */
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
    }


    private fun updateTimeChart(isAM: Boolean) {
        val halfDayBlocks = ChartUtils.splitAndFillTimeBlocks(fullDaySchedule, isAM)
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

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월"
    }

    override fun onGetTodoListSuccess(code: String, todoList: List<TodoList>) {
        Toast.makeText(requireContext(), "투두리스트가 성공적으로 조회되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onGetTodoListFailure(code: String, message: String?) {
        val errorMessage = when {
            code == "COMMON500" -> "서버 오류입니다. 관리자에게 문의해주세요."
            code == "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            code == "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> "투두리스트 조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

}
