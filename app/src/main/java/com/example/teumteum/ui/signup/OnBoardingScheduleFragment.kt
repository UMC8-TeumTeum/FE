package com.example.teumteum.ui.signup

import BottomSheetScheduleFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingScheduleBinding
import kotlin.collections.toList
import com.example.teumteum.data.Schedule
import java.time.LocalTime
import java.util.Calendar
import kotlin.collections.forEachIndexed

class OnBoardingScheduleFragment : Fragment(){

    private lateinit var binding: FragmentOnBoardingScheduleBinding
    private val scheduleAdapter by lazy { ScheduleAdapter() }

    private lateinit var dayTextViews: List<TextView>

    private var selectedDayIndex = 0

    private val scheduleMap = mutableMapOf<Int, MutableList<Schedule>>()

    private var sleepStart: LocalTime? = null
    private var sleepEnd: LocalTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            sleepStart = it.getString("sleepStart")?.let { time -> LocalTime.parse(time) }
            sleepEnd = it.getString("sleepEnd")?.let { time -> LocalTime.parse(time) }
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
    ): View? {
        binding = FragmentOnBoardingScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(80)

        binding.nextBtn.setOnClickListener {
//            startActivity(Intent(requireContext(), MainActivity::class.java))
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingRemindFragment())
                .addToBackStack(null)
                .commit()
        }

        // RecyclerView 세팅
        binding.scheduleRv.adapter = scheduleAdapter
        binding.scheduleRv.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddIv.setOnClickListener {
            val list = scheduleMap.getOrPut(selectedDayIndex) { mutableListOf() }

            val bottomSheet = BottomSheetScheduleFragment(
                selectedDayIndex,
                list.toList(),
                onScheduleAdded = { schedule ->
                    list.add(schedule)
                    scheduleAdapter.submitList(list.toList())
                },
                sleepStart = sleepStart,
                sleepEnd = sleepEnd
            )

            bottomSheet.show(parentFragmentManager, "BottomSheetScheduleFragment")
        }

        dayTextViews = listOf(
            binding.sunTv, binding.monTv, binding.tueTv,
            binding.wedTv, binding.thuTv, binding.friTv, binding.satTv
        )

        setupDaySelection()
        updateDayHighlight(selectedDayIndex)

    }

    private fun setupDaySelection() {
        dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                updateDayHighlight(index)
            }
        }
    }

    //요일 선택했을 때 하이라이팅, 해당 요일 일정 보여주기
    private fun updateDayHighlight(selectedIndex: Int) {

        dayTextViews[selectedDayIndex].background = null
        dayTextViews[selectedDayIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        dayTextViews[selectedIndex].background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_day_selected)
        dayTextViews[selectedIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        selectedDayIndex = selectedIndex

        // 해당 요일의 일정 보여주기
        scheduleAdapter.submitList(scheduleMap[selectedDayIndex] ?: emptyList())
    }



}