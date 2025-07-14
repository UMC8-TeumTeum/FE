package com.example.teumteum.ui.singup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingProfileBinding
import com.example.teumteum.databinding.FragmentOnBoardingScheduleBinding
import com.example.teumteum.ui.calendar.CalendarMode
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.main.CalendarVPAdapter
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.util.saveSelectedDate
import org.threeten.bp.LocalDate

class OnBoardingScheduleFragment : Fragment(){

    private lateinit var binding: FragmentOnBoardingScheduleBinding
    private lateinit var selectedDayTextView: TextView
    private val dayTextViews by lazy {
        listOf(binding.sunTv, binding.monTv, binding.tueTv, binding.wedTv, binding.thuTv, binding.friTv, binding.satTv)
    }

    private var selectedDayIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnBoardingScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(25)

        binding.nextBtn.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))

        }

        setupDaySelection()

    }

    private fun setupDaySelection() {
        dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                updateDayHighlight(index)
            }
        }
    }

    private fun updateDayHighlight(selectedIndex: Int) {
        // 이전 선택 요일: 배경 제거 + 글자색 검정
        dayTextViews[selectedDayIndex].background = null
        dayTextViews[selectedDayIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        // 새로 선택한 요일: 배경 보라색 + 글자색 흰색
        dayTextViews[selectedIndex].background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_day_selected)
        dayTextViews[selectedIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        selectedDayIndex = selectedIndex

        // 해당 요일의 일정 보여주기
//        scheduleAdapter.submitList(scheduleMap[selectedDayIndex] ?: emptyList())
    }

}