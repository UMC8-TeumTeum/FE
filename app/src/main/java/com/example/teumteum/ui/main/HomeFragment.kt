package com.example.teumteum.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentHomeBinding
import com.example.teumteum.ui.calendar.CalendarMode

import com.example.teumteum.data.entities.TodoHomeItem
import com.example.teumteum.ui.alarm.AlarmFragment
import com.example.teumteum.ui.calendar.CalendarVPAdapter
import com.example.teumteum.ui.filling.FillingActivity01Fragment
import com.example.teumteum.ui.todo.TodoRVAdapter
import com.example.teumteum.ui.todo.TodoRegisterFragment
import com.example.teumteum.ui.wish.WishlistFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.teumteum.utils.applyBlurShadow

class HomeFragment : Fragment(), IDateClickListener {

    lateinit var binding: FragmentHomeBinding

    private val today: LocalDate = LocalDate.now()
    private lateinit var selectedDate: LocalDate

    private lateinit var adapter: TodoRVAdapter

    private var todoDummyList = mutableListOf(
        TodoHomeItem(1, "UX디자인 수업", "오후 12:00", "오후 2:30", isPublic = true, isAlarmOn = true),
        TodoHomeItem(2, "교내 근로", "오후 3:30", "오후 5:30", isPublic = false, isAlarmOn = null),
        TodoHomeItem(3, "중랑천 산책", "오후 6:30", "오후 8:00", isPublic = false, isAlarmOn = false)
    )

//    private var wishDummyList = mutableListOf(
//        WishItem(1, "화분 물 주기", "10m", "일상"),
//        WishItem(2, "무신사 아이 쇼핑", "10m", "일상"),
//        WishItem(3, "뜨개질하기", "30m", "취미"),
//        WishItem(4, "피그마 파일 정리", "20m", "일상"),
//        WishItem(5, "인센스 & 명상", "10m", "휴식"),
//        WishItem(6, "방 구조 바꾸기", "30m", "일상"),
//        WishItem(7, "매거진 3장 읽기", "10m", "일상"),
//        WishItem(8, "사진첩 정리", "30m", "일상"),
//        WishItem(9, "중랑천 산책", "1h~", "운동"),
//        WishItem(10, "테스트용1", "10m", "문화생활"),
//        WishItem(11, "테스트용2", "20m", "자기계발")
//    )

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

        adapter = TodoRVAdapter(parentFragmentManager, todoDummyList)
        binding.todolistRv.adapter = adapter

        binding.fabAddIv.post {
            applyBlurShadow(
                sourceView = binding.fabAddIv,
                targetImageView = binding.fabShadowIv
            )
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

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월"
    }
}
