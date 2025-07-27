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
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class OnBoardingSleepPatternFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingSleepPatternBinding

    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null

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
        (activity as? SignUpActivity)?.setProgressBar(60)

        binding.nextBtn.setOnClickListener {
//            startActivity(Intent(requireContext(), MainActivity::class.java))
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingScheduleFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.sleepStartContainer.setOnClickListener {
            showCustomTimePicker { time ->
                selectedStartTime = time
                binding.startChoiceTv.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                updateNextButtonState()
            }
        }

        binding.sleepEndContainer.setOnClickListener {
            showCustomTimePicker { time ->
                selectedEndTime = time
                binding.endChoiceTv.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                updateNextButtonState()
            }
        }

        binding.startUpArrow.setOnClickListener{
            changeHour(binding.startChoiceTv, true)
        }

        binding.startDownArrow.setOnClickListener {
            changeHour(binding.startChoiceTv, false)
        }

        binding.endUpArrow.setOnClickListener {
            changeHour(binding.endChoiceTv, true)
        }

        binding.endDownArrow.setOnClickListener {
            changeHour(binding.endChoiceTv, false)
        }
    }

    private fun showCustomTimePicker(onTimeSelected: (LocalTime) -> Unit) {
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

        // 배경 적용
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
            if (hour == 0) hour = 0

            val minute = minuteValues[minutePicker.value].toInt()

            val selectedTime = LocalTime.of(hour, minute)
            onTimeSelected(selectedTime)

            dialog.dismiss()
        }

        dialog.show()
    }

    //화살표로 시간 증가/감소
    private fun changeHour(targetTextView: TextView, increase: Boolean) {
        val currentText = targetTextView.text.toString()
        if (currentText.isNotBlank()) {
            val currentTime = LocalTime.parse(currentText)
            val newTime = if (increase) currentTime.plusHours(1) else currentTime.minusHours(1)
            targetTextView.text = newTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            if (targetTextView == binding.startChoiceTv) {
                selectedStartTime = newTime
            } else if (targetTextView == binding.endChoiceTv) {
                selectedEndTime = newTime
            }
            updateNextButtonState()
        }
    }

    //다음으로 버튼 업데이트
    private fun updateNextButtonState() {
        val bothSelected = selectedStartTime != null && selectedEndTime != null

        if (!bothSelected) {
            binding.nextBtn.isEnabled = false
            binding.nextBtn.setBackgroundColor(Color.parseColor("#F6F6F6"))
            binding.nextBtn.setTextColor(requireContext().getColor(R.color.black))
            return
        }

        val isValid = if (bothSelected) validateSleepTime() else true

        binding.nextBtn.isEnabled = isValid

        binding.nextBtn.setBackgroundColor(
            if (isValid)
                requireContext().getColor(R.color.black)
            else
                Color.parseColor("#F6F6F6")
        )

        binding.nextBtn.setTextColor(
            if (isValid)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.black)
        )
    }

    //수면 시간 검증
    private fun validateSleepTime(): Boolean {
        val start = selectedStartTime!!
        val end = selectedEndTime!!

        if (start == end) return false

        return true
    }

}