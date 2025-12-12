package com.example.teumteum.ui.wish

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
import com.example.teumteum.databinding.FragmentWishSetting01Binding
import com.example.teumteum.databinding.ItemClockMiniPageBinding
import com.example.teumteum.ui.activity.FillingSetting02Fragment
import com.example.teumteum.ui.activity.FillingSetting03Fragment
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.ClockHalf
import com.example.teumteum.ui.clock.ClockVPAdapter
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.main.data.TimeType
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.wish.adapter.WishTimeAdapter
import com.example.teumteum.ui.wish.data.UiTimeSlot
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.orEmpty

@AndroidEntryPoint
class WishSetting01Fragment : Fragment() {

    private lateinit var binding: FragmentWishSetting01Binding

    private var selectedTimeText: String? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var isAM: Boolean = true
    private var wishId: Long? = null
    private var isDirectInput: Boolean = true

    private lateinit var clockAdapter: ClockVPAdapter<ItemClockMiniPageBinding>

    private var selectedDateServer: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishSetting01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val title = arguments?.getString("title")
        setTitle(title.toString())

        val time = arguments?.getString("time")
        setTime(time.toString())

        wishId = arguments?.getLong("wish_id")

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        // EMPTY만 필터 후 변환
        val emptyBlocks = homeViewModel.scheduleList.value.orEmpty()
            .filter { it.type == TimeType.EMPTY }   // enum 경로에 맞게 수정
            .map { UiTimeSlot(it.startTime.toHHmm(), it.endTime.toHHmm()) }

        // 어댑터 생성 (문자열 콜백)
        val wishTimeAdapter = WishTimeAdapter(
            onSelect = { _, slot ->
                selectedStartTime = slot.startTime
                selectedEndTime   = slot.endTime
                selectedTimeText = "${slot.startTime} ~ ${slot.endTime}"
                isDirectInput = false

                enableNextButton()
            },
            onDirectInput = {
                isDirectInput = true
                // 직접 입력 바텀시트/다이얼로그 띄우고 완료되면 selectedStartTime/EndTime에 "HH:mm" 셋팅
                // 예: showTimeInputBottomSheet { start, end -> selectedStartTime = start; selectedEndTime = end }
                selectedTimeText = "직접 입력하기"
                enableNextButton()
            }
        )

        binding.wishTimeRc.adapter = wishTimeAdapter
        binding.wishTimeRc.layoutManager = LinearLayoutManager(requireContext())

        wishTimeAdapter.submitList(emptyBlocks)


        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupClockPager()
        updateIndicator(isAM)

        binding.amPmTv.setOnClickListener {
            val amPos = clockAdapter.positionOf(ClockHalf.AM)
            val pmPos = clockAdapter.positionOf(ClockHalf.PM)
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

        binding.selectDateBtn.setOnClickListener {
            BottomSheetAssignCalendarFragment()
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
        }

        binding.nextBtn.setOnClickListener {
            if(isDirectInput){
                val fragment = FillingSetting03Fragment().apply {
                    arguments = Bundle().apply {
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
            }else{
                val fragment = FillingSetting02Fragment().apply {
                    arguments = Bundle().apply {
                        wishId?.let { putLong("wish_id", it) }
                        putString("title", title)
                        putString("time", time)
                        putString("selected_date", selectedDateServer)
                        putString("startTime", selectedStartTime)
                        putString("endTime", selectedEndTime)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun enableNextButton() {
        binding.nextBtn.isEnabled = true
        binding.nextBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        binding.nextBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setTitle(title: String){
        binding.wishTitleTv.text = title
    }

    private fun setTime(time: String){
        binding.wishTimeTv.text = time
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
        val minutesInDay = 24 * 60
        val norm = ((this % minutesInDay) + minutesInDay) % minutesInDay
        val h = norm / 60
        val m = norm % 60
        return String.format("%02d:%02d", h, m)
    }
}