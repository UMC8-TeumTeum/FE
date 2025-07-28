package com.example.teumteum.ui.friend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateMatchingDetailBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signup.SignUpActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class FriendRoommateMatchingDetailFragment : Fragment() {

    private lateinit var binding: FragmentFriendRoommateMatchingDetailBinding

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var lastSelectedCardView: View? = null

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

        binding.btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateFriendFragment())
                .addToBackStack(null)
                .commit()
        }

        setupTimePickers()

        // 초기 버튼 상태 설정
        binding.sendBtn.isEnabled = false
        binding.sendBtn.setBackgroundColor(android.graphics.Color.parseColor("#F6F6F6"))

    }

    private fun setupTimePickers() {
        val startTextViews = listOf(binding.startChoiceTv1, binding.startChoiceTv2, binding.startChoiceTv3)
        val endTextViews = listOf(binding.endChoiceTv1, binding.endChoiceTv2, binding.endChoiceTv3)

        val startContainers = listOf(binding.sleepStartContainer1, binding.sleepStartContainer2, binding.sleepStartContainer3)
        val endContainers = listOf(binding.sleepEndContainer1, binding.sleepEndContainer2, binding.sleepEndContainer3)

        val upArrows = listOf(binding.startUpArrow1, binding.startUpArrow2, binding.startUpArrow3, binding.endUpArrow1, binding.endUpArrow2, binding.endUpArrow3)
        val downArrows = listOf(binding.startDownArrow1, binding.startDownArrow2, binding.startDownArrow3, binding.endDownArrow1, binding.endDownArrow2, binding.endDownArrow3)

        startContainers.zip(startTextViews).forEach { (container, tv) ->
            container.setOnClickListener { showCustomTimePicker(tv) }
        }
        endContainers.zip(endTextViews).forEach { (container, tv) ->
            container.setOnClickListener { showCustomTimePicker(tv) }
        }

        upArrows.forEach { arrow ->
            arrow.setOnClickListener {
                val target = arrow.tag as? TextView
                target?.let { increaseHour(it) }
            }
        }
        downArrows.forEach { arrow ->
            arrow.setOnClickListener {
                val target = arrow.tag as? TextView
                target?.let { decreaseHour(it) }
            }
        }

        // 직접 연결 (tag 미사용 방식)
        binding.startUpArrow1.setOnClickListener { increaseHour(binding.startChoiceTv1) }
        binding.startDownArrow1.setOnClickListener { decreaseHour(binding.startChoiceTv1) }
        binding.endUpArrow1.setOnClickListener { increaseHour(binding.endChoiceTv1) }
        binding.endDownArrow1.setOnClickListener { decreaseHour(binding.endChoiceTv1) }

        binding.startUpArrow2.setOnClickListener { increaseHour(binding.startChoiceTv2) }
        binding.startDownArrow2.setOnClickListener { decreaseHour(binding.startChoiceTv2) }
        binding.endUpArrow2.setOnClickListener { increaseHour(binding.endChoiceTv2) }
        binding.endDownArrow2.setOnClickListener { decreaseHour(binding.endChoiceTv2) }

        binding.startUpArrow3.setOnClickListener { increaseHour(binding.startChoiceTv3) }
        binding.startDownArrow3.setOnClickListener { decreaseHour(binding.startChoiceTv3) }
        binding.endUpArrow3.setOnClickListener { increaseHour(binding.endChoiceTv3) }
        binding.endDownArrow3.setOnClickListener { decreaseHour(binding.endChoiceTv3) }
    }

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
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val isAm = ampmPicker.value == 0
            var hour = hourPicker.value % 12
            if (!isAm) hour += 12
            if (hour == 0) hour = 0

            val minute = minuteValues[minutePicker.value]
            val timeText = String.format("%02d:%s", hour, minute)

            targetTextView.text = timeText

            val parentCard = targetTextView.parent?.parent as? ViewGroup
            if (parentCard != null) {
                lastSelectedCardView?.background = null
                parentCard.setBackgroundResource(R.drawable.friend_time_card_bg_selected)
                lastSelectedCardView = parentCard
            }

            updateNextButtonState()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun increaseHour(targetTextView: TextView) {
        val currentText = targetTextView.text.toString()
        if (currentText.isNotBlank()) {
            val parts = currentText.split(":")
            if (parts.size == 2) {
                var hour = parts[0].toIntOrNull() ?: return
                val minute = parts[1]

                hour = (hour + 1) % 24
                val newTime = String.format("%02d:%s", hour, minute)
                targetTextView.text = newTime
                updateNextButtonState()
            }
        }
    }

    private fun decreaseHour(targetTextView: TextView) {
        val currentText = targetTextView.text.toString()
        if (currentText.isNotBlank()) {
            val parts = currentText.split(":")
            if (parts.size == 2) {
                var hour = parts[0].toIntOrNull() ?: return
                val minute = parts[1]

                hour = if (hour == 0) 23 else hour - 1
                val newTime = String.format("%02d:%s", hour, minute)
                targetTextView.text = newTime
                updateNextButtonState()
            }
        }
    }

    private fun updateNextButtonState() {
        val isTimeSelected = listOf(
            binding.startChoiceTv1, binding.endChoiceTv1,
            binding.startChoiceTv2, binding.endChoiceTv2,
            binding.startChoiceTv3, binding.endChoiceTv3
        ).any { it.text.toString() != "선택" }

        val isTitleFilled = binding.editTextTitle.text.toString().isNotBlank()

        val isEnabled = isTimeSelected && isTitleFilled  //  상세 멘트 제거

        binding.sendBtn.isEnabled = isEnabled
        binding.sendBtn.setBackgroundColor(
            if (isEnabled) android.graphics.Color.parseColor("#0F0F0F")
            else android.graphics.Color.parseColor("#F6F6F6")
        )
        binding.sendBtn.setTextColor(
            if (isEnabled) android.graphics.Color.parseColor("#FFFFFF")
            else android.graphics.Color.parseColor("#0F0F0F")
        )
    }
}