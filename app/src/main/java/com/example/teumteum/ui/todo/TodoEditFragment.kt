package com.example.teumteum.ui.todo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentTodoEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.teumteum.data.entities.TodoHomeItem
import com.example.teumteum.databinding.DialogConfirmTodoDeleteBinding
import com.example.teumteum.databinding.DialogConfirmTodoEditBinding
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodoEditFragment : BottomSheetDialogFragment(), IDateClickListener {

    private lateinit var binding: FragmentTodoEditBinding

    private var currentTargetTextView: TextView? = null

    private var todoId: Int = -1

    private val selectedItems = mutableSetOf<String>()
    private val alarmOptions = listOf("30분 전", "10분 전", "5분 전", "3분 전", "1분 전")
    private var popupWindow: PopupWindow? = null

    private var isCalendarVisible = false
    private var calendarFragmentStart: MonthlyCalendarFragment? = null
    private var calendarFragmentEnd: MonthlyCalendarFragment? = null
    private var isStartDateSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todoId = arguments?.getInt("todo_id") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDummy = arguments?.getBoolean("is_dummy") ?: false
        if (isDummy) {
            val title = arguments?.getString("title") ?: ""
            val start = arguments?.getString("start_time") ?: ""
            val end = arguments?.getString("end_time") ?: ""
            val isPublic = arguments?.getBoolean("is_public") ?: false

            binding.todoTitleEt.setText(title)
            binding.startTimeTv.text = start
            binding.endTimeTv.text = end
            binding.categoryToggle03Iv.isChecked = isPublic
        }

        val isAlarmOn = arguments?.let {
            if (it.containsKey("is_alarm_on")) {
                it.getBoolean("is_alarm_on")
            } else {
                null
            }
        }

        // isAlarmOn 값에 따른 알림 바텀시트 변경
        when (isAlarmOn) {
            null -> {
                binding.alarmItem01Ll.visibility = View.GONE
                binding.alarmItem02Ll.visibility = View.GONE
                selectedItems.clear()
            }
            true -> {
                binding.alarmItem01Ll.visibility = View.VISIBLE
                binding.alarmItem02Ll.visibility = View.VISIBLE
                selectedItems.add("30분 전")
                selectedItems.add("10분 전")
            }
            false -> {
                binding.alarmItem01Ll.visibility = View.VISIBLE
                binding.alarmItem02Ll.visibility = View.VISIBLE
                selectedItems.clear()
            }
        }

        setupPickers()

        binding.startTimeTv.setOnClickListener {
            val isVisibleNow = binding.timePickerStartContainer.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = true)
            }
            binding.timePickerStartContainer.isVisible = !isVisibleNow
            binding.timePickerEndContainer.isVisible = false
            currentTargetTextView = binding.startTimeTv.takeIf { !isVisibleNow }
        }

        binding.endTimeTv.setOnClickListener {
            val isVisibleNow = binding.timePickerEndContainer.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = false)
            }
            binding.timePickerEndContainer.isVisible = !isVisibleNow
            binding.timePickerStartContainer.isVisible = false
            currentTargetTextView = binding.endTimeTv.takeIf { !isVisibleNow }
        }

        listOf(binding.ampmPicker01Np, binding.hourPicker01Np, binding.minutePicker01Np).forEach {
            it.setOnValueChangedListener { _, _, _ -> }
        }
        listOf(binding.ampmPicker02Np, binding.hourPicker02Np, binding.minutePicker02Np).forEach {
            it.setOnValueChangedListener { _, _, _ -> }
        }

        binding.btnPlus.setOnClickListener {
            showAlarmPopupWindow(it)
        }

        binding.btnTodoSave.setOnClickListener {
            val titleText = binding.todoTitleEt.text.toString().trim()

            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 더미 처리
            Toast.makeText(requireContext(), "수정되었습니다. (더미)", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnTodoDelete.setOnClickListener {
            showTodoDummyDeleteDialog()
        }

        binding.startDateTv.setOnClickListener {
            isStartDateSelected = true
            toggleCalendarVisibility()
        }

        binding.endDateTv.setOnClickListener {
            isStartDateSelected = false
            toggleCalendarVisibility()
        }

        if (todoId == 3) {
            val deactiveColor = ContextCompat.getColor(requireContext(), R.color.teumteum_deactive)

            binding.todoTitleEt.setTextColor(deactiveColor)
            binding.timerIconIv.setColorFilter(deactiveColor)
            binding.startDateTv.setTextColor(deactiveColor)
            binding.startTimeTv.setTextColor(deactiveColor)
            binding.endDateTv.setTextColor(deactiveColor)
            binding.endTimeTv.setTextColor(deactiveColor)
            binding.alarmIconIv.setColorFilter(deactiveColor)
            binding.alarmSet01Tv.setTextColor(deactiveColor)
            binding.alarmSet02Tv.setTextColor(deactiveColor)
            binding.addAlarmTv.setTextColor(deactiveColor)
            binding.publicIconIv.setColorFilter(deactiveColor)
            binding.publicSettingTv.setTextColor(deactiveColor)
            binding.includeIconIv.setColorFilter(deactiveColor)
            binding.includeReportTv.setTextColor(deactiveColor)
            binding.detailTextIv.setColorFilter(deactiveColor)
            binding.detailTextEt.setTextColor(deactiveColor)
            binding.detailTextEt.setHintTextColor(deactiveColor)

            binding.todoTitleEt.isEnabled = false
            binding.startDateTv.isEnabled = false
            binding.startTimeTv.isEnabled = false
            binding.endDateTv.isEnabled = false
            binding.endTimeTv.isEnabled = false
            binding.alarmSet01Tv.isEnabled = false
            binding.alarmSet02Tv.isEnabled = false
            binding.addAlarmTv.isEnabled = false
            binding.btnPlus.isEnabled = false
            binding.detailTextEt.isEnabled = false

            binding.categoryToggle01Iv.isEnabled = false
            binding.categoryToggle02Iv.isEnabled = false
            binding.categoryToggle03Iv.isEnabled = false
            binding.categoryToggle04Iv.isEnabled = false

            binding.btnTodoDelete.isEnabled = false
            binding.btnTodoSave.isEnabled = false

            Toast.makeText(requireContext(), "이 일정은 편집할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val screenHeight = resources.displayMetrics.heightPixels
                val desiredHeight = (screenHeight * 0.84).toInt()

                it.layoutParams.height = desiredHeight
                it.requestLayout()

                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = desiredHeight
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.isDraggable = false // 확장 불가능
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                showTodoCancelEditDialog()
                true
            } else {
                false
            }
        }

        return dialog
    }

    private fun showAlarmPopupWindow(anchor: View) {
        if (popupWindow?.isShowing == true) {
            popupWindow?.dismiss()
            return
        }

        val popupView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_dropdown)
            setPadding(4, 4, 4, 4)
            elevation = 16f
        }

        // 항목 추가
        alarmOptions.forEachIndexed { index, label ->
            val itemView = layoutInflater.inflate(R.layout.alarm_dropdown, popupView, false)
            val labelText = itemView.findViewById<TextView>(R.id.alarm_label_tv)
            val checkIcon = itemView.findViewById<ImageView>(R.id.check_icon)

            labelText.text = label
            checkIcon.visibility = if (selectedItems.contains(label)) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (selectedItems.contains(label)) {
                    selectedItems.remove(label)
                    checkIcon.visibility = View.GONE
                    removeAlarmItem(label)
                } else {
                    selectedItems.add(label)
                    checkIcon.visibility = View.VISIBLE
                    addAlarmItem(label)
                }
            }

            popupView.addView(itemView)

            if (index < alarmOptions.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    )
                    setBackgroundColor(resources.getColor(R.color.teumteum_line, null))
                }
                popupView.addView(divider)
            }
        }

        val popupWidth = resources.displayMetrics.widthPixels / 2

        // 팝업 설정
        popupWindow = PopupWindow(
            popupView,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = 16f
            setBackgroundDrawable(null)

            showAsDropDown(anchor, -popupWidth + anchor.width, 16)
        }
    }

    private fun addAlarmItem(label: String) {
        when (label) {
            "30분 전" -> {
                binding.alarmItem01Ll.visibility = View.VISIBLE
            }
            "10분 전" -> {
                binding.alarmItem02Ll.visibility = View.VISIBLE
            }
            else -> {
                val layout = layoutInflater.inflate(R.layout.item_alarm, binding.alarmLayoutContainer, false)
                val labelText = layout.findViewById<TextView>(R.id.alarm_set_tv)
                labelText.text = label
                layout.tag = label
                binding.alarmLayoutContainer.addView(layout)
            }
        }
    }

    private fun removeAlarmItem(label: String) {
        when (label) {
            "30분 전" -> {
                binding.alarmItem01Ll.visibility = View.GONE
            }
            "10분 전" -> {
                binding.alarmItem02Ll.visibility = View.GONE
            }
            else -> {
                for (i in 0 until binding.alarmLayoutContainer.childCount) {
                    val child = binding.alarmLayoutContainer.getChildAt(i)
                    if (child.tag == label) {
                        binding.alarmLayoutContainer.removeView(child)
                        break
                    }
                }
            }
        }
    }

    companion object {
        fun newInstanceWithTodoDummy(item: TodoHomeItem): TodoEditFragment {
            return TodoEditFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("is_dummy", true)
                    putInt("todo_id", item.id)
                    putString("title", item.title)
                    putString("start_time", item.startTime)
                    putString("end_time", item.endTime)
                    putBoolean("is_public", item.isPublic)
                    item.isAlarmOn?.let { putBoolean("is_alarm_on", it) }
                }
            }
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

    private fun setupPickers() {
        binding.ampmPicker01Np.apply {
            minValue = 0
            maxValue = 1
            displayedValues = arrayOf("오전", "오후")
            post { applyTextStyleToNumberPicker(this, context) }
        }
        binding.hourPicker01Np.apply {
            minValue = 1
            maxValue = 12
            wrapSelectorWheel = true
            post { applyTextStyleToNumberPicker(this, context) }
        }
        binding.minutePicker01Np.apply {
            minValue = 0
            maxValue = 5
            displayedValues = arrayOf("00", "10", "20", "30", "40", "50")
            wrapSelectorWheel = true
            post { applyTextStyleToNumberPicker(this, context) }
        }

        binding.ampmPicker02Np.apply {
            minValue = 0
            maxValue = 1
            displayedValues = arrayOf("오전", "오후")
            post { applyTextStyleToNumberPicker(this, context) }
        }
        binding.hourPicker02Np.apply {
            minValue = 1
            maxValue = 12
            wrapSelectorWheel = true
            post { applyTextStyleToNumberPicker(this, context) }
        }
        binding.minutePicker02Np.apply {
            minValue = 0
            maxValue = 5
            displayedValues = arrayOf("00", "10", "20", "30", "40", "50")
            wrapSelectorWheel = true
            post { applyTextStyleToNumberPicker(this, context) }
        }
    }

    private fun applySelectedTime(isStart: Boolean) {
        val ampmPicker = if (isStart) binding.ampmPicker01Np else binding.ampmPicker02Np
        val hourPicker = if (isStart) binding.hourPicker01Np else binding.hourPicker02Np
        val minutePicker = if (isStart) binding.minutePicker01Np else binding.minutePicker02Np

        val ampm = ampmPicker.value
        val hour = hourPicker.value
        val minute = arrayOf("00", "10", "20", "30", "40", "50")[minutePicker.value]
        val timeText = "${if (ampm == 0) "오전" else "오후"} $hour:$minute"

        if (isStart) {
            binding.startTimeTv.text = timeText
            binding.timePickerStartContainer.isVisible = false
        } else {
            if (!validateEndTime()) {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("시간 오류")
                    .setMessage("종료 시간을 시작 시간 이후로 설정해주세요.")
                    .setPositiveButton("확인") { _, _ ->
                        binding.timePickerEndContainer.isVisible = true // 다시 종료 시간 피커 열기
                    }
                    .setCancelable(false)
                    .show()
                return
            }
            binding.endTimeTv.text = timeText
            binding.timePickerEndContainer.isVisible = false
        }
    }

    private fun validateEndTime(): Boolean {
        // 시작 시간
        val startAmpm = binding.ampmPicker01Np.value // 0: 오전, 1: 오후
        val startHour = binding.hourPicker01Np.value
        val startMinute = binding.minutePicker01Np.value * 10

        // 종료 시간
        val endAmpm = binding.ampmPicker02Np.value
        val endHour = binding.hourPicker02Np.value
        val endMinute = binding.minutePicker02Np.value * 10

        // 24시간제로 변환
        val startTotalMinutes = ((if (startAmpm == 1 && startHour != 12) startHour + 12 else if (startAmpm == 0 && startHour == 12) 0 else startHour) * 60) + startMinute
        val endTotalMinutes = ((if (endAmpm == 1 && endHour != 12) endHour + 12 else if (endAmpm == 0 && endHour == 12) 0 else endHour) * 60) + endMinute

        return endTotalMinutes > startTotalMinutes
    }

    private fun showTodoDummyDeleteDialog() {
        val dialogBinding = DialogConfirmTodoDeleteBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.todoConfirmTv.setOnClickListener {
            Toast.makeText(requireContext(), "삭제되었습니다. (더미)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            dismiss()
        }

        dialogBinding.todoCancelTv.setOnClickListener {
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

    private fun showTodoCancelEditDialog() {
        val dialogBinding = DialogConfirmTodoEditBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.todoConfirmTv.setOnClickListener {
            dialog.dismiss()
            dismiss()
        }

        dialogBinding.todoCancelTv.setOnClickListener {
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

    private fun toggleCalendarVisibility() {
        isCalendarVisible = !isCalendarVisible

        if (isStartDateSelected) {
            binding.homeCalendarViewLl.visibility = if (isCalendarVisible) View.VISIBLE else View.GONE

            if (isCalendarVisible && calendarFragmentStart == null) {
                calendarFragmentStart = MonthlyCalendarFragment.newInstance(
                    position = Int.MAX_VALUE / 2,
                    onClickListener = this,
                    showDot = false
                )
                childFragmentManager.beginTransaction()
                    .replace(R.id.home_calendar_container_fl, calendarFragmentStart!!)
                    .commit()
            }
        } else {
            binding.homeCalendarView02Ll.visibility = if (isCalendarVisible) View.VISIBLE else View.GONE

            if (isCalendarVisible && calendarFragmentEnd == null) {
                calendarFragmentEnd = MonthlyCalendarFragment.newInstance(
                    position = Int.MAX_VALUE / 2,
                    onClickListener = this,
                    showDot = false
                )
                childFragmentManager.beginTransaction()
                    .replace(R.id.home_calendar_container_02_fl, calendarFragmentEnd!!)
                    .commit()
            }
        }
    }

    override fun onClickDate(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        val formattedDate = date.format(formatter)

        if (isStartDateSelected) {
            binding.startDateTv.text = formattedDate
            binding.homeCalendarViewLl.visibility = View.GONE
        } else {
            binding.endDateTv.text = formattedDate
            binding.homeCalendarView02Ll.visibility = View.GONE
        }

        isCalendarVisible = false
    }

}
