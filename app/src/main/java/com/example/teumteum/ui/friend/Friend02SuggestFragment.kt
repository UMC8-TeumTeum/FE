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
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02SuggestBinding
import com.example.teumteum.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Friend02SuggestFragment : Fragment() {

    private var _binding: FragmentFriend02SuggestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    private var teumList: List<TeumReceivedItem> = emptyList()
    private var responseId: Int = -1

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

        //  전달받은 데이터 꺼내기
        teumList = arguments?.getParcelableArrayList("teumList") ?: emptyList()
        responseId = arguments?.getInt("responseId") ?: -1

        //  어댑터 연결
        adapter = FriendRequestCardAdapter(teumList)
        binding.requestViewPager.adapter = adapter

        // 바텀 네비게이션 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        //  뒤로가기 버튼 처리
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02PossibleTimeFragment.newInstance(ArrayList(teumList), responseId))
                .addToBackStack(null)
                .commit()
        }

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

        //  전송 버튼 클릭 시 → FriendSendFragment 이동
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

        dialog.setOnShowListener { dialogInterface ->
            (dialogInterface as? BottomSheetDialog)?.let { bottomSheetDialog ->
                bottomSheetDialog.behavior.addBottomSheetCallback(object :
                    com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        // 상태 변경 시
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // 배경 변경
                        bottomSheet.setBackgroundResource(R.drawable.calendar_background)
                    }
                })
            }
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val isAm = ampmPicker.value == 0
            var hour = hourPicker.value % 12
            if (!isAm) hour += 12
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
