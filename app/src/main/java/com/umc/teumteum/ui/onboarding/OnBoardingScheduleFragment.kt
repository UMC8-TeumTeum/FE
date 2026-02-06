package com.umc.teumteum.ui.onboarding

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.onboarding.model.Schedule
import com.umc.teumteum.data.remote.onboarding.model.ScheduleRequest
import com.umc.teumteum.data.remote.onboarding.model.Week
import com.umc.teumteum.databinding.FragmentOnBoardingScheduleBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.ui.onboarding.adapter.ScheduleAdapter
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.Calendar

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
        observeViewModel()

        val initialMarginBottom =
            (binding.nextBtn.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.nextBtn) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + bottomInset
            }
            insets
        }

        dayTextViews = listOf(
            binding.sunTv, binding.monTv, binding.tueTv,
            binding.wedTv, binding.thuTv, binding.friTv, binding.satTv
        )

        expandTouchAreas(dayTextViews, extraDp = 18)

        setupDaySelection()
        updateDayHighlight(selectedDayIndex)
        viewModel.updateCurrentDaySchedule(selectedDayIndex)

        binding.scheduleRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
            scheduleAdapter.onItemClick = { clicked ->
                BottomSheetScheduleEditFragment(
                    selectedDayIndex = selectedDayIndex,
                    target = clicked,
                ).show(parentFragmentManager, "BottomSheetScheduleEditFragment")
            }
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
        dayTextViews[selectedDayIndex].setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )

        dayTextViews[selectedIndex].background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_day_selected)
        dayTextViews[selectedIndex].setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        selectedDayIndex = selectedIndex
        viewModel.updateCurrentDaySchedule(selectedDayIndex)
    }

    private fun getScheduleRequest(): ScheduleRequest {
        val allSchedules =
            mutableListOf<Schedule>()
        val weekMap = mapOf(
            0 to Week.SUNDAY, 1 to Week.MONDAY, 2 to Week.TUESDAY,
            3 to Week.WEDNESDAY, 4 to Week.THURSDAY, 5 to Week.FRIDAY, 6 to Week.SATURDAY
        )

        viewModel.scheduleMap.forEach { (dayIndex, schedules) ->
            val weekdayEnum = weekMap[dayIndex] ?: Week.SUNDAY
            schedules.forEach { s ->
                allSchedules.add(
                    Schedule(
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

                }

                else -> Unit
            }
        }

        viewModel.currentDayScheduleList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.submitList(list)
        }
    }

    private fun navigateToNext() {
        // SignUpActivity의 메서드를 통해 다음 단계로 이동
        (activity as? SignUpActivity)?.proceedToNextOnboardingStep(this)
        viewModel.resetState()
    }

    private class MultiTouchDelegate(parent: View) : TouchDelegate(Rect(), parent) {
        private val delegates = mutableListOf<TouchDelegate>()

        fun add(delegate: TouchDelegate) {
            delegates.add(delegate)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return delegates.any { it.onTouchEvent(event) }
        }
    }

    private fun expandTouchAreas(views: List<View>, extraDp: Int = 16) {
        if (views.isEmpty()) return
        val parent = views.first().parent as? View ?: return

        parent.post {
            val density = parent.resources.displayMetrics.density
            val extraPx = (extraDp * density).toInt()

            val multi = MultiTouchDelegate(parent)

            views.forEach { v ->
                val rect = Rect()
                v.getHitRect(rect)
                rect.inset(-extraPx, -extraPx)
                multi.add(TouchDelegate(rect, v))
            }

            parent.touchDelegate = multi
        }
    }

}