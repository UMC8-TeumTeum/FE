package com.example.teumteum.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.TimeBlock
import com.example.teumteum.data.TimeType
import com.example.teumteum.databinding.FragmentWishSetting01Binding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.google.android.material.bottomnavigation.BottomNavigationView

class WishSetting01Fragment : Fragment() {

    private lateinit var binding: FragmentWishSetting01Binding

    private var selectedButtonId: Int? = null

    private var selectedTimeText: String? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    private val fullDaySchedule = listOf(
        TimeBlock(0, 360, TimeType.SLEEP),   // 00:00 ~ 06:00
        TimeBlock(360, 580, TimeType.TODO),  // 06:00 ~ 09:40
        TimeBlock(720, 860, TimeType.TODO),  // 12:00 ~ 14:20
        TimeBlock(870, 930, TimeType.EMPTY), // 14:30 ~ 15:30
        TimeBlock(930, 1050, TimeType.TODO), // 15:30 ~ 17:30
        TimeBlock(1110, 1200, TimeType.TODO),// 18:30 ~ 20:00
        TimeBlock(1200, 1320, TimeType.EMPTY),// 20:00 ~ 22:00
        TimeBlock(1320, 1440, TimeType.SLEEP)// 22:00 ~ 24:00
    )

    private var isAM: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishSetting01Binding.inflate(inflater, container, false)
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
                    val fragment = WishSetting03Fragment().apply {
                        arguments = Bundle().apply {
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
                    val fragment = WishSetting02Fragment().apply {
                        arguments = Bundle().apply {
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

        ChartUtils.setupPieChart(binding.clockChart)

        val sleepBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)

        binding.clockChart.renderer = IconPieChartRenderer(
            binding.clockChart,
            binding.clockChart.animator,
            binding.clockChart.viewPortHandler,
            sleepBitmap
        )

        updateTimeChart(isAM)
        updateIndicator(isAM)

        binding.amPmTv.setOnClickListener {
            isAM = !isAM
            updateTimeChart(isAM)
            updateIndicator(isAM)
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

    private fun updateTimeChart(isAM: Boolean) {
        val halfDayBlocks = ChartUtils.splitAndFillTimeBlocks(fullDaySchedule, isAM)
        ChartUtils.setTimePieChartData(requireContext(), binding.clockChart, halfDayBlocks)
    }

    private fun updateIndicator(isAM: Boolean) {
        val leftView = binding.leftView
        val rightView = binding.rightView

        if (isAM) {
            //왼쪽이 점, 오른쪽이 막대
            leftView.layoutParams.width = dpToPx(4)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            rightView.layoutParams.width = dpToPx(28)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar_purple)

            binding.amPmTv.text="AM"
        } else {
            //왼쪽이 막대, 오른쪽이 점
            leftView.layoutParams.width = dpToPx(28)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar_purple)

            rightView.layoutParams.width = dpToPx(4)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            binding.amPmTv.text="PM"
        }

        leftView.requestLayout()
        rightView.requestLayout()


    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

}