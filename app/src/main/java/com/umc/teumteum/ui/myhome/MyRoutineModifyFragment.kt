package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.onboarding.model.Week
import com.umc.teumteum.databinding.FragmentMyRoutineModifyBinding
import com.umc.teumteum.ui.main.MainActivity
import com.umc.teumteum.ui.myhome.adapter.MyRoutineAdapter
import com.umc.teumteum.ui.myhome.viewModel.MyRoutineViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class MyRoutineModifyFragment : Fragment() {

    private lateinit var binding: FragmentMyRoutineModifyBinding

    private val scheduleAdapter by lazy { MyRoutineAdapter() }
    private val viewModel: MyRoutineViewModel by activityViewModels()

    private lateinit var dayTextViews: List<TextView>
    private var selectedDayIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        binding = FragmentMyRoutineModifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        observeViewModel()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        dayTextViews = listOf(
            binding.sunTv, binding.monTv, binding.tueTv,
            binding.wedTv, binding.thuTv, binding.friTv, binding.satTv
        )

        binding.scheduleRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        setupDaySelection()

        updateDayHighlight(selectedDayIndex)

        fetchRoutine(selectedDayIndex)

        binding.fabAddIv.setOnClickListener {
            val existing = viewModel.routineMap[selectedDayIndex]?.toList() ?: emptyList()
            val bottomSheet = BottomSheetRoutineFragment(
                selectedDayIndex,
                existing
            )
            bottomSheet.show(parentFragmentManager, "BottomSheetRoutineFragment")
        }

        binding.scheduleRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }

        scheduleAdapter.setOnItemClickListener { selectedRoutine ->
            val existing = viewModel.routineMap[selectedDayIndex]?.toList().orEmpty()

            val bottomSheet = BottomSheetRoutineModifyFragment(
                selectedDayIndex = selectedDayIndex,
                existingSchedules = existing,
                targetRoutine = selectedRoutine
            )
            bottomSheet.show(parentFragmentManager, "BottomSheetRoutineModifyFragment")
        }
    }

    private fun setupDaySelection() {
        dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                updateDayHighlight(index)

                fetchRoutine(index)
            }
        }
    }

    private fun updateDayHighlight(selectedIndex: Int) {
        dayTextViews[selectedDayIndex].background = null
        dayTextViews[selectedDayIndex].setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )

        dayTextViews[selectedIndex].background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_day_selected)
        dayTextViews[selectedIndex].setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        )

        selectedDayIndex = selectedIndex

        viewModel.updateCurrentDaySchedule(selectedDayIndex)
    }

    private fun fetchRoutine(dayIndex: Int) {
        val week = when (dayIndex) {
            0 -> Week.SUNDAY
            1 -> Week.MONDAY
            2 -> Week.TUESDAY
            3 -> Week.WEDNESDAY
            4 -> Week.THURSDAY
            5 -> Week.FRIDAY
            6 -> Week.SATURDAY
            else -> Week.SUNDAY
        }
        viewModel.fetchMyRoutine(week)
    }

    private fun observeViewModel() {
        viewModel.currentDayRoutineList.observe(viewLifecycleOwner) { list ->
            scheduleAdapter.submitList(list)
        }
    }
}