package com.example.teumteum.ui.friend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateMatchingDetailBinding
import com.example.teumteum.ui.friend.adapter.TimeCardAdapter
import com.example.teumteum.ui.friend.data.SelectedTime
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signup.SignUpActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import kotlin.getValue

@AndroidEntryPoint
class FriendRoommateMatchingDetailFragment : Fragment() {

    private lateinit var binding: FragmentFriendRoommateMatchingDetailBinding

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var lastSelectedCardView: View? = null
    private lateinit var timeCardAdapter: TimeCardAdapter

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendRoommateMatchingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(75)
        (activity as? MainActivity)?.hideBottomBar()

        val fullText = "틈 요청 제목을 작성해주세요*"
        val spannable = android.text.SpannableString(fullText)
        val starIndex = fullText.indexOf("*")

        if (starIndex != -1) {
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#7770FE")),
                starIndex,
                starIndex + 1,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.teumRequestTitle.text = spannable
        }


        binding.clearTitleBtn.setOnClickListener {
            binding.editTextTitle.text.clear()
        }

        binding.clearDetailBtn.setOnClickListener {
            binding.editTextDetail.text.clear()
        }

        binding.editTextTitle.addTextChangedListener {
            updateNextButtonState()
        }

        binding.editTextDetail.addTextChangedListener {
            updateNextButtonState()
        }

        // 전송할게요 버튼 클릭 시 dialogFragment 화면 띄우기
        binding.sendBtn.setOnClickListener {
            setViewModelData()

            val dialog = FriendMatchingPreviewDialog()
            dialog.show(parentFragmentManager, "PreviewDialog")
        }


        // 뒤로가기 버튼 처리
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupTimeCardRecyclerView()
        observeViewModel()
        timeCardAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                // notify 이후 다음 프레임에 상태 읽기 (선택 반영 보장)
                binding.possibleTimeRc.post { updateNextButtonState() }
            }
            override fun onChanged() = onItemRangeChanged(0, timeCardAdapter.itemCount)
        })
//        setupTimePickers()

        // 초기 버튼 상태 설정
        binding.sendBtn.isEnabled = false
        binding.sendBtn.setBackgroundColor(android.graphics.Color.parseColor("#F6F6F6"))

    }

//    private fun setupTimePickers() {
//        val startTextViews = listOf(binding.startChoiceTv1, binding.startChoiceTv2, binding.startChoiceTv3)
//        val endTextViews = listOf(binding.endChoiceTv1, binding.endChoiceTv2, binding.endChoiceTv3)
//
//        val startContainers = listOf(binding.sleepStartContainer1, binding.sleepStartContainer2, binding.sleepStartContainer3)
//        val endContainers = listOf(binding.sleepEndContainer1, binding.sleepEndContainer2, binding.sleepEndContainer3)
//
//        val upArrows = listOf(binding.startUpArrow1, binding.startUpArrow2, binding.startUpArrow3, binding.endUpArrow1, binding.endUpArrow2, binding.endUpArrow3)
//        val downArrows = listOf(binding.startDownArrow1, binding.startDownArrow2, binding.startDownArrow3, binding.endDownArrow1, binding.endDownArrow2, binding.endDownArrow3)
//
//        startContainers.zip(startTextViews).forEach { (container, tv) ->
//            container.setOnClickListener { showCustomTimePicker(tv) }
//        }
//        endContainers.zip(endTextViews).forEach { (container, tv) ->
//            container.setOnClickListener { showCustomTimePicker(tv) }
//        }
//
//        upArrows.forEach { arrow ->
//            arrow.setOnClickListener {
//                val target = arrow.tag as? TextView
//                target?.let { increaseHour(it) }
//            }
//        }
//        downArrows.forEach { arrow ->
//            arrow.setOnClickListener {
//                val target = arrow.tag as? TextView
//                target?.let { decreaseHour(it) }
//            }
//        }
//
//        // 직접 연결 (tag 미사용 방식)
//        binding.startUpArrow1.setOnClickListener { increaseHour(binding.startChoiceTv1) }
//        binding.startDownArrow1.setOnClickListener { decreaseHour(binding.startChoiceTv1) }
//        binding.endUpArrow1.setOnClickListener { increaseHour(binding.endChoiceTv1) }
//        binding.endDownArrow1.setOnClickListener { decreaseHour(binding.endChoiceTv1) }
//
//        binding.startUpArrow2.setOnClickListener { increaseHour(binding.startChoiceTv2) }
//        binding.startDownArrow2.setOnClickListener { decreaseHour(binding.startChoiceTv2) }
//        binding.endUpArrow2.setOnClickListener { increaseHour(binding.endChoiceTv2) }
//        binding.endDownArrow2.setOnClickListener { decreaseHour(binding.endChoiceTv2) }
//
//        binding.startUpArrow3.setOnClickListener { increaseHour(binding.startChoiceTv3) }
//        binding.startDownArrow3.setOnClickListener { decreaseHour(binding.startChoiceTv3) }
//        binding.endUpArrow3.setOnClickListener { increaseHour(binding.endChoiceTv3) }
//        binding.endDownArrow3.setOnClickListener { decreaseHour(binding.endChoiceTv3) }
//    }

//    private fun showCustomTimePicker(targetTextView: TextView) {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null, false)
//
//        val ampmPicker = dialogView.findViewById<NumberPicker>(R.id.ampmPicker01Np)
//        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker01Np)
//        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker01Np)
//
//        val minuteValues = arrayOf("00", "10", "20", "30", "40", "50")
//
//        ampmPicker.minValue = 0
//        ampmPicker.maxValue = 1
//        ampmPicker.displayedValues = arrayOf("AM", "PM")
//
//        hourPicker.minValue = 1
//        hourPicker.maxValue = 12
//        hourPicker.wrapSelectorWheel = true
//
//        minutePicker.minValue = 0
//        minutePicker.maxValue = minuteValues.size - 1
//        minutePicker.displayedValues = minuteValues
//        minutePicker.wrapSelectorWheel = true
//
//        val dialog = BottomSheetDialog(requireContext())
//        dialog.setContentView(dialogView)
//
//        dialog.setOnShowListener { dialogInterface ->
//            val bottomSheet = (dialogInterface as BottomSheetDialog)
//                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
//        }
//
//        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
//            dialog.dismiss()
//        }
//
//        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
//            val isAm = ampmPicker.value == 0
//            var hour = hourPicker.value % 12
//            if (!isAm) hour += 12
//            if (hour == 0) hour = 0
//
//            val minute = minuteValues[minutePicker.value]
//            val timeText = String.format("%02d:%s", hour, minute)
//
//            targetTextView.text = timeText
//
//            val parentCard = targetTextView.parent?.parent as? ViewGroup
//            if (parentCard != null) {
//                lastSelectedCardView?.background = null
//                parentCard.setBackgroundResource(R.drawable.friend_time_card_bg_selected)
//                lastSelectedCardView = parentCard
//            }
//
//            updateNextButtonState()
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }

//    private fun increaseHour(targetTextView: TextView) {
//        val currentText = targetTextView.text.toString()
//        if (currentText.isNotBlank()) {
//            val parts = currentText.split(":")
//            if (parts.size == 2) {
//                var hour = parts[0].toIntOrNull() ?: return
//                val minute = parts[1]
//
//                hour = (hour + 1) % 24
//                val newTime = String.format("%02d:%s", hour, minute)
//                targetTextView.text = newTime
//                updateNextButtonState()
//            }
//        }
//    }
//
//    private fun decreaseHour(targetTextView: TextView) {
//        val currentText = targetTextView.text.toString()
//        if (currentText.isNotBlank()) {
//            val parts = currentText.split(":")
//            if (parts.size == 2) {
//                var hour = parts[0].toIntOrNull() ?: return
//                val minute = parts[1]
//
//                hour = if (hour == 0) 23 else hour - 1
//                val newTime = String.format("%02d:%s", hour, minute)
//                targetTextView.text = newTime
//                updateNextButtonState()
//            }
//        }
//    }

    private fun updateNextButtonState() {

        val isTimeSelected = timeCardAdapter.getSelectedItem() != null
        val isTitleFilled = binding.editTextTitle.text.toString().isNotBlank()
        val isEnabled = isTimeSelected && isTitleFilled

        binding.sendBtn.isEnabled = isEnabled
        binding.sendBtn.setBackgroundColor(
            if (isEnabled) 0xFF0F0F0F.toInt() else 0xFFF6F6F6.toInt()
        )
        binding.sendBtn.setTextColor(
            if (isEnabled) 0xFFFFFFFF.toInt() else 0xFF0F0F0F.toInt()
        )
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
                binding.possibleTimeRc.post { updateNextButtonState() }
            }

        }
        binding.possibleTimeRc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timeCardAdapter
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

    private fun showCustomTimePicker(
        initial: String,
        onPicked: (String) -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null, false)
        val am = dialogView.findViewById<NumberPicker>(R.id.ampmPicker01Np)
        val h = dialogView.findViewById<NumberPicker>(R.id.hourPicker01Np)
        val m = dialogView.findViewById<NumberPicker>(R.id.minutePicker01Np)
        val mins = arrayOf("00","10","20","30","40","50")

        am.minValue = 0; am.maxValue = 1; am.displayedValues = arrayOf("AM","PM")
        h.minValue = 1; h.maxValue = 12; h.wrapSelectorWheel = true
        m.minValue = 0; m.maxValue = mins.size-1; m.displayedValues = mins; m.wrapSelectorWheel = true

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
        updateNextButtonState()
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.possibleTimeList.observe(viewLifecycleOwner) { list ->
            val nonNullList = list.filterNotNull()
            timeCardAdapter.setData(nonNullList)

            updateNextButtonState()
        }
    }

    private fun setViewModelData() {
        val selectedTime = timeCardAdapter.getSelectedItem()
        viewModel.setTeumRequestSelectedTime(
            SelectedTime(
                startTime = convert24To00(selectedTime!!.startTime),
                endTime = convert24To00(selectedTime.endTime)
            )
        )
        viewModel.setTeumRequestTitle(binding.editTextTitle.text.toString())
        if(binding.editTextDetail.text.isEmpty()){
            //기본 멘트
            viewModel.setTeumRequestDescription("같이 빈틈을 채워봐요.")
        }else{
            viewModel.setTeumRequestDescription(binding.editTextDetail.text.toString())
        }

    }

    //24:00 -> 00:00 변환
    private fun convert24To00(timeStr: String): String {
        return if (timeStr == "24:00") "00:00" else timeStr
    }
}