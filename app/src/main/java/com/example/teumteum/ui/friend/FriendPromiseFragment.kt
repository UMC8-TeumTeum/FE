package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumScheduleDetailResult
import com.example.teumteum.databinding.FragmentFriendPromiseBinding
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.friend.adapter.TeumEventAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.getSavedDateOrToday
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class FriendPromiseFragment : Fragment() {

    private var _binding: FragmentFriendPromiseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()

    private val baseDate: LocalDate by lazy { getSavedDateOrToday(requireContext()) }
    private var currentMonthOffset = 0
    private val today = LocalDate.now()

    private lateinit var eventAdapter: TeumEventAdapter
    private var selectedDate: LocalDate? = null

    // 바텀 시트
    private fun showPromiseDetailBottomSheet(
        detail: TeumScheduleDetailResult,
        isPast: Boolean
    ) {
        val bottomSheet = PromiseDetailBottomSheet(detail, isPast)
        bottomSheet.show(childFragmentManager, "PromiseDetailBottomSheet")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendPromiseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        viewModel.scheduledDotDates.observe(viewLifecycleOwner) {
            setupCalendarFragment()  // dotDates가 변경되면 프래그먼트 다시 붙이기
        }

        viewModel.scheduledTeumList.observe(viewLifecycleOwner) { list ->
            eventAdapter.updateData(list)
        }

        //  바텀시트 띄우는 옵저버 추가
        viewModel.teumScheduleDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                val isPast = viewModel.isPastSchedule.value ?: false
                showPromiseDetailBottomSheet(it, isPast)
            }
        }

        setupHeader()
        setupRecyclerView()
        setupCalendarNavigation()
        setupCalendarFragment()  //  MonthlyCalendarFragment 적용
        fetchDotDates()          //  pink_dot 날짜 요청

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupHeader() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"
    }

    private fun setupRecyclerView() {
        eventAdapter = TeumEventAdapter(emptyList()) { scheduleId ->
            viewModel.fetchTeumScheduleDetail(scheduleId)
        }

        binding.rvEventList.adapter = eventAdapter
    }


    private fun setupCalendarNavigation() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            setupHeader()
            setupCalendarFragment()
            fetchDotDates()
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            setupHeader()
            setupCalendarFragment()
            fetchDotDates()
        }
    }

    //  MonthlyCalendarFragment를 달력으로 붙이기
    private fun setupCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())

        val calendarFragment = MonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = object : IDateClickListener {
                // 날짜 선택 시
                override fun onClickDate(date: LocalDate) {
                    selectedDate = date

                    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    viewModel.fetchScheduledTeumList(dateStr)
                }
            },
            showDot = true,
            dotDates = viewModel.scheduledDotDates.value ?: emptyList() //  약속된 날짜 전달
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

    //  해당 월의 pink_dot용 날짜 받아오기 (ViewModel 연결)
    private fun fetchDotDates() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val monthStr = displayDate.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.KOREA))

        viewModel.fetchScheduledTeumDates(monthStr)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
