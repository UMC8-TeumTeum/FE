package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateDateBinding
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.getSavedDateOrToday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class FriendRoommateDateFragment : Fragment() {

    private var _binding: FragmentFriendRoommateDateBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: LocalDate? = null
    private val today: LocalDate = LocalDate.now()
    private val baseDate: LocalDate by lazy { getSavedDateOrToday(requireContext()) }
    private var currentMonthOffset = 0

    private val onClickListener = object : IDateClickListener {
        override fun onClickDate(date: LocalDate) {
            selectedDate = date
            updateCalendarFragment()

            // 선택된 날짜가 오늘 이후일 때 버튼 활성화
            if (date.isAfter(today)) {
                binding.nextBtn.isEnabled = true
                binding.nextBtn.setBackgroundColor(Color.parseColor("#000000"))  // 검정색
                binding.nextBtn.setTextColor(Color.parseColor("#FFFFFF"))        // 흰 글씨
            } else {
                binding.nextBtn.isEnabled = false
                binding.nextBtn.setBackgroundColor(Color.parseColor("#F6F6F6"))  // 비활성 색
                binding.nextBtn.setTextColor(Color.parseColor("#0F0F0F"))        // 회색 글씨
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRoommateDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        setupNavigationButtons()
        updateCalendarFragment()

        // 초기 버튼 상태 비활성화
        binding.nextBtn.isEnabled = false
        binding.nextBtn.setBackgroundColor(Color.parseColor("#F6F6F6"))
        binding.nextBtn.setTextColor(Color.parseColor("#0F0F0F"))

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.nextBtn.setOnClickListener {
            val formattedDate = selectedDate?.let {
                val formatter = DateTimeFormatter.ofPattern("yy.MM.dd(E)", Locale.KOREAN)
                it.format(formatter)
            } ?: ""

            val bundle = Bundle().apply {
                putString("selected_date", formattedDate)
            }

            val fragment = FriendRoommateFriendFragment()
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }


    }

    private fun setupNavigationButtons() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            updateCalendarFragment()
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            updateCalendarFragment()
        }
    }

    private fun updateCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"

        val calendarFragment = MonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = onClickListener,
            showDot = true
        ).apply {
            arguments = Bundle().apply {
                putSerializable("displayDate", displayDate)
                putSerializable("selectedDate", selectedDate)
                putSerializable("today", today)
            }
        }

        childFragmentManager.beginTransaction()
            .replace(binding.calendarContainer.id, calendarFragment)
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}