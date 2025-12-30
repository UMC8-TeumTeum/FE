package com.example.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.model.SleepPatternRequest
import com.example.teumteum.databinding.FragmentMySleepPatternSettingBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.myhome.viewModel.SettingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MySleepPatternSettingFragment : Fragment() {

    private lateinit var binding: FragmentMySleepPatternSettingBinding

    private val viewModel: SettingViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMySleepPatternSettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.sleepStartContainer.setOnClickListener {
            showCustomTimePicker { time ->
                binding.startChoiceTv.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                tryUpdateSleepPattern()
            }
        }

        binding.sleepEndContainer.setOnClickListener {
            showCustomTimePicker { time ->
                binding.endChoiceTv.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                tryUpdateSleepPattern()
            }
        }

        binding.startUpArrow.setOnClickListener {
            changeHour(binding.startChoiceTv, true, true)
            tryUpdateSleepPattern()
        }

        binding.startDownArrow.setOnClickListener {
            changeHour(binding.startChoiceTv, false, true)
            tryUpdateSleepPattern()
        }

        binding.endUpArrow.setOnClickListener {
            changeHour(binding.endChoiceTv, true, false)
            tryUpdateSleepPattern()
        }

        binding.endDownArrow.setOnClickListener {
            changeHour(binding.endChoiceTv, false, false)
            tryUpdateSleepPattern()
        }
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

    private fun changeHour(targetTextView: TextView, increase: Boolean, isStart: Boolean) {
        val currentText = targetTextView.text.toString()
        if (currentText.isNotBlank()) {
            val currentTime = LocalTime.parse(currentText)
            val newTime = if (increase) currentTime.plusHours(1) else currentTime.minusHours(1)
            targetTextView.text = newTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    private fun tryUpdateSleepPattern() {
        val startText = binding.startChoiceTv.text.toString().trim()
        val endText = binding.endChoiceTv.text.toString().trim()

        if (startText.isBlank() || endText.isBlank()) return

        val start = runCatching { LocalTime.parse(startText, timeFormatter) }.getOrNull() ?: return
        val end = runCatching { LocalTime.parse(endText, timeFormatter) }.getOrNull() ?: return

        viewModel.updateSleepPattern(
            SleepPatternRequest(start.toString(), end.toString())
        )
    }
}