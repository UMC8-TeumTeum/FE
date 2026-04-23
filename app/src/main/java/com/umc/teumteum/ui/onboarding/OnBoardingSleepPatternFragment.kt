package com.umc.teumteum.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.umc.teumteum.databinding.FragmentOnBoardingSleepPatternBinding
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingViewModel
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.utils.enableTapToNext
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
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
        super.onViewCreated(view, savedInstanceState)

        selectedStartTime = viewModel.sleepStartTime.value
        selectedEndTime = viewModel.sleepEndTime.value

        observeViewModel()

        val initialMarginBottom =
            (binding.nextBtn.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.nextBtn) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + bottomInset
            }
            insets
        }

        viewModel.sleepStartTime.value?.let {
            binding.startChoiceTv.text = it.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        viewModel.sleepEndTime.value?.let {
            binding.endChoiceTv.text = it.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        updateNextButtonState()

        binding.nextBtn.setOnClickListener {
            val start = viewModel.sleepStartTime.value
            val end = viewModel.sleepEndTime.value
            if (start != null && end != null) {
                viewModel.postSleepPattern(SleepPatternRequest(start.toString(), end.toString()))
            } else {
                navigateToNext()
            }
        }

        binding.sleepStartContainer.setOnClickListener {
            showCustomTimePicker { time ->
                viewModel.setSleepStartTime(time)
                binding.startChoiceTv.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                updateNextButtonState()
            }
        }

        binding.sleepEndContainer.setOnClickListener {
            showCustomTimePicker { time ->
                viewModel.setSleepEndTime(time)
                binding.endChoiceTv.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                updateNextButtonState()
            }
        }

        binding.startUpArrow.setOnClickListener {
            changeHour(binding.startChoiceTv, true, true)
        }

        binding.startDownArrow.setOnClickListener {
            changeHour(binding.startChoiceTv, false, true)
        }

        binding.endUpArrow.setOnClickListener {
            changeHour(binding.endChoiceTv, true, false)
        }

        binding.endDownArrow.setOnClickListener {
            changeHour(binding.endChoiceTv, false, false)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> binding.nextBtn.isEnabled = false

                is OnBoardingUiState.Success -> {
                    binding.nextBtn.isEnabled = true
                    navigateToNext()
                }

                is OnBoardingUiState.Error -> {
                    binding.nextBtn.isEnabled = true
                }

                else -> Unit
            }
        }
    }

    private fun navigateToNext() {
        // SignUpActivity의 메서드를 통해 다음 단계로 이동
        (activity as? SignUpActivity)?.proceedToNextOnboardingStep(this)
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
        ampmPicker.wrapSelectorWheel = true

        hourPicker.minValue = 1
        hourPicker.maxValue = 12
        hourPicker.wrapSelectorWheel = true

        minutePicker.minValue = 0
        minutePicker.maxValue = minuteValues.size - 1
        minutePicker.displayedValues = minuteValues
        minutePicker.wrapSelectorWheel = true

        ampmPicker.enableTapToNext(wrap = true)
        hourPicker.enableTapToNext(wrap = true)
        minutePicker.enableTapToNext(wrap = true)

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

    private fun changeHour(targetTextView: TextView, increase: Boolean, isStart: Boolean) {
        val currentText = targetTextView.text.toString()
        if (currentText.isNotBlank()) {
            val currentTime = LocalTime.parse(currentText)
            val newTime = if (increase) currentTime.plusHours(1) else currentTime.minusHours(1)
            targetTextView.text = newTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            if (isStart) {
                viewModel.setSleepStartTime(newTime)
            } else {
                viewModel.setSleepEndTime(newTime)
            }

            updateNextButtonState()
        }
    }

    private fun updateNextButtonState() {
        val start = viewModel.sleepStartTime.value
        val end = viewModel.sleepEndTime.value

        val shouldEnable = (start != null && end != null) || (start == null && end == null)

        binding.nextBtn.isEnabled = shouldEnable
        binding.nextBtn.setBackgroundColor(
            if (shouldEnable)
                requireContext().getColor(R.color.text_primary)
            else
                requireContext().getColor(R.color.teumteum_bg)
        )
        binding.nextBtn.setTextColor(
            if (shouldEnable)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.text_primary)
        )
    }
}