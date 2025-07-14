package com.example.teumteum.ui.singup

import BottomSheetScheduleFragment
import android.content.Intent
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
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.register.BottomSheetRegisterFragment
import kotlin.collections.toList
import com.example.teumteum.data.Schedule

class OnBoardingScheduleFragment : Fragment(){

    private lateinit var binding: FragmentOnBoardingScheduleBinding
    private val scheduleAdapter by lazy { ScheduleAdapter() }

    private val dayTextViews by lazy {
        listOf(binding.sunTv, binding.monTv, binding.tueTv, binding.wedTv, binding.thuTv, binding.friTv, binding.satTv)
    }

    private var selectedDayIndex = 0

    private val scheduleMap = mutableMapOf<Int, MutableList<Schedule>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        (activity as? SignUpActivity)?.setProgressBar(50)

        binding.nextBtn.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        // RecyclerView 세팅
        binding.scheduleRv.adapter = scheduleAdapter
        binding.scheduleRv.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddIv.setOnClickListener {
            val bottomSheet = BottomSheetScheduleFragment(selectedDayIndex) { schedule ->
                val list = scheduleMap.getOrPut(selectedDayIndex) { mutableListOf() }
                list.add(schedule)
                scheduleAdapter.submitList(list.toList())
            }
            bottomSheet.show(parentFragmentManager, "BottomSheetScheduleFragment")
        }

        setupDaySelection()

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