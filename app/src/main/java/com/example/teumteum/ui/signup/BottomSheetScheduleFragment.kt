import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.core.view.isVisible
import com.example.teumteum.R
import com.example.teumteum.data.Schedule
import com.example.teumteum.databinding.FragmentBottomSheetScheduleBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetScheduleFragment(
    private val selectedDayIndex: Int,
    private val onScheduleAdded: (Schedule) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetScheduleBinding
    private val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dayText = "매주 ${dayNames[selectedDayIndex]}요일"
        binding.startDateTv.text = dayText
        binding.endDateTv.text = dayText

        setupPickers()

        binding.startTimeTv.setOnClickListener {
            val visible = binding.timePickerStartLl.isShown
            if (visible) applySelectedTime(isStart = true)
            binding.timePickerStartLl.isVisible = !visible
            binding.timePickerEndLl.isVisible = false
        }

        binding.endTimeTv.setOnClickListener {
            val visible = binding.timePickerEndLl.isShown
            if (visible) applySelectedTime(isStart = false)
            binding.timePickerEndLl.isVisible = !visible
            binding.timePickerStartLl.isVisible = false
        }

        binding.registerBtn.setOnClickListener {
            val title = binding.scheduleTitleEt.text.toString().trim()
            val description = binding.descriptionTextEt.text.toString().trim()

            //Todo: 일정 등록 시 입력값 부족할 때 처리

            applySelectedTime(isStart = true)
            applySelectedTime(isStart = false)

            val schedule = Schedule(
                title = title,
                day = dayNames[selectedDayIndex],
                startTime = binding.startTimeTv.text.toString(),
                endTime = binding.endTimeTv.text.toString(),
                description = description
            )
            onScheduleAdded(schedule)
            dismiss()
        }
    }

    private fun setupPickers() {
        listOf(binding.ampmPicker01Np, binding.ampmPicker02Np).forEach {
            it.minValue = 0
            it.maxValue = 1
            it.displayedValues = arrayOf("오전", "오후")
        }

        listOf(binding.hourPicker01Np, binding.hourPicker02Np).forEach {
            it.minValue = 1
            it.maxValue = 12
            it.wrapSelectorWheel = true
        }

        val minuteValues = arrayOf("00", "10", "20", "30", "40", "50")
        listOf(binding.minutePicker01Np, binding.minutePicker02Np).forEach {
            it.minValue = 0
            it.maxValue = minuteValues.size - 1
            it.displayedValues = minuteValues
            it.wrapSelectorWheel = true
        }
    }

    private fun applySelectedTime(isStart: Boolean) {
        val ampmPicker = if (isStart) binding.ampmPicker01Np else binding.ampmPicker02Np
        val hourPicker = if (isStart) binding.hourPicker01Np else binding.hourPicker02Np
        val minutePicker = if (isStart) binding.minutePicker01Np else binding.minutePicker02Np

        val ampm = if (ampmPicker.value == 0) "오전" else "오후"
        val hour = hourPicker.value
        val minute = arrayOf("00", "10", "20", "30", "40", "50")[minutePicker.value]

        val timeText = "$ampm $hour:$minute"

        if (isStart) {
            binding.startTimeTv.text = timeText
            binding.timePickerStartLl.isVisible = false
        } else {
            binding.endTimeTv.text = timeText
            binding.timePickerEndLl.isVisible = false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }

        return dialog
    }

}
