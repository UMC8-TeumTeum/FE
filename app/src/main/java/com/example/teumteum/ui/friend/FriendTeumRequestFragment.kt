package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendTeumRequestBinding
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.friend.adapter.TeumRequestAdapter
import com.example.teumteum.ui.friend.adapter.TeumRequestItem
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.getSavedDateOrToday
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class FriendTeumRequestFragment : Fragment() {

    private var _binding: FragmentFriendTeumRequestBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: LocalDate? = null
    private val today: LocalDate = LocalDate.now()
    private val baseDate: LocalDate by lazy { getSavedDateOrToday(requireContext()) }
    private var currentMonthOffset = 0

    private lateinit var adapter: TeumRequestAdapter

    // 날짜 클릭 시 호출되는 리스너 내부에 RecyclerView 갱신 처리
    private val onClickListener = object : IDateClickListener {
        override fun onClickDate(date: LocalDate) {
            selectedDate = date
            updateCalendarFragment()

            // 선택한 날짜 포맷 → "25.MM.DD"
            val formattedDate = "25.${date.monthValue.toString().padStart(2, '0')}.${date.dayOfMonth.toString().padStart(2, '0')}"

            // 선택한 날짜에 맞는 데이터로 리스트 갱신 (예시)
            val updatedList = listOf(
                TeumRequestItem(
                    name = "문혜원",
                    date = formattedDate,
                    time = "10:00 ~ 13:00",
                    title = "중강 기념 한강 피크닉",
                    description = "중강 기념 한강 피크닉 가자! 돗자리는 내가 챙길게~ 다들 몸만 오세요",
                    profileImageRes = R.drawable.gray_teum
                )
            )

            // RecyclerView 업데이트
            adapter = TeumRequestAdapter(updatedList)
            binding.requestHistoryRecyclerView.adapter = adapter

            //  날짜 선택 시에만 보이게
            binding.requestHistoryRecyclerView.visibility = View.VISIBLE
        }
    }

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
        setupNavigationButtons()
        updateCalendarFragment()

        // RecyclerView 처음엔 안 보이도록
        binding.requestHistoryRecyclerView.visibility = View.GONE

        //  초기 데이터 연결 제거 또는 주석처리
        /*
        val initialList = listOf(
            TeumRequestItem(
                name = "문혜원",
                date = "25.06.05",
                time = "10:00 ~ 13:00",
                title = "중강 기념 한강 피크닉",
                description = "중강 기념 한강 피크닉 가자! 돗자리는 내가 챙길게~ 다들 몸만 오세요",
                profileImageRes = R.drawable.gray_teum
            )
        )

        adapter = TeumRequestAdapter(initialList)
        binding.requestHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.requestHistoryRecyclerView.adapter = adapter
        */

        // 대신 layoutManager만 설정 (한 번만 하면 됨)
        binding.requestHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
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
