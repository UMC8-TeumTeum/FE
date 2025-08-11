package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teumteum.databinding.FragmentFriendTodoListBinding
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.friend.adapter.TeumEventAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.getSavedDateOrToday
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class FriendTodoListFragment : Fragment() {

    private var _binding: FragmentFriendTodoListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()

    private val baseDate: LocalDate by lazy { getSavedDateOrToday(requireContext()) }
    private var currentMonthOffset = 0
    private val today = LocalDate.now()

    private lateinit var eventAdapter: TeumEventAdapter
    private var selectedDate: LocalDate? = null
    private var lastClickedScheduleId: Int = -1

    private var friendUserId: Int = -1 // 공개 투두 조회 대상

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        // 닉네임/유저ID 받기
        val nickname = arguments?.getString("nickname") ?: "닉네임"
        friendUserId = arguments?.getInt("userId") ?: -1
        binding.tvName.text = "${nickname}님이"

        setupHeader()
        setupRecyclerView()
        setupCalendarNavigation()
        setupCalendarFragment()
        fetchDotDates() // 진입 시 현재 월 공개 투두 날짜 조회

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 공개 투두 dot 날짜 관찰 → 달력 갱신
        viewModel.publicTodoDotDates.observe(viewLifecycleOwner) {
            setupCalendarFragment()
        }

        // (리스트/상세는 필요 시 계속 사용)
        viewModel.scheduledTeumList.observe(viewLifecycleOwner) { list ->
            eventAdapter.updateData(list)
        }
        viewModel.teumScheduleDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                val isPast = viewModel.isPastSchedule.value ?: false
                // showPromiseDetailBottomSheet(it, lastClickedScheduleId, isPast)
            }
        }
    }

    private fun setupHeader() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"
    }

    private fun setupRecyclerView() {
        eventAdapter = TeumEventAdapter(emptyList()) { scheduleId ->
            lastClickedScheduleId = scheduleId
            viewModel.fetchTeumScheduleDetail(scheduleId)
        }
        binding.rvEventList.adapter = eventAdapter
    }

    private fun setupCalendarNavigation() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            setupHeader()
            setupCalendarFragment()
            fetchDotDates() //이전/다음 달마다 재조회
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            setupHeader()
            setupCalendarFragment()
            fetchDotDates()
        }
    }

    private fun setupCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val calendarFragment = MonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = object : IDateClickListener {
                override fun onClickDate(date: LocalDate) {
                    selectedDate = date
                    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    // 날짜 클릭 시 리스트 조회가 필요하면 여기에 API 호출 연결
                    // viewModel.fetchFriendPublicTodosByDate(friendUserId, dateStr)
                }
            },
            showDot = true,
            dotDates = viewModel.publicTodoDotDates.value ?: emptyList() // 공개 투두 dot 사용
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

    private fun fetchDotDates() {
        if (friendUserId == -1) return // userId 없으면 호출 X
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val monthStr = displayDate.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.KOREA))
        viewModel.fetchFriendPublicTodoDates(friendUserId, monthStr) // 공개 투두 날짜 조회
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
