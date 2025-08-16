package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
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

    private val baseDate: LocalDate = LocalDate.now()
    private var currentMonthOffset = 0
    private val today = LocalDate.now()

    private lateinit var todoAdapter: PublicTodoAdapter
    private var selectedDate: LocalDate = LocalDate.now()
    private var friendUserId: Int = -1

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

        val nickname = arguments?.getString("nickname") ?: "닉네임"
        friendUserId = arguments?.getInt("userId") ?: -1
        binding.tvName.text = "${nickname}님이"

        Log.d("FRIEND_TODO_LIST", "friendUserId = $friendUserId")

        setupHeader()
        setupRecyclerView()
        setupCalendarNavigation()
        setupCalendarFragment()

        // arguments에서 friendUserId 읽은 뒤에 호출
        fetchDotDates()

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewModel.publicTodoDotDates.observe(viewLifecycleOwner) {
            setupCalendarFragment()
        }

        viewModel.publicTodosByDate.observe(viewLifecycleOwner) { list ->
            todoAdapter.submitList(list)
            binding.rvEventList.isVisible = list.isNotEmpty()
        }

        onDateSelected(selectedDate)
    }

    private fun setupHeader() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"
    }

    private fun setupRecyclerView() {
        todoAdapter = PublicTodoAdapter()
        binding.rvEventList.adapter = todoAdapter
        binding.rvEventList.isVisible = false
    }

    private fun setupCalendarNavigation() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            todoAdapter.submitList(emptyList())
            setupHeader()
            setupCalendarFragment()
            fetchDotDates()

            // 원래 달로 복귀하면 리스트 자동 복구
            if (currentMonthOffset == 0) {
                selectedDate = today
                onDateSelected(selectedDate)
            }
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            todoAdapter.submitList(emptyList())
            setupHeader()
            setupCalendarFragment()
            fetchDotDates()

            if (currentMonthOffset == 0) {
                selectedDate = today
                onDateSelected(selectedDate)
            }
        }
    }

    private fun setupCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val calendarFragment = FriendMonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = object : IDateClickListener {
                override fun onClickDate(date: LocalDate) {
                    onDateSelected(date)
                }
            },
            showDot = true,
            dotDates = viewModel.publicTodoDotDates.value ?: emptyList()
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
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (friendUserId != -1) {
            viewModel.fetchFriendPublicTodosByDate(friendUserId, dateStr)
        }
    }

    private fun fetchDotDates() {
        if (friendUserId == -1) return
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val monthStr = displayDate.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.KOREA))
        viewModel.fetchFriendPublicTodoDates(friendUserId, monthStr)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

