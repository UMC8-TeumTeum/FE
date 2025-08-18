package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.databinding.FragmentFriendTeumRequestBinding
import com.example.teumteum.ui.calendar.FriendMonthlyCalendarFragment
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.friend.adapter.TeumRequestAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.getSavedDateOrToday
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class FriendTeumRequestFragment : Fragment() {

    private var _binding: FragmentFriendTeumRequestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private var selectedDate: LocalDate = LocalDate.now()
    private val today: LocalDate = LocalDate.now()
    private val baseDate: LocalDate = LocalDate.now()
    private var currentMonthOffset = 0

    private var requestDotDates: HashSet<LocalDate> = hashSetOf()

    private lateinit var adapter: TeumRequestAdapter

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

        // 어댑터 초기화
        adapter = TeumRequestAdapter()
        binding.requestHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.requestHistoryRecyclerView.adapter = adapter
        binding.requestHistoryRecyclerView.visibility = View.GONE // 처음엔 숨김

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 이전/다음
        setupNavigationButtons()

        //  점 LiveData 관찰 → 달력 다시 그림
        viewModel.requestDotDates.observe(viewLifecycleOwner) { dates ->
            requestDotDates = HashSet(dates)
            updateCalendarFragment() // 최신 점 목록 반영해서 다시 그림
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

        // 최초 달력 그리기
        updateCalendarFragment()

        // 진입 시 오늘 데이터 로드
        onDateSelected(selectedDate)
    }

    private fun setupNavigationButtons() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            adapter.submitList(emptyList())
            fetchDotsForCurrentMonth()
            updateCalendarFragment()

            if (currentMonthOffset == 0) {
                selectedDate = today
                onDateSelected(selectedDate)
            }
        }
        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            adapter.submitList(emptyList())
            fetchDotsForCurrentMonth()
            updateCalendarFragment()

            if (currentMonthOffset == 0) {
                selectedDate = today
                onDateSelected(selectedDate)
            }
        }
    }

    //  현재 표시 월에 대해 ‘틈 요청 날짜 리스트’ API 호출
    private fun fetchDotsForCurrentMonth() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val ym = java.time.YearMonth.of(displayDate.year, displayDate.monthValue)
        val monthStr = ym.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.fetchRequestTeumDates(monthStr)
    }

    // 날짜 클릭 리스너: 선택 강조만 (리스트는 API 준비되면 연결)
    private val onClickListener = object : IDateClickListener {
        override fun onClickDate(date: LocalDate) {
            onDateSelected(date)
        }
    }

    private fun updateCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"

        val calendarFragment = FriendMonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = onClickListener,
            showDot = true,
            dotDates = requestDotDates.toList()
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

    private fun onDateSelected(date: LocalDate) {
        selectedDate = date
        updateCalendarFragment()

        val dateStr = date.format(java.time.format.DateTimeFormatter.ISO_DATE)
        viewModel.loadTeumRequestsByDate(dateStr)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
