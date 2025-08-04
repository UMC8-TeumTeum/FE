package com.example.teumteum.ui.signup

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.OnBoardingService
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.databinding.FragmentOnBoardingSleepPatternBinding
import com.example.teumteum.ui.signup.view.SleepPatternView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingSleepPatternFragment : Fragment(), SleepPatternView {

    private lateinit var binding: FragmentOnBoardingSleepPatternBinding

    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null

    @Inject
    lateinit var onBoardingService: OnBoardingService

    override fun onSleepPatternSuccess(code: String) {
        val msg = "수면패턴 입력 완료 (code: $code)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("SLEEP_PATTERN_FRAGMENT", msg)

        val fragment = OnBoardingScheduleFragment().apply {
            arguments = Bundle().apply {
                selectedStartTime?.let { putString("sleepStart", it.toString()) }
                selectedEndTime?.let { putString("sleepEnd", it.toString()) }
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onSleepPatternFailure(code: String, message: String?) {
        val msg = "수면 패턴 입력 실패 (code: $code, message: ${message ?: "없음"})"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.e("SLEEP_PATTERN_FRAGMENT", msg)

        //온보딩 단계가 아닐 경우 - 이후 테스트를 위해 화면 이동하도록 구현
        if (message?.contains("ONBOARDING4001") == true) {
            Toast.makeText(requireContext(), "온보딩 단계가 아닙니다.", Toast.LENGTH_SHORT).show()

            val fragment = OnBoardingScheduleFragment().apply {
                arguments = Bundle().apply {
                    selectedStartTime?.let { putString("sleepStart", it.toString()) }
                    selectedEndTime?.let { putString("sleepEnd", it.toString()) }
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

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
            //입력 값이 있을 때 만 호출
            if(selectedStartTime != null && selectedEndTime != null){
                val request = getSleepPatternRequest()
                onBoardingService.setSleepPatternView(this)
                onBoardingService.postSleepPattern(request)
            }else{
                val fragment = OnBoardingScheduleFragment().apply {
                    arguments = Bundle().apply {
                        selectedStartTime?.let { putString("sleepStart", it.toString()) }
                        selectedEndTime?.let { putString("sleepEnd", it.toString()) }
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
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

        binding.nextBtn.isEnabled = bothSelected

        binding.nextBtn.setBackgroundColor(
            if (bothSelected)
                requireContext().getColor(R.color.black)
            else
                Color.parseColor("#F6F6F6")
        )

        binding.nextBtn.setTextColor(
            if (bothSelected)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.black)
        )
    }

    private fun getSleepPatternRequest(): SleepPatternRequest{
        return SleepPatternRequest(
            sleepTime = selectedStartTime.toString(),
            wakeTime = selectedEndTime.toString()
        )
    }

}