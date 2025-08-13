package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teumteum.databinding.FragmentFriendTodoListBinding
import com.example.teumteum.ui.calendar.FriendMonthlyCalendarFragment
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.friend.adapter.PublicTodoAdapter
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

    private lateinit var todoAdapter: PublicTodoAdapter
    private var selectedDate: LocalDate? = null
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

        // 날짜별 공개 투두 결과 관찰 → 카드 표시/숨김
        viewModel.publicTodosByDate.observe(viewLifecycleOwner) { list ->
            todoAdapter.submitList(list)
            binding.rvEventList.isVisible = list.isNotEmpty()
        }

    }

    private fun setupHeader() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"
    }

    private fun setupRecyclerView() {
        todoAdapter = PublicTodoAdapter()
        binding.rvEventList.adapter = todoAdapter
        binding.rvEventList.isVisible = false // 처음엔 숨김
    }

    private fun setupCalendarNavigation() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            setupHeader()
            setupCalendarFragment()
            fetchDotDates() // 이전 달 재조회
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            setupHeader()
            setupCalendarFragment()
            fetchDotDates() // 다음 달 재조회
        }
    }

    private fun setupCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val calendarFragment = FriendMonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = object : IDateClickListener {
                override fun onClickDate(date: LocalDate) {
                    selectedDate = date
                    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    if (friendUserId != -1) {
                        // 날짜 클릭 시 특정 날짜 공개 투두 조회
                        viewModel.fetchFriendPublicTodosByDate(friendUserId, dateStr)
                    }
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
