package com.example.teumteum.ui.wish

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.teumteum.R
import com.example.teumteum.data.remote.activity.model.AssignWishRequest
import com.example.teumteum.databinding.DialogConfirmRegisterBinding
import com.example.teumteum.databinding.FragmentWishSetting03Binding
import com.example.teumteum.ui.main.HomeFragment
import com.example.teumteum.ui.wish.viewModel.WishViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class WishSetting03Fragment : Fragment() {

    private lateinit var binding: FragmentWishSetting03Binding

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var selectedDate: String? = null

    private var wishId: Long? = null
    private val viewModel: WishViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishSetting03Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val title = arguments?.getString("title")
        setTitle(title.toString())

        val time = arguments?.getString("time")
        setTime(time.toString())

        wishId = arguments?.getLong("wish_id")

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        // 오늘 날짜로 폴백
        selectedDate = arguments?.getString("selected_date")
            ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)

        val selectedTime = arguments?.getString("selected_time")

        binding.wishTimeSettingTv.text = selectedTime

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
                viewModel.assignWish(wishId!!, assignWishRequest(false))
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

    private fun setTitle(title: String){
        binding.wishTitleTv.text = title
    }

    private fun setTime(time: String){
        binding.wishTimeTv.text = time
    }

    private fun combineDateTime(date: String, timeHHmm: String): String {
        return "${date}T$timeHHmm"
    }

    private fun assignWishRequest(isForce: Boolean): AssignWishRequest {
        val startHHmm = binding.startChoiceTv.text.toString()
        val endHHmm = binding.endChoiceTv.text.toString()

        val date = selectedDate ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val startIso = combineDateTime(date, startHHmm)
        val endIso = combineDateTime(date, endHHmm)

        return AssignWishRequest(
            startTime = startIso,
            endTime = endIso,
            isForce = isForce
        )
    }

    private fun showRegisterDialog() {
        val dialogBinding = DialogConfirmRegisterBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.assignConfirmTv.setOnClickListener {
            if (wishId != null) {
                viewModel.assignWish(wishId!!, assignWishRequest(true))
            }
            dialog.dismiss()
        }

        dialogBinding.assignCancelTv.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            dialog.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.y = (resources.displayMetrics.heightPixels * 0.37).toInt()
                layoutParams.dimAmount = 0.5f
                window.attributes = layoutParams

                window.setDimAmount(0.5f)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }

        dialog.show()
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
                    Log.d("ASSIGN_FRAMENT", "빈틈채우기에 성공하였습니다.")
                    parentFragmentManager.setFragmentResult("assign", Bundle())

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
                    when (err.code) {
                        "HOME4092" -> showRegisterDialog()
                        "CONFLICT4094", "CONFLICT4092", "HOME4001" ->
                            Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                        else -> err.message.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}