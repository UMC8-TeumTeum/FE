package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriend02SuggestBinding
import com.example.teumteum.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class Friend02SuggestFragment : Fragment() {

    private var _binding: FragmentFriend02SuggestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter

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

        // ViewPager2 + Adapter 연결
        adapter = FriendRequestCardAdapter(getDummyList())
        binding.requestViewPager.adapter = adapter

        // 시간 카드 1 클릭 시
        binding.startTime1.setOnClickListener {
            showCustomTimePicker(binding.startTime1)
            highlightSelectedCard(isFirst = true)
        }
        binding.endTime1.setOnClickListener {
            showCustomTimePicker(binding.endTime1)
            highlightSelectedCard(isFirst = true)
        }

        // 시간 카드 2 클릭 시
        binding.startTime2.setOnClickListener {
            showCustomTimePicker(binding.startTime2)
            highlightSelectedCard(isFirst = false)
        }
        binding.endTime2.setOnClickListener {
            showCustomTimePicker(binding.endTime2)
            highlightSelectedCard(isFirst = false)
        }

        // 전송 버튼 클릭 시 이동
        binding.btnSend.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendSendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** 커스텀 TimePicker 다이얼로그 표시 */
    private fun showCustomTimePicker(targetTextView: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null, false)

        val ampmPicker = dialogView.findViewById<NumberPicker>(R.id.ampmPicker01Np)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker01Np)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker01Np)

        val minuteValues = arrayOf("00", "10", "20", "30", "40", "50")

        // Picker 초기화
        ampmPicker.minValue = 0
        ampmPicker.maxValue = 1
        ampmPicker.displayedValues = arrayOf("AM", "PM")

        hourPicker.minValue = 1
        hourPicker.maxValue = 12
        hourPicker.wrapSelectorWheel = true

        minutePicker.minValue = 0
        minutePicker.maxValue = minuteValues.size - 1
        minutePicker.displayedValues = minuteValues
        minutePicker.wrapSelectorWheel = true

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        // ✅ 배경 적용
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }

        // 취소 버튼
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        // 확인 버튼
        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val isAm = ampmPicker.value == 0
            var hour = hourPicker.value % 12
            if (!isAm) hour += 12
            if (hour == 0) hour = 0 // 12AM → 0시로

            val minute = minuteValues[minutePicker.value]
            val timeText = String.format("%02d:%s", hour, minute)

            targetTextView.text = timeText
            dialog.dismiss()
        }

        dialog.show()
    }

    /** 시간 카드 선택 강조 */
    private fun highlightSelectedCard(isFirst: Boolean) {
        if (isFirst) {
            binding.timeCard1.setBackgroundResource(R.drawable.friend_time_card_bg_selected)
            binding.timeCard2.setBackgroundResource(R.drawable.friend_time_card_bg_default)
        } else {
            binding.timeCard1.setBackgroundResource(R.drawable.friend_time_card_bg_default)
            binding.timeCard2.setBackgroundResource(R.drawable.friend_time_card_bg_selected)
        }
    }

    /** 예시 데이터 */
    private fun getDummyList(): List<FriendRequestData> = listOf(
        FriendRequestData(
            name = "이름",
            date = "25.05.02",
            time = "15:20 ~ 16:10",
            title = "강아지 산책 가자",
            desc = "모모랑 초코랑 종합천 한바퀴 쓰윽 돌고\n돌아오는 길에 호떡 먹자!"
        ),
        FriendRequestData(
            name = "보보",
            date = "25.05.03",
            time = "11:00 ~ 12:00",
            title = "산책 좋아하는 보보",
            desc = "동물병원 들렀다가 간식도 먹고 돌아오자!"
        )
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
