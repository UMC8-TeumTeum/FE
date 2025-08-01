package com.example.teumteum.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.entities.Alarm
import com.example.teumteum.data.entities.AlarmItem
import com.example.teumteum.databinding.FragmentHomeAlarmBinding
import com.example.teumteum.ui.alarm.view.AlarmListView
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlarmFragment : Fragment(), AlarmListView {

    private lateinit var binding: FragmentHomeAlarmBinding

    private lateinit var adapter: AlarmRVAdapter

    private var alarmDummyList = mutableListOf(
        AlarmItem(1, "문혜원", "sample1", "새롭게 온 요청이 있어요!", "3분", false),
        AlarmItem(2, "하수연", "sample2", "친구에게 보낸 요청이 수락됐어요", "3시간", false),
        AlarmItem(3, "이솔민", "sample3", "친구에게 보낸 요청이 거절됐어요", "3주", false),
        AlarmItem(4, "장채미", "sample4", "친구가 새로운 시간을 제안했어요", "10주", false),
        AlarmItem(5, "하수연", "sample5", "친구와의 틈 약속이 취소됐어요", "33주", false),
        AlarmItem(6, "장지니", "sample6", "친구가 새로운 시간을 제안했어요", "36주", false),
        AlarmItem(7, "최원", "sample7", "친구에게 보낸 요청이 수락됐어요", "40주", false),
        AlarmItem(8, "박애플", "sample8", "새롭게 온 요청이 있어요!", "58주", false),
        AlarmItem(9, "김결", "sample9", "새롭게 온 요청이 있어요!", "70주", false),
        AlarmItem(10, "양나루", "sample10", "친구에게 보낸 요청이 수락됐어요", "89주", false),
        AlarmItem(11, "강현다", "sample11", "친구에게 보낸 요청이 수락됐어요", "91주", false)
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeAlarmBinding.inflate(inflater, container, false)
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

    override fun onGetAlarmListSuccess(alarmList: List<Alarm>) {
        Toast.makeText(requireContext(), "알림이 성공적으로 조회되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onGetAlarmListFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> message ?: "조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }
}