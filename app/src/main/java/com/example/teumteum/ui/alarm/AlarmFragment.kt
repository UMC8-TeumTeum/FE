package com.example.teumteum.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.entities.AlarmItem
import com.example.teumteum.databinding.FragmentAlarmBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlarmFragment : Fragment() {

    private lateinit var binding: FragmentAlarmBinding

    private lateinit var adapter: AlarmRVAdapter

    private var alarmDummyList = mutableListOf(
        AlarmItem(1, "문혜원", "sample1", "새롭게 온 요청이 있어요!", "3분", false),
        AlarmItem(2, "하수연", "sample2", "친구에게 보낸 요청이 수락됐어요", "3시간", false),
        AlarmItem(3, "이솔민", "sample3", "친구에게 보낸 요청이 거절됐어요", "3주", false),
        AlarmItem(4, "장채미", "sample4", "친구가 새로운 시간을 제안했어요", "10주", false),
        AlarmItem(5, "하수연", "sample5", "친구와의 틈 약속이 취소됐어요", "33주", false)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        adapter = AlarmRVAdapter(parentFragmentManager, alarmDummyList)
        binding.alarmRv.adapter = adapter

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}