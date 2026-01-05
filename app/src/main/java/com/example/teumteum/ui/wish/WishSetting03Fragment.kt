package com.example.teumteum.ui.wish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.teumteum.R
import com.example.teumteum.data.remote.activity.model.AssignWishRequest
import com.example.teumteum.databinding.FragmentWishSetting03Binding
import com.example.teumteum.ui.main.HomeFragment
import com.example.teumteum.ui.wish.viewModel.WishViewModel
import com.example.teumteum.utils.applyPickerValue
import com.example.teumteum.utils.parse24hTimeToPickerValue
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class WishSetting03Fragment : Fragment() {

    private var _binding: FragmentWishSetting03Binding? = null
    private val binding get() = _binding!!

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null

    private var wishId: Long? = null
    private val viewModel: WishViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishSetting03Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val title = arguments?.getString("title")
        binding.wishTitleTv.text = title

        val time = arguments?.getString("time")
        binding.wishTimeTv.text = time

        wishId = arguments?.getLong("wish_id")

        val selectedTimeText = arguments?.getString("selected_time_text")

        binding.wishTimeSettingTv.text = selectedTimeText?.takeIf { it.isNotBlank() }
            ?: "직접 입력하기"

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        binding.wishStartContainer.setOnClickListener {
            showCustomTimePicker(binding.startChoiceTv)
        }

        binding.wishEndContainer.setOnClickListener {
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

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.registerBtn.setOnClickListener {
            if (wishId != null) {
                viewModel.assignWish(wishId!!, assignWishRequest())
            }
        }
        setupObservers()
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

        parse24hTimeToPickerValue(
            timeText = targetTextView.text.toString(),
            minuteOptions = minuteValues
        )?.let { value ->
            ampmPicker.applyPickerValue(
                hourPicker = hourPicker,
                minutePicker = minutePicker,
                value = value
            )
        }

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
            if (hour == 0) hour = 0 // 12AM → 0시로

            val minute = minuteValues[minutePicker.value]
            val timeText = String.format("%02d:%s", hour, minute)

            targetTextView.text = timeText

            if (targetTextView == binding.startChoiceTv) {
                selectedStartTime = timeText
            } else if (targetTextView == binding.endChoiceTv) {
                selectedEndTime = timeText
            }

            enableNextButton()
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

    private fun assignWishRequest(): AssignWishRequest {
        val startHHmm = binding.startChoiceTv.text.toString()
        val endHHmm = binding.endChoiceTv.text.toString()

        val startDate = getStartDate()
        val startIso = "${startDate}T$startHHmm"

        val (endDate, endTime) = getEndDate(startDate, endHHmm)
        val endIso = "${endDate}T$endTime"

        return AssignWishRequest(
            startTime = startIso,
            endTime = endIso,
        )
    }

    private fun enableNextButton() {
        val allSet = selectedStartTime != null && selectedEndTime != null
        binding.registerBtn.isEnabled = allSet

        binding.registerBtn.setBackgroundColor(
            if (allSet)
                requireContext().getColor(R.color.text_primary)
            else
                requireContext().getColor(R.color.teumteum_bg)
        )

        binding.registerBtn.setTextColor(
            if (allSet)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.text_primary)
        )
    }

    private fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assignSuccess.collect {
                    Log.d("ASSIGN_FRAGMENT", "빈틈채우기에 성공하였습니다.")

                    val dateStr = getStartDate() // "yyyy-MM-dd"
                    val safeDateStr = runCatching { LocalDate.parse(dateStr).toString() }
                        .getOrElse { LocalDate.now().toString() }

                    val result = Bundle().apply {
                        putString("date", safeDateStr)
                    }
                    parentFragmentManager.setFragmentResult("assign_home", result)

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.assignError.collect { err ->
                    Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getStartDate(): String {
        return arguments?.getString("selected_date")
            ?: arguments?.getString("startDate")
            ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    private fun getEndDate(startDate: String, endHHmm: String): Pair<String, String> {
        return if (endHHmm == "24:00") {
            LocalDate.parse(startDate).plusDays(1).toString() to "00:00"
        } else {
            startDate to endHHmm
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}