package com.example.teumteum.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.example.teumteum.databinding.FragmentFillingSetting01Binding
import com.example.teumteum.databinding.ItemClockMiniPageBinding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.ClockHalf
import com.example.teumteum.ui.clock.ClockVPAdapter
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.orEmpty

@AndroidEntryPoint
class FillingSetting01Fragment : Fragment() {

    private lateinit var binding: FragmentFillingSetting01Binding

    private var selectedButtonId: Int? = null

    private var selectedTimeText: String? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var isAM: Boolean = true

    private var aiId: String? = null
    private var wishId: Long? = null

    private lateinit var clockAdapter: ClockVPAdapter<ItemClockMiniPageBinding>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFillingSetting01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val selectedBg = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val selectedText = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.teumteum_gray)
        val defaultText = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        val title = arguments?.getString("title")
        setTitle(title.toString())

        val time = arguments?.getString("time")
        setTime(time.toString())

        aiId = arguments?.getString("ai_id")
        wishId = arguments?.getLong("wish_id", -1L)
            ?.takeIf { it > 0L }

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        fun resetButtons() {
            listOf(
                binding.select01Button,
                binding.select02Button,
                binding.select03Button,
                binding.select04Button
            ).forEach {
                it.setBackgroundColor(defaultBg)
                it.setTextColor(defaultText)
            }
        }

        binding.select01Button.setOnClickListener {
            resetButtons()
            binding.select01Button.setBackgroundColor(selectedBg)
            binding.select01Button.setTextColor(selectedText)
            selectedButtonId = R.id.select_01_button
            selectedTimeText = binding.timeSelect01Tv.text.toString()
            enableNextButton()
        }

        binding.select02Button.setOnClickListener {
            resetButtons()
            binding.select02Button.setBackgroundColor(selectedBg)
            binding.select02Button.setTextColor(selectedText)
            selectedButtonId = R.id.select_02_button
            selectedTimeText = binding.timeSelect02Tv.text.toString()

            // 시간 분리
            selectedTimeText?.let {
                val parts = it.split("~")
                if (parts.size == 2) {
                    selectedStartTime = parts[0]
                    selectedEndTime = parts[1]
                }
            }

            enableNextButton()
        }

        binding.select03Button.setOnClickListener {
            resetButtons()
            binding.select03Button.setBackgroundColor(selectedBg)
            binding.select03Button.setTextColor(selectedText)
            selectedButtonId = R.id.select_03_button
            selectedTimeText = binding.timeSelect03Tv.text.toString()

            // 시간 분리
            selectedTimeText?.let {
                val parts = it.split("~")
                if (parts.size == 2) {
                    selectedStartTime = parts[0]
                    selectedEndTime = parts[1]
                }
            }

            enableNextButton()
        }

        binding.select04Button.setOnClickListener {
            resetButtons()
            binding.select04Button.setBackgroundColor(selectedBg)
            binding.select04Button.setTextColor(selectedText)
            selectedButtonId = R.id.select_04_button
            selectedTimeText = binding.timeSelect04Tv.text.toString()

            // 시간 분리
            selectedTimeText?.let {
                val parts = it.split("~")
                if (parts.size == 2) {
                    selectedStartTime = parts[0]
                    selectedEndTime = parts[1]
                }
            }

            enableNextButton()
        }

        binding.nextBtn.setOnClickListener {
            when (selectedButtonId) {
                R.id.select_01_button -> {
                    val fragment = FillingSetting03Fragment().apply {
                        arguments = Bundle().apply {
                            aiId?.let   { putString("ai_id", it) }
                            wishId?.let { putLong("wish_id", it) }
                            putString("title", title)
                            putString("time", time)
                            putString("selected_time", selectedTimeText)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                R.id.select_02_button, R.id.select_03_button, R.id.select_04_button -> {
                    val fragment = FillingSetting02Fragment().apply {
                        arguments = Bundle().apply {
                            aiId?.let   { putString("ai_id", it) }
                            wishId?.let { putLong("wish_id", it) }
                            putString("title", title)
                            putString("time", time)
                            putString("selected_time", selectedTimeText)
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

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupClockPager()

        binding.amPmTv.setOnClickListener {
            val amPos = clockAdapter.positionOf(ClockHalf.AM)
            val pmPos = clockAdapter.positionOf(ClockHalf.PM)
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

        updateIndicator(isAM)
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

    private fun setTitle(title: String){
        binding.assignTitleTv.text = title
    }

    private fun setTime(time: String){
        binding.assignTimeTv.text = time
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
}