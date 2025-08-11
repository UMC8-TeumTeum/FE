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
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentTodoEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.teumteum.data.remote.todo.model.EditTodoRequest
import com.example.teumteum.databinding.DialogConfirmTodoDeleteBinding
import com.example.teumteum.databinding.DialogConfirmTodoEditBinding
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.utils.combineDateTime
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class TodoEditFragment : BottomSheetDialogFragment(), IDateClickListener {

    private var _binding: FragmentTodoEditBinding? = null
    private val binding get() = _binding!!

    private var currentTargetTextView: TextView? = null

    private var todoId: Long = -1

    private val selectedItems = mutableSetOf<String>()
    private val alarmOptions = listOf("30분 전", "10분 전", "5분 전", "3분 전", "1분 전")
    private var popupWindow: PopupWindow? = null

    private var isCalendarVisible = false
    private var calendarFragmentStart: MonthlyCalendarFragment? = null
    private var calendarFragmentEnd: MonthlyCalendarFragment? = null
    private var isStartDateSelected = true

    private val viewModel: TodoViewModel by activityViewModels()
    private val myHomeViewModel: MyHomeViewModel by activityViewModels()

    private val alarmLabelToMinutes = mapOf(
        "30분 전" to 30,
        "10분 전" to 10,
        "5분 전" to 5,
        "3분 전" to 3,
        "1분 전" to 1
    )

    private var originalTitle: String = ""
    private var originalStartTime: String = ""
    private var originalEndTime: String = ""
    private var originalDescription: String = ""
    private var originalIsPublic: Boolean = false
    private var originalIncludeTeum: Boolean = false
    private var originalRemindAlarm: List<Int> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todoId = arguments?.getLong("todo_id") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            edit()
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

        if (todoId != -1L) {
            viewModel.getTodo(todoId)
        }

        myHomeViewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_teum) // 기본 이미지 리소스
                    .error(R.drawable.gray_teum)       // 에러 시 이미지
                    .into(binding.profileIv)
            } else {
                binding.profileIv.setImageResource(R.drawable.gray_teum)
            }
        }

        setupObservers()
    }

    private fun getTodoRequest(): EditTodoRequest {
        val title = binding.todoTitleEt.text.toString()
        val startTime = combineDateTime(binding.startDateTv, binding.startTimeTv)
        val endTime = combineDateTime(binding.endDateTv, binding.endTimeTv)

        val description = binding.detailTextEt.text.toString()
        val isPublic = binding.publicToggle01Iv.isChecked
        val includeTeum = binding.includeToggle01Iv.isChecked
//        val remindAlarm = getSelectedRemindAlarms()

        return EditTodoRequest(
            title = title,
            startTime = startTime,
            endTime = endTime,
            description = description,
            isPublic = isPublic,
            includeTeum = includeTeum,
//            remindAlarm = remindAlarm
        )
    }

    private fun edit() {
        val title = binding.todoTitleEt.text.toString()
        val startTimeText = binding.startTimeTv.text.toString()
        val endTimeText = binding.endTimeTv.text.toString()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (startTimeText == "시작 시간" || endTimeText == "종료 시간") {
            Toast.makeText(requireContext(), "시작/종료 시간을 설정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = getTodoRequest()
        viewModel.editTodo(todoId, request)
    }

    private fun getSelectedRemindAlarms(): List<Int> {
        val alarms = mutableListOf<Int>()

        if (binding.alarmToggle01Iv.isChecked) {
            alarms.add(30)
        }
        if (binding.alarmToggle02Iv.isChecked) {
            alarms.add(10)
        }

        // 추가된 알림 항목들
        for (i in 0 until binding.alarmLayoutContainer.childCount) {
            val child = binding.alarmLayoutContainer.getChildAt(i)
            val toggle = child.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)
            val labelText = child.findViewById<TextView>(R.id.alarm_set_tv).text.toString()

            if (toggle.isChecked) { // 커스텀 토글이 실제로 체크 가능한 경우
                alarmLabelToMinutes[labelText]?.let { alarms.add(it) }
            }
        }

        return alarms
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
                if (isModified()) {
                    showTodoCancelEditDialog()
                } else {
                    dismiss() // 수정 없으면 바로 닫기
                }
                true
            } else {
                false
            }
        }

        return dialog
    }

    private fun isModified(): Boolean {
        val currentTitle = binding.todoTitleEt.text.toString().trim()
        val currentStartTime = combineDateTime(binding.startDateTv, binding.startTimeTv)
        val currentEndTime = combineDateTime(binding.endDateTv, binding.endTimeTv)
        val currentDescription = binding.detailTextEt.text.toString().trim()
        val currentIsPublic = binding.publicToggle01Iv.isChecked
        val currentIncludeTeum = binding.includeToggle01Iv.isChecked
        val currentRemindAlarm = getSelectedRemindAlarms()

        return currentTitle != originalTitle ||
                currentStartTime == originalStartTime ||
                currentEndTime == originalEndTime ||
                currentDescription != originalDescription ||
                currentIsPublic != originalIsPublic ||
                currentIncludeTeum != originalIncludeTeum ||
                currentRemindAlarm != originalRemindAlarm
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
            viewModel.deleteTodo(todoId)
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

    companion object {
        fun newInstance(todoId: Long): TodoEditFragment {
            return TodoEditFragment().apply {
                arguments = Bundle().apply {
                    putLong("todo_id", todoId)
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.todo.observe(viewLifecycleOwner) { todo ->
            if (todo == null) return@observe

            binding.todoTitleEt.setText(todo.title)

            val startDateTime = LocalDateTime.parse(todo.startTime)
            val endDateTime = LocalDateTime.parse(todo.endTime)

            val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
            val timeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)

            binding.startDateTv.text = startDateTime.toLocalDate().format(dateFormatter)
            binding.endDateTv.text = endDateTime.toLocalDate().format(dateFormatter)

            binding.startTimeTv.text = startDateTime.toLocalTime().format(timeFormatter)
            binding.endTimeTv.text = endDateTime.toLocalTime().format(timeFormatter)

            binding.detailTextEt.setText(todo.description)

            binding.publicToggle01Iv.isChecked = todo.isPublic

            binding.includeToggle01Iv.isChecked = todo.includeTeum

            todo.remindAlarm?.forEach { minutes ->
                val label = alarmLabelToMinutes.entries.firstOrNull { it.value == minutes }?.key
                label?.let {
                    if (!selectedItems.contains(it)) {
                        selectedItems.add(it)
                        addAlarmItem(it)
                    }

                    when (it) {
                        "30분 전" -> binding.alarmToggle01Iv.isChecked = true
                        "10분 전" -> binding.alarmToggle02Iv.isChecked = true
                        else -> {

                            for (i in 0 until binding.alarmLayoutContainer.childCount) {
                                val child = binding.alarmLayoutContainer.getChildAt(i)
                                val labelText = child.findViewById<TextView>(R.id.alarm_set_tv).text.toString()
                                if (labelText == it) {
                                    val toggle = child.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)
                                    toggle.isChecked = true
                                    break
                                }
                            }
                        }
                    }
                }
            }

            // 선택 여부 확인용 원본 저장
            originalTitle = todo.title
            originalStartTime = todo.startTime
            originalEndTime = todo.endTime
            originalDescription = todo.description
            originalIsPublic = todo.isPublic
            originalIncludeTeum = todo.includeTeum
            originalRemindAlarm = todo.remindAlarm ?: emptyList()

            if (todo.type.name == "ROUTINE") {
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

                binding.alarmToggle01Iv.isEnabled = false
                binding.alarmToggle02Iv.isEnabled = false
                binding.publicToggle01Iv.isEnabled = false
                binding.includeToggle01Iv.isEnabled = false

                binding.btnTodoDelete.isEnabled = false
                binding.btnTodoSave.isEnabled = false

                for (i in 0 until binding.alarmLayoutContainer.childCount) {
                    val alarmView = binding.alarmLayoutContainer.getChildAt(i)

                    if (alarmView is ViewGroup) {
                        for (j in 0 until alarmView.childCount) {
                            val child = alarmView.getChildAt(j)

                            (child as? TextView)?.setTextColor(deactiveColor)
                            (child as? SwitchCompat)?.apply {
                                isEnabled = false
                                trackDrawable = ContextCompat.getDrawable(context, R.drawable.style_toggle_disabled_btn)
                                thumbDrawable = ContextCompat.getDrawable(context, R.drawable.style_toggle_disabled_thumb)
                            }
                        }
                    }
                }

                Toast.makeText(requireContext(), "반복일정은 편집할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            parentFragmentManager.setFragmentResult("todo_get", Bundle())
        }

        viewModel.editSuccess.observe(viewLifecycleOwner) {
            if (it == true) {
                Toast.makeText(requireContext(), "투두가 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("todo_edit", Bundle())
                dismiss()  // 현재 바텀시트만 닫기
            }
        }

        // 삭제 성공 시
        viewModel.deleteSuccess.observe(viewLifecycleOwner) {
            if (it == true) {
                Toast.makeText(requireContext(), "투두가 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("todo_delete", Bundle())
                dismiss()  // 현재 바텀시트만 닫기
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
