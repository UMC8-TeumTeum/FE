package com.example.teumteum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.BottomSheetRegisterFragment
import com.example.teumteum.CalendarMode
import com.example.teumteum.CalendarVPAdapter
import com.example.teumteum.IDateClickListener
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentHomeBinding

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class HomeFragment : Fragment(), IDateClickListener {

    lateinit var binding: FragmentHomeBinding

    private val today: LocalDate = LocalDate.now()
    private lateinit var selectedDate: LocalDate

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
            val bottomSheet = BottomSheetRegisterFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

//        binding.fabAddIv.setOnClickListener {
//            val bottomSheetDialog = BottomSheetDialog(requireContext())
//            val bottomSheetView = layoutInflater.inflate(R.layout.fragment_bottom_sheet_register, null)
//            bottomSheetDialog.setContentView(bottomSheetView)
//
//            bottomSheetDialog.setOnShowListener { dialog ->
//                val bottomSheet =
//                    (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//                bottomSheet?.let {
//                    val screenHeight = resources.displayMetrics.heightPixels
//                    it.layoutParams?.height = (screenHeight * 0.84).toInt()
//                    val behavior = BottomSheetBehavior.from(it)
//                    behavior.peekHeight = (screenHeight * 0.84).toInt()
//                    it.requestLayout()
//                }
//            }
//
//            bottomSheetDialog.show()
//        }

        setWeeklyCalendarViewPager()
        setMonthlyCalendarViewPager()
        setCalendarModeToggleListeners()
        showWeeklyCalendar()

        return binding.root
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
                val newSelectedDate = today.plusWeeks(weekOffset.toLong())

                selectedDate = newSelectedDate
                binding.homeSelectedDateTv.text = dateFormat(newSelectedDate)
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
        binding.homeWeeklyCalendarIv.setOnClickListener {
            updateCalendarToggle(true)
            showWeeklyCalendar()
        }

        binding.homeMonthlyCalendarIv.setOnClickListener {
            updateCalendarToggle(false)
            showMonthlyCalendar()
        }
    }

    /** 주간/월간 버튼 이미지 변경 */
    private fun updateCalendarToggle(isWeekly: Boolean) {
        val weeklyRes = if (isWeekly) R.drawable.btn_weekly_calendar_active_sv else R.drawable.btn_weekly_calendar_deactive_sv
        val monthlyRes = if (isWeekly) R.drawable.btn_monthly_calendar_deactive_sv else R.drawable.btn_monthly_calendar_active_sv
        binding.homeWeeklyCalendarIv.setImageResource(weeklyRes)
        binding.homeMonthlyCalendarIv.setImageResource(monthlyRes)
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
