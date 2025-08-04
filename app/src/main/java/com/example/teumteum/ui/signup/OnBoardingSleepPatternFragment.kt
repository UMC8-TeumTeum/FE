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
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.OnBoardingService
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.databinding.FragmentOnBoardingSleepPatternBinding
import com.example.teumteum.ui.signup.view.SleepPatternView
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.ui.signup.viewModel.OnBoardingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class OnBoardingSleepPatternFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingSleepPatternBinding
    private val viewModel: OnBoardingViewModel by activityViewModels()

    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingSleepPatternBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? SignUpActivity)?.setProgressBar(60)

        observeViewModel()

        binding.nextBtn.setOnClickListener {
            if (selectedStartTime != null && selectedEndTime != null) {
                viewModel.postSleepPattern(getSleepPatternRequest())
            } else {
                navigateToNext()
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

        binding.startUpArrow.setOnClickListener {
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

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> {
                    binding.nextBtn.isEnabled = false
                }

                is OnBoardingUiState.Success -> {
                    binding.nextBtn.isEnabled = true
                    navigateToNext()
                }

                is OnBoardingUiState.Error -> {
                    binding.nextBtn.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                    // 온보딩 단계를 넘겼을 때도 다음 화면으로 이동
                    if (state.code.contains("ONBOARDING4001")) {
                        navigateToNext()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun getSleepPatternRequest(): SleepPatternRequest {
        return SleepPatternRequest(
            sleepTime = selectedStartTime.toString(),
            wakeTime = selectedEndTime.toString()
        )
    }

    private fun navigateToNext() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OnBoardingScheduleFragment().apply {
                arguments = Bundle().apply {
                    selectedStartTime?.let { putString("sleepStart", it.toString()) }
                    selectedEndTime?.let { putString("sleepEnd", it.toString()) }
                }
            })
            .addToBackStack(null)
            .commit()

        viewModel.resetState()
    }

    private fun showCustomTimePicker(onTimeSelected: (LocalTime) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val ampmPicker = dialogView.findViewById<NumberPicker>(R.id.ampmPicker01Np)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker01Np)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker01Np)
        val minuteValues = arrayOf("00", "10", "20", "30", "40", "50")

        ampmPicker.minValue = 0
        ampmPicker.maxValue = 1
        ampmPicker.displayedValues = arrayOf("AM", "PM")

        hourPicker.minValue = 1
        hourPicker.maxValue = 12

        minutePicker.minValue = 0
        minutePicker.maxValue = minuteValues.size - 1
        minutePicker.displayedValues = minuteValues

        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnShowListener {
                findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.setBackgroundResource(R.drawable.calendar_background)
            }
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val hour = hourPicker.value % 12 + if (ampmPicker.value == 1) 12 else 0
            val minute = minuteValues[minutePicker.value].toInt()
            val selectedTime = LocalTime.of(hour, minute)
            onTimeSelected(selectedTime)
            dialog.dismiss()
        }

        dialog.show()
    }

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
}