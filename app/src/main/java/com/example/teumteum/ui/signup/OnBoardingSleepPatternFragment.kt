package com.example.teumteum.ui.signup

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingSleepPatternBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class OnBoardingSleepPatternFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingSleepPatternBinding

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOnBoardingSleepPatternBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(75)

        binding.nextBtn.setOnClickListener {
//            startActivity(Intent(requireContext(), MainActivity::class.java))
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingRemindFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.sleepStartContainer.setOnClickListener {
            showCustomTimePicker(binding.startChoiceTv)
        }

        binding.sleepEndContainer.setOnClickListener {
            showCustomTimePicker(binding.endChoiceTv)
        }

        binding.startUpArrow.setOnClickListener{
            increaseHour(binding.startChoiceTv)
        }

        binding.startDownArrow.setOnClickListener {
            decreaseHour(binding.startChoiceTv)
        }

        binding.endUpArrow.setOnClickListener {
            increaseHour(binding.endChoiceTv)
        }

        binding.endDownArrow.setOnClickListener {
            decreaseHour(binding.endChoiceTv)
        }
    }

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

            if (targetTextView == binding.startChoiceTv) {
                selectedStartTime = timeText
            } else if (targetTextView == binding.endChoiceTv) {
                selectedEndTime = timeText
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
            }
        }
    }

    //다음으로 버튼 업데이트
    private fun updateNextButtonState() {
        val allSet = selectedStartTime != null && selectedEndTime != null
        binding.nextBtn.isEnabled = allSet

        binding.nextBtn.setBackgroundColor(
            if (allSet)
                requireContext().getColor(R.color.black)
            else
                Color.parseColor("#F6F6F6")
        )

        binding.nextBtn.setTextColor(
            if (allSet)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.black)
        )
    }

}