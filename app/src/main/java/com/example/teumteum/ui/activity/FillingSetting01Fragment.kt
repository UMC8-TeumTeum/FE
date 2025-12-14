package com.example.teumteum.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFillingSetting01Binding
import com.example.teumteum.databinding.ItemClockMiniPageBinding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.ClockHalf
import com.example.teumteum.ui.clock.ClockVPAdapter
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.main.data.TimeType
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.wish.BottomSheetAssignCalendarFragment
import com.example.teumteum.ui.wish.adapter.WishTimeAdapter
import com.example.teumteum.ui.wish.data.UiTimeSlot
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.orEmpty

@AndroidEntryPoint
class FillingSetting01Fragment : Fragment() {

    private lateinit var binding: FragmentFillingSetting01Binding

    private var selectedTimeText: String? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var isAM: Boolean = true
    private var isDirectInput: Boolean = true

    private var aiId: String? = null
    private var wishId: Long? = null

    private lateinit var clockAdapter: ClockVPAdapter<ItemClockMiniPageBinding>
    private lateinit var wishTimeAdapter: WishTimeAdapter

    private var selectedDateServer: String = LocalDate.now().toString()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFillingSetting01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val title = arguments?.getString("title")
        binding.assignTitleTv.text = title

        val time = arguments?.getString("time")
        binding.assignTimeTv.text = time

        aiId = arguments?.getString("ai_id")
        wishId = arguments?.getLong("wish_id", -1L)
            ?.takeIf { it > 0L }

        val today = LocalDate.now()
        selectedDateServer = today.toString()

        val displayFormatter = DateTimeFormatter.ofPattern("yy.MM.dd(E)", Locale.KOREAN)
        binding.assignDateTv.text = today.format(displayFormatter)

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        // 어댑터 생성 (문자열 콜백)
        wishTimeAdapter = WishTimeAdapter(
            onSelect = { _, slot ->
                selectedStartTime = slot.startTime
                selectedEndTime   = slot.endTime
                selectedTimeText = "${slot.startTime} ~ ${slot.endTime}"
                isDirectInput = false
                enableNextButton()
            },
            onDirectInput = {
                isDirectInput = true
                selectedTimeText = "직접 입력하기"
                enableNextButton()
            }
        )

        binding.wishTimeRc.adapter = wishTimeAdapter
        binding.wishTimeRc.layoutManager = LinearLayoutManager(requireContext())

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupClockPager()
        setupObservers()

        homeViewModel.getTodaySchedule(selectedDateServer)

        binding.amPmTv.setOnClickListener {
            val amPos = clockAdapter.positionOf(ClockHalf.AM)
            val pmPos = clockAdapter.positionOf(ClockHalf.PM)
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

        binding.selectDateBtn.setOnClickListener {
            BottomSheetAssignCalendarFragment
                .newInstance(selectedDateServer)
                .show(parentFragmentManager, "BottomSheetCalendar")
        }

        parentFragmentManager.setFragmentResultListener(
            "assign_date_result",
            viewLifecycleOwner
        ) { _, bundle ->
            val display = bundle.getString("assign_date_display") ?: return@setFragmentResultListener
            val server = bundle.getString("assign_date_server") ?: return@setFragmentResultListener

            binding.assignDateTv.text = display
            selectedDateServer = server

            selectedStartTime = null
            selectedEndTime = null
            selectedTimeText = null
            isDirectInput = true

            binding.nextBtn.isEnabled = false
            binding.nextBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teumteum_bg))
            binding.nextBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))

            wishTimeAdapter.clearSelection()
            homeViewModel.getTodaySchedule(server)
        }

        binding.nextBtn.setOnClickListener {
            if (isDirectInput) {
                val fragment = FillingSetting03Fragment().apply {
                    arguments = Bundle().apply {
                        aiId?.let { putString("ai_id", it)}
                        wishId?.let { putLong("wish_id", it) }
                        putString("title", title)
                        putString("time", time)
                        putString("selected_date", selectedDateServer)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {

                val startDate = selectedDateServer
                var endDate = selectedDateServer
                val sendStartTime = selectedStartTime ?: return@setOnClickListener
                var sendEndTime = selectedEndTime ?: return@setOnClickListener

                // 24:00 -> 다음날 00:00 변환
                if (sendEndTime == "24:00") {
                    endDate = LocalDate.parse(selectedDateServer)
                        .plusDays(1)
                        .toString()
                    sendEndTime = "00:00"
                }

                val fragment = FillingSetting02Fragment().apply {
                    arguments = Bundle().apply {
                        aiId?.let { putString("ai_id", it)}
                        wishId?.let { putLong("wish_id", it) }
                        putString("title", title)
                        putString("time", time)

                        putString("startDate", startDate)
                        putString("endDate", endDate)
                        putString("startTime", sendStartTime)
                        putString("endTime", sendEndTime)

                        putString("selected_time_text", selectedTimeText)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        updateIndicator(isAM)
    }

    private fun setupObservers() {
        homeViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            // EMPTY 시간 추출 및 어댑터 갱신
            val emptyBlocks = scheduleList
                .filter { it.type == TimeType.EMPTY }
                .map { UiTimeSlot(it.startTime.toHHmm(), it.endTime.toHHmm()) }

            wishTimeAdapter.submitList(emptyBlocks) {
                wishTimeAdapter.notifyDataSetChanged()
            }

            clockAdapter.notifyDataSetChanged()
        }
    }

    private fun setupClockPager() {
        clockAdapter = ClockVPAdapter(
            inflate = ItemClockMiniPageBinding::inflate,
            chartOf = { it.clockChart },
            onBindPage = { chart, half ->
                ChartUtils.setupPieChart(chart)
                val sleepBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)
                chart.renderer = IconPieChartRenderer(chart, chart.animator, chart.viewPortHandler, sleepBitmap)

                // AM/PM 데이터 바인딩
                val blocks = homeViewModel.scheduleList.value.orEmpty()
                val halfBlocks = ChartUtils.splitAndFillTimeBlocks(blocks, half == ClockHalf.AM)
                ChartUtils.setTimePieChartData(requireContext(), chart, halfBlocks)

            }
        )

        binding.clockPager.adapter = clockAdapter
        binding.clockPager.offscreenPageLimit = 1

        val amPos = clockAdapter.positionOf(ClockHalf.AM) // 0
        val pmPos = clockAdapter.positionOf(ClockHalf.PM) // 1

        binding.clockPager.setCurrentItem(amPos, false)

        binding.clockPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicator(position == amPos)
            }
        })

        binding.amPmTv.setOnClickListener {
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

    }

    private fun enableNextButton() {
        binding.nextBtn.isEnabled = true
        binding.nextBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        binding.nextBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun updateIndicator(isAM: Boolean) {
        val leftView = binding.leftView
        val rightView = binding.rightView

        if (isAM) {
            //왼쪽이 막대, 오른쪽이 점
            leftView.layoutParams.width = dpToPx(28)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar_purple)

            rightView.layoutParams.width = dpToPx(4)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            binding.amPmTv.text="AM"
        } else {
            //왼쪽이 점, 오른쪽이 막대
            leftView.layoutParams.width = dpToPx(4)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            rightView.layoutParams.width = dpToPx(28)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar_purple)

            binding.amPmTv.text="PM"
        }

        leftView.requestLayout()
        rightView.requestLayout()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // 분 → "HH:mm"
    private fun Int.toHHmm(): String {
        if (this == 24 * 60) return "24:00"
        val minutesInDay = 24 * 60
        val norm = ((this % minutesInDay) + minutesInDay) % minutesInDay
        val h = norm / 60
        val m = norm % 60
        return String.format("%02d:%02d", h, m)
    }
}