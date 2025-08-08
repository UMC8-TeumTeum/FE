package com.example.teumteum.ui.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.teumteum.databinding.FragmentTodoRegisterBinding
import com.example.teumteum.R

import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.ui.wish.WishRegisterFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import com.example.teumteum.ui.main.data.TimeBlock
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.utils.combineDateTime
import dagger.hilt.android.AndroidEntryPoint

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class TodoRegisterFragment : BottomSheetDialogFragment(), IDateClickListener{

    private var _binding: FragmentTodoRegisterBinding? = null
    private val binding get() = _binding!!

    private var currentTargetTextView: TextView? = null
    private var popupWindow: PopupWindow? = null

    private val alarmLabelToMinutes = mapOf(
        "30분 전" to 30,
        "10분 전" to 10,
        "5분 전" to 5,
        "3분 전" to 3,
        "1분 전" to 1
    )

    private val selectedItems = mutableSetOf("30분 전", "10분 전")
    private val alarmOptions = alarmLabelToMinutes.keys.toList()

    private var isTodoSelected = true

    private var isCalendarVisible = false
    private var calendarFragmentStart: MonthlyCalendarFragment? = null
    private var calendarFragmentEnd: MonthlyCalendarFragment? = null
    private var isStartDateSelected = true

    private val todoViewModel: TodoViewModel by viewModels()

    private var sleepStart: LocalTime? = null
    private var sleepEnd: LocalTime? = null

    private var sleepBlocks: List<TimeBlock> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sleepBlocks = it.getParcelableArrayList("sleepBlocks") ?: emptyList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        todoViewModel.getOnboardingReminders()

        arguments?.let {
            val start = it.getString("sleepStart")
            val end = it.getString("sleepEnd")
            sleepStart = start?.let { LocalTime.parse(it) }
            sleepEnd = end?.let { LocalTime.parse(it) }
        }

        val today = getTodayFormatted()

        // 시작/종료 날짜를 오늘 날짜로 초기화
        binding.startDateTv.text = today
        binding.endDateTv.text = today

        selectedItems.forEach { label -> addAlarmItem(label) }

        setupPickers()

        selectedItems.forEach { label ->
            addAlarmItem(label)
        }

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

        binding.btnTodoRegister.setOnClickListener {
            register()
        }

        binding.btnWish.setOnClickListener {
            if (isTodoSelected) {
                binding.btnWish.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                binding.btnWish.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                binding.btnTodo.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teumteum_bg))
                binding.btnTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                isTodoSelected = false

                childFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, WishRegisterFragment())
                    .commit()
            }
        }

        binding.startDateTv.setOnClickListener {
            isStartDateSelected = true
            toggleCalendarVisibility()
        }

        binding.endDateTv.setOnClickListener {
            isStartDateSelected = false
            toggleCalendarVisibility()
        }

        setupObservers()
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
                AlertDialog.Builder(requireContext())
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
            "30분 전" -> binding.alarmItem01Ll.visibility = View.VISIBLE
            "10분 전" -> binding.alarmItem02Ll.visibility = View.VISIBLE
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
                binding.alarmToggle01Iv.isChecked = false
            }
            "10분 전" -> {
                binding.alarmItem02Ll.visibility = View.GONE
                binding.alarmToggle02Iv.isChecked = false
            }
            else -> {
                for (i in 0 until binding.alarmLayoutContainer.childCount) {
                    val child = binding.alarmLayoutContainer.getChildAt(i)
                    if (child.tag == label) {
                        val toggle = child.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)
                        toggle.isChecked = false
                        binding.alarmLayoutContainer.removeView(child)
                        break
                    }
                }
            }
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

        return dialog
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

    private fun getTodayFormatted(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        return today.format(formatter)
    }

    private fun getTodoRequest(): RegisterTodoRequest {
        val title = binding.todoTitleEt.text.toString()
        val startTime = combineDateTime(binding.startDateTv, binding.startTimeTv)
        val endTime = combineDateTime(binding.endDateTv, binding.endTimeTv)

        val description = binding.detailTextEt.text.toString()
        val isPublic = binding.publicToggle01Iv.isChecked
        val includeTeum = binding.includeToggle01Iv.isChecked
        val remindAlarm = getSelectedRemindAlarms()

        return RegisterTodoRequest(
            title = title,
            startTime = startTime,
            endTime = endTime,
            description = description,
            isPublic = isPublic,
            includeTeum = includeTeum,
            remindAlarm = remindAlarm
        )
    }

    private fun register() {
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

        val startTime = combineDateTime(binding.startDateTv, binding.startTimeTv)
        val endTime = combineDateTime(binding.endDateTv, binding.endTimeTv)
        val startLocalTime = LocalTime.parse(startTime.substring(11)) // HH:mm
        val endLocalTime = LocalTime.parse(endTime.substring(11))

        val startMin = startLocalTime.hour * 60 + startLocalTime.minute
        val endMin = endLocalTime.hour * 60 + endLocalTime.minute

        if (isOverlappingWithSleep(startMin, endMin)) {
            Toast.makeText(requireContext(), "해당 시간에는 수면 패턴이 존재합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = getTodoRequest()
        todoViewModel.registerTodo(request)
    }

    private fun isOverlappingWithSleep(startMin: Int, endMin: Int): Boolean {
        return sleepBlocks.any { sleep ->
            val sleepStart = sleep.startTime
            val sleepEnd = sleep.endTime
            // 겹치는 경우
            startMin < sleepEnd && endMin > sleepStart
        }
    }

    private fun setupObservers() {

        todoViewModel.registerSuccess.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "투두가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult("todo_register", Bundle())

            // 모든 바텀시트 닫기
            (requireActivity().supportFragmentManager.fragments).forEach { fragment ->
                if (fragment is BottomSheetDialogFragment) {
                    fragment.dismissAllowingStateLoss()
                }
            }
        }

        todoViewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

}