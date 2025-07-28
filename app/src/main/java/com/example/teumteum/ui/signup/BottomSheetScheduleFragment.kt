import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.teumteum.R
import com.example.teumteum.data.Schedule
import com.example.teumteum.databinding.FragmentBottomSheetScheduleBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class BottomSheetScheduleFragment(
    private val selectedDayIndex: Int,
    private val existingSchedules: List<Schedule>,
    private val onScheduleAdded: (Schedule) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetScheduleBinding
    private val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null

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
            val visible = binding.timePickerStartContainer.isVisible
            if (visible) applySelectedTime(isStart = true)
            binding.timePickerStartContainer.isVisible = !visible
            binding.timePickerEndContainer.isVisible = false
        }

        binding.endTimeTv.setOnClickListener {
            val visible = binding.timePickerEndContainer.isVisible
            if (visible) applySelectedTime(isStart = false)
            binding.timePickerEndContainer.isVisible = !visible
            binding.timePickerStartContainer.isVisible = false
        }

        binding.registerBtn.setOnClickListener {
            val title = binding.scheduleTitleEt.text.toString().trim()
            val description = binding.descriptionTextEt.text.toString().trim()

            // 시간 검증
            if (startTime == null || endTime == null) {
                Toast.makeText(requireContext(), "시작/종료 시간을 모두 선택하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (endTime!!.isBefore(startTime)) {
                Toast.makeText(requireContext(), "일정은 자정을 넘길 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 시작과 종료가 같을 경우 00:00만 허용
            if (startTime == endTime) {
                if (!(startTime == LocalTime.MIDNIGHT && endTime == LocalTime.MIDNIGHT)) {
                    Toast.makeText(requireContext(), "시작과 종료 시간이 같을 수 없습니다 (00:00만 가능)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (isTimeOverlap(startTime!!, endTime!!)) {
                Toast.makeText(requireContext(), "같은 요일의 다른 일정과 시간이 겹칩니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val schedule = Schedule(
                title = title,
                day = dayNames[selectedDayIndex],
                startTime = startTime!!,
                endTime = endTime!!,
                description = description
            )
            onScheduleAdded(schedule)
            dismiss()
        }
    }

    private fun setupPickers() {
        val context = binding.root.context

        listOf(binding.ampmPicker01Np, binding.ampmPicker02Np).forEach {
            it.minValue = 0
            it.maxValue = 1
            it.displayedValues = arrayOf("오전", "오후")
            applyTextStyleToNumberPicker(it, context)
        }

        listOf(binding.hourPicker01Np, binding.hourPicker02Np).forEach {
            it.minValue = 1
            it.maxValue = 12
            it.wrapSelectorWheel = true
            applyTextStyleToNumberPicker(it, context)
        }

        val minuteValues = arrayOf("00", "10", "20", "30", "40", "50")
        listOf(binding.minutePicker01Np, binding.minutePicker02Np).forEach {
            it.minValue = 0
            it.maxValue = minuteValues.size - 1
            it.displayedValues = minuteValues
            it.wrapSelectorWheel = true
            applyTextStyleToNumberPicker(it, context)
        }
    }

    private fun applySelectedTime(isStart: Boolean) {
        val ampmPicker = if (isStart) binding.ampmPicker01Np else binding.ampmPicker02Np
        val hourPicker = if (isStart) binding.hourPicker01Np else binding.hourPicker02Np
        val minutePicker = if (isStart) binding.minutePicker01Np else binding.minutePicker02Np

        val minuteValues = arrayOf("00", "10", "20", "30", "40", "50")
        val isAm = ampmPicker.value == 0

        var hour = hourPicker.value % 12
        if (!isAm) hour += 12
        if (hour == 0) hour = 0

        val minute = minuteValues[minutePicker.value].toInt()
        val selectedTime = LocalTime.of(hour, minute)

        val displayText = selectedTime.format(DateTimeFormatter.ofPattern("a h:mm",Locale("ko", "KR")))

        if (isStart) {
            startTime = selectedTime
            binding.startTimeTv.text = displayText
            binding.timePickerStartContainer.isVisible = false
        } else {
            endTime = selectedTime
            binding.endTimeTv.text = displayText
            binding.timePickerEndContainer.isVisible = false
        }
    }

    private fun applyTextStyleToNumberPicker(picker: NumberPicker, context: Context) {
        try {
            val count = picker.childCount
            for (i in 0 until count) {
                val child = picker.getChildAt(i)
                if (child is EditText) {
                    child.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    child.textSize = 15f
                    child.typeface = ResourcesCompat.getFont(context, R.font.noto_sans_kr_regular)
                    child.includeFontPadding = false

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isTimeOverlap(newStart: LocalTime, newEnd: LocalTime): Boolean {
        return existingSchedules.any { schedule ->
            val existingStart = schedule.startTime
            val existingEnd = schedule.endTime
            (newStart < existingEnd && newEnd > existingStart)
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
