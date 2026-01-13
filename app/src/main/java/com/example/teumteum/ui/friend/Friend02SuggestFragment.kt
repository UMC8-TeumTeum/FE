package com.example.teumteum.ui.friend

import com.example.teumteum.ui.friend.adapter.TimeCardAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.PossibleTimeRequest
import com.example.teumteum.data.remote.friend.model.ResendTeumRequest
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02SuggestBinding
import com.example.teumteum.ui.friend.adapter.FriendRequestCardAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.enableTapToNext
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import kotlin.getValue

@AndroidEntryPoint
class Friend02SuggestFragment : Fragment() {

    private var _binding: FragmentFriend02SuggestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    private lateinit var timeCardAdapter: TimeCardAdapter
    private var teumList: List<TeumReceivedItem> = emptyList()
    private var responseId: Int = -1

    private val viewModel: FriendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02SuggestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바텀 네비게이션 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        //  전달받은 데이터 꺼내기
        teumList = arguments?.getParcelableArrayList("teumList") ?: emptyList()
        responseId = arguments?.getInt("responseId") ?: -1

        //  어댑터 연결
        adapter = FriendRequestCardAdapter(teumList)
        binding.requestViewPager.adapter = adapter

        //  뒤로가기 버튼 처리
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02PossibleTimeFragment.newInstance(ArrayList(teumList), responseId))
                .addToBackStack(null)
                .commit()
        }

        //  전송 버튼 클릭 시 → 재요청 및 FriendSendFragment 이동
        binding.btnSend.setOnClickListener {
            val selected = timeCardAdapter.getSelectedItem()
            Log.d("SELECTED_TIME_CARD", selected.toString())
            if (selected == null) {
                Toast.makeText(requireContext(), "시간을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 현재 보여주는 요청(카드) 기준 정보
            val currentItem = teumList.firstOrNull { it.responseId == responseId } ?: teumList.firstOrNull()
            if (currentItem == null) {
                return@setOnClickListener
            }

            // 로딩 중 중복 클릭 방지
            binding.btnSend.isEnabled = false

            viewModel.resendTeumRequest(
                currentItem.requestId,
                ResendTeumRequest(
                    startTime = convert24To00(selected.startTime),
                    endTime = convert24To00(selected.endTime)
                ),
                onSuccess = {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, FriendSendFragment())
                        .addToBackStack(null)
                        .commit()
                },
                onError = { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    binding.btnSend.isEnabled = true
                }
            )
        }

        setupTimeCardRecyclerView()
        observeViewModel()

        val currentItem = teumList.firstOrNull { it.responseId == responseId } ?: teumList.firstOrNull()
        val requesterId = currentItem?.senderUser?.userId
        if (requesterId == null) {
            return
        }

        // 2) 날짜 받기
        val selectedDate = currentItem.date

        // 3) Request 생성 (내 아이디 + 요청자 아이디)
        val request = PossibleTimeRequest(
            userIds = listOfNotNull(requesterId),
            date = selectedDate
        )

        // 4) API 호출
        viewModel.getPossibleTimeWithFriend(request)
    }

    private fun showCustomTimePicker(
        initial: String,
        onPicked: (String) -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null, false)
        val am = dialogView.findViewById<NumberPicker>(R.id.ampmPicker01Np)
        val h = dialogView.findViewById<NumberPicker>(R.id.hourPicker01Np)
        val m = dialogView.findViewById<NumberPicker>(R.id.minutePicker01Np)
        val mins = arrayOf("00","10","20","30","40","50")

        am.minValue = 0
        am.maxValue = 1
        am.displayedValues = arrayOf("AM","PM")
        am.wrapSelectorWheel = true

        h.minValue = 1
        h.maxValue = 12
        h.wrapSelectorWheel = true

        m.minValue = 0
        m.maxValue = mins.size-1
        m.displayedValues = mins
        m.wrapSelectorWheel = true

        am.enableTapToNext(wrap = true)
        h.enableTapToNext(wrap = true)
        m.enableTapToNext(wrap = true)

        // 초기값 세팅
        runCatching {
            val (ih, im) = initial.split(":").map { it.toInt() }
            val isAm = ih < 12
            am.value = if (isAm) 0 else 1
            val th = if (ih % 12 == 0) 12 else ih % 12
            h.value = th
            m.value = mins.indexOf(String.format("%02d", im)).coerceAtLeast(0)
        }

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val isAm = am.value == 0
            var hour24 = h.value % 12
            if (!isAm) hour24 += 12
            val mm = mins[m.value]
            val picked = String.format("%02d:%s", hour24, mm)
            onPicked(picked)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupTimeCardRecyclerView() {
        timeCardAdapter = TimeCardAdapter { position, isStart, startBound, endBound, current ->
            showCustomTimePicker(initial = current) { picked ->
                // 카드의 허용 범위 [startBound, endBound] 검사
                if (!isWithinRange(picked, startBound, endBound)) {
                    Toast.makeText(requireContext(), "가능한 시간대에서 벗어났어요!", Toast.LENGTH_SHORT).show()
                    return@showCustomTimePicker
                }

                timeCardAdapter.updateTime(position, isStart, picked)
            }
        }
        binding.possibleTimeRc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timeCardAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.possibleTimeList.observe(viewLifecycleOwner) { list ->
            Log.d("DEBUG", "observeViewModel triggered: ${list.size}개")

            val nonNullList = list.filterNotNull()
            Log.d("DEBUG", "after filterNotNull: ${nonNullList.size}개")

            timeCardAdapter.setData(nonNullList)
        }
    }

    //정해진 시간 범위의 시간으로 선택했는지 확인
    private fun isWithinRange(picked: String, min: String, max: String): Boolean {
        val normMax = if (max == "24:00") "23:59" else max
        val t = LocalTime.parse(picked)
        val tMin = LocalTime.parse(min)
        val tMax = LocalTime.parse(normMax)
        return !t.isBefore(tMin) && !t.isAfter(tMax)
    }

    //24:00 -> 00:00 변환
    private fun convert24To00(timeStr: String): String {
        return if (timeStr == "24:00") "00:00" else timeStr
    }

    companion object {
        fun newInstance(responseId: Int, teumList: ArrayList<TeumReceivedItem>): Friend02SuggestFragment {
            return Friend02SuggestFragment().apply {
                arguments = Bundle().apply {
                    putInt("responseId", responseId)
                    putParcelableArrayList("teumList", teumList)
                }
            }
        }
    }
}
