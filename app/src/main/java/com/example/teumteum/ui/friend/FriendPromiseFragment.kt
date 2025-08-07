package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private var lastClickedScheduleId: Int = -1 //  클릭한 스케줄 ID 저장

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

        setupHeader()
        setupRecyclerView()
        setupCalendarNavigation()
        setupCalendarFragment()
        fetchDotDates()

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.scheduledDotDates.observe(viewLifecycleOwner) {
            setupCalendarFragment() // dot 위치 업데이트
        }

        viewModel.scheduledTeumList.observe(viewLifecycleOwner) { list ->
            eventAdapter.updateData(list)
        }

        viewModel.teumScheduleDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let {
                val isPast = viewModel.isPastSchedule.value ?: false
                showPromiseDetailBottomSheet(it, lastClickedScheduleId, isPast) //  스케줄 ID 함께 전달
            }
        }
    }

    private fun setupHeader() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"
    }

    private fun setupRecyclerView() {
        eventAdapter = TeumEventAdapter(emptyList()) { scheduleId ->
            lastClickedScheduleId = scheduleId //  클릭한 ID 저장
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

    private fun setupCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val calendarFragment = MonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = object : IDateClickListener {
                override fun onClickDate(date: LocalDate) {
                    selectedDate = date
                    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    viewModel.fetchScheduledTeumList(dateStr)
                }
            },
            showDot = true,
            dotDates = viewModel.scheduledDotDates.value ?: emptyList()
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
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        val monthStr = displayDate.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.KOREA))
        viewModel.fetchScheduledTeumDates(monthStr)
    }

    //  바텀시트에 detail + scheduleId 넘기기
    private fun showPromiseDetailBottomSheet(
        detail: TeumScheduleDetailResult,
        scheduleId: Int,
        isPast: Boolean
    ) {
        val bottomSheet = PromiseDetailBottomSheet(
            detail = detail,
            scheduleId = scheduleId,
            isPast = isPast
        )
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
