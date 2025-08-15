package com.example.teumteum.ui.signup

import BottomSheetScheduleFragment
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingScheduleBinding
import kotlin.collections.toList
import com.example.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.model.Week
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.ui.signup.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.Calendar
import kotlin.collections.forEachIndexed

@AndroidEntryPoint
class OnBoardingScheduleFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingScheduleBinding
    private val scheduleAdapter by lazy { ScheduleAdapter() }

    private lateinit var dayTextViews: List<TextView>
    private var selectedDayIndex = 0
    private var sleepStart: LocalTime? = null
    private var sleepEnd: LocalTime? = null

    private val viewModel: OnBoardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val start = it.getString("sleepStart")
            val end = it.getString("sleepEnd")
            sleepStart = start?.let { LocalTime.parse(it) }
            sleepEnd = end?.let { LocalTime.parse(it) }
        }

        if (savedInstanceState == null) {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            selectedDayIndex = when (dayOfWeek) {
                Calendar.SUNDAY -> 0
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 0
            }
        } else {
            selectedDayIndex = savedInstanceState.getInt("selectedDayIndex", 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedDayIndex", selectedDayIndex)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(80)

        observeViewModel()

        dayTextViews = listOf(
            binding.sunTv, binding.monTv, binding.tueTv,
            binding.wedTv, binding.thuTv, binding.friTv, binding.satTv
        )
        setupDaySelection()
        updateDayHighlight(selectedDayIndex)
        viewModel.updateCurrentDaySchedule(selectedDayIndex)

        binding.scheduleRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        binding.fabAddIv.setOnClickListener {
            val existing = viewModel.scheduleMap[selectedDayIndex]?.toList() ?: emptyList()
            val bottomSheet = BottomSheetScheduleFragment(
                selectedDayIndex,
                existing
            )
            bottomSheet.show(parentFragmentManager, "BottomSheetScheduleFragment")
        }

        binding.nextBtn.setOnClickListener {
            val hasAnySchedule = viewModel.scheduleMap.values.any { it.isNotEmpty() }
            if (hasAnySchedule) {
                val request = getScheduleRequest()
                viewModel.postSchedule(request)
            } else {
                navigateToNext()
            }
        }
    }

    private fun setupDaySelection() {
        dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener { updateDayHighlight(index) }
        }
    }

    private fun updateDayHighlight(selectedIndex: Int) {
        dayTextViews[selectedDayIndex].background = null
        dayTextViews[selectedDayIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        dayTextViews[selectedIndex].background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_day_selected)
        dayTextViews[selectedIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        selectedDayIndex = selectedIndex
//        scheduleAdapter.submitList(viewModel.scheduleMap[selectedDayIndex] ?: emptyList())

        viewModel.updateCurrentDaySchedule(selectedDayIndex)
    }

    private fun getScheduleRequest(): ScheduleRequest {
        val allSchedules = mutableListOf<com.example.teumteum.data.remote.onboarding.model.Schedule>()
        val weekMap = mapOf(
            0 to Week.SUNDAY, 1 to Week.MONDAY, 2 to Week.TUESDAY,
            3 to Week.WEDNESDAY, 4 to Week.THURSDAY, 5 to Week.FRIDAY, 6 to Week.SATURDAY
        )

        viewModel.scheduleMap.forEach { (dayIndex, schedules) ->
            val weekdayEnum = weekMap[dayIndex] ?: Week.SUNDAY
            schedules.forEach { s ->
                allSchedules.add(
                    com.example.teumteum.data.remote.onboarding.model.Schedule(
                        title = s.title,
                        description = s.description,
                        weekday = weekdayEnum,
                        startTime = s.startTime.toString(),
                        endTime = s.endTime.toString()
                    )
                )
            }
        }

        return ScheduleRequest(routine = allSchedules)
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Success -> {
                    navigateToNext()
                }
                is OnBoardingUiState.Error -> {
//                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    if (state.code == "ONBOARDING4001") {
                        Log.d("ScheduleFragment", "ONBOARDING4001 - 강제 이동")
                        navigateToNext()
                    }
                }
                else -> Unit
            }
        }

        viewModel.currentDayScheduleList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.submitList(list)
        }
    }

    private fun navigateToNext() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OnBoardingRemindFragment())
            .addToBackStack(null)
            .commit()

        viewModel.resetState()
    }
}