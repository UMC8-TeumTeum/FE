package com.example.teumteum.ui.todo

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.teumteum.data.remote.todo.model.EditTodoRequest
import com.example.teumteum.data.remote.todo.model.ReminderAlarm
import com.example.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.example.teumteum.databinding.BottomSheetTodoEditBinding
import com.example.teumteum.databinding.DialogConfirmAiContentDeleteBinding
import com.example.teumteum.databinding.DialogConfirmTeumDeleteBinding
import com.example.teumteum.databinding.DialogConfirmTodoDeleteBinding
import com.example.teumteum.databinding.DialogConfirmTodoEditBinding
import com.example.teumteum.databinding.DialogConfirmWishDeleteBinding

import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.todo.adapter.TeumProfileAdapter
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.utils.TimeUtils.combineDateTime
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@AndroidEntryPoint
class BottomSheetTodoEditFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTodoEditBinding? = null
    private val binding get() = _binding!!

    private var currentTargetTextView: TextView? = null

    private var todoId: Long = -1

    private val profileAdapter by lazy { TeumProfileAdapter() }

    private var popupWindow: PopupWindow? = null

    private val alarmLabelToMinutes = mutableMapOf(
        "30분 전" to 30,
        "10분 전" to 10,
        "5분 전" to 5,
        "3분 전" to 3,
        "1분 전" to 1
    )

    private val minutesToLabel = alarmLabelToMinutes.entries.associate { (k, v) -> v to k }.toMutableMap()
    private val selectedItems = mutableSetOf<String>()
    private val alarmOptions = mutableListOf<String>().apply { addAll(alarmLabelToMinutes.keys) }

    private var isCalendarVisible = false
    private var isStartDateSelected = true
    private var selectedStartDate: LocalDate = LocalDate.now()
    private var selectedEndDate: LocalDate = LocalDate.now()
    private val today: LocalDate = LocalDate.now()

    private val viewModel: TodoViewModel by activityViewModels()
    private val friendViewModel: FriendViewModel by activityViewModels()

    private var originalTitle: String = ""
    private var originalStartTime: String = ""
    private var originalEndTime: String = ""
    private var originalDescription: String = ""
    private var originalIsPublic: Boolean = false
    private var originalIncludeTeum: Boolean = false
    private var originalRemindAlarm: List<Int> = emptyList()

    private var _normalTextColor: Int? = null
    private var _normalHintColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todoId = arguments?.getLong("todo_id") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTodoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = getTodayFormatted()

        // 시작/종료 날짜를 오늘 날짜로 초기화
        binding.startDateTv.text = today
        binding.endDateTv.text = today

        val scheduleType: ScheduleType = arguments?.getString("schedule_type")
            ?.let { runCatching { ScheduleType.valueOf(it) }.getOrNull() }
            ?: ScheduleType.TODO

        val label = when (scheduleType) {
            ScheduleType.TODO    -> "투두"
            ScheduleType.AI      -> "AI 콘텐츠"
            ScheduleType.TEUM    -> "틈 약속"
            ScheduleType.ROUTINE -> "투두"
            ScheduleType.WISH    -> "위시"
        }

        binding.btnTodo.text = label

        binding.profileImageRc.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = profileAdapter
        }

        resetAlarmUI()
        setupPickers()
        setupStartCalendar()
        setupEndCalendar()
        setupWeekdayLabels()
        setupClickListeners(scheduleType)
        setupObservers()

        // 원래 스크롤뷰 패딩 저장
        val originalBottomPadding = binding.editScroll.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            // 버튼 실제 높이
            val btnH = binding.todoBottomBar.height

            // 스크롤 영역: 키보드 + 버튼 높이만큼 바닥 패딩
            binding.editScroll.setPadding(
                binding.editScroll.paddingLeft,
                binding.editScroll.paddingTop,
                binding.editScroll.paddingRight,
                if (imeVisible) originalBottomPadding + btnH else originalBottomPadding
            )

            // 키보드 올라왔을 때 보이는 흰색 영역 제거
            binding.todoBottomBar.visibility = if (imeVisible) View.GONE else View.VISIBLE

            insets
        }

        if (todoId != -1L) {
            viewModel.getTodo(todoId)
        }
    }

    private fun setupClickListeners(scheduleType: ScheduleType) {
        binding.startTimeTv.setOnClickListener {
            if (isCalendarVisible) {
                toggleCalendarVisibility(show = false)
            }
            val isVisibleNow = binding.timePickerStartContainer.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = true)
            }
            binding.timePickerStartContainer.isVisible = !isVisibleNow
            binding.timePickerEndContainer.isVisible = false
            currentTargetTextView = binding.startTimeTv.takeIf { !isVisibleNow }
        }

        binding.endTimeTv.setOnClickListener {
            if (isCalendarVisible) {
                toggleCalendarVisibility(show = false)
            }
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
            when (scheduleType) {
                ScheduleType.TODO    -> showTodoDeleteDialog()
                ScheduleType.TEUM    -> showTeumDeleteDialog()
                ScheduleType.AI      -> showAiDeleteDialog()
                ScheduleType.WISH    -> showWishDeleteDialog()
                else                 -> showTodoDeleteDialog()
            }
        }

        binding.startDateTv.setOnClickListener {
            if (binding.timePickerStartContainer.isVisible || binding.timePickerEndContainer.isVisible) {
                binding.timePickerStartContainer.isVisible = false
                binding.timePickerEndContainer.isVisible = false
                currentTargetTextView = null
            }
            isStartDateSelected = true
            toggleCalendarVisibility(show = true)
        }

        binding.endDateTv.setOnClickListener {
            if (binding.timePickerStartContainer.isVisible || binding.timePickerEndContainer.isVisible) {
                binding.timePickerStartContainer.isVisible = false
                binding.timePickerEndContainer.isVisible = false
                currentTargetTextView = null
            }
            isStartDateSelected = false
            toggleCalendarVisibility(show = true)
        }
    }

    private fun setupStartCalendar() {
        val currentMonth = java.time.YearMonth.now()
        val startMonth = currentMonth.minusYears(50)
        val endMonth = currentMonth.plusYears(50)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView01.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView01.scrollToMonth(currentMonth)

        binding.calendarView01.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val tv = container.textView
                tv.text = day.date.dayOfMonth.toString()
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                val isThisMonth = day.position == DayPosition.MonthDate
                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isThisMonth) R.color.text_primary else R.color.teumteum_deactive
                    )
                )

                if (day.date == today && isThisMonth) {
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.teumteum_gray))
                }

                if (day.date == selectedStartDate && isThisMonth) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.main_1))
                }

                container.view.setOnClickListener {
                    if (!isThisMonth) return@setOnClickListener
                    val old = selectedStartDate
                    selectedStartDate = day.date
                    binding.calendarView01.notifyDateChanged(old)
                    binding.startDateTv.text = day.date.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
                    binding.calendarView01.notifyDateChanged(day.date)
                    toggleCalendarVisibility(show = false)
                }
            }
        }
    }

    private fun setupEndCalendar() {
        val currentMonth = java.time.YearMonth.now()
        val startMonth = currentMonth.minusYears(50)
        val endMonth = currentMonth.plusYears(50)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView02.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView02.scrollToMonth(currentMonth)

        binding.calendarView02.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val tv = container.textView
                tv.text = day.date.dayOfMonth.toString()
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                val isThisMonth = day.position == DayPosition.MonthDate
                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isThisMonth) R.color.text_primary else R.color.teumteum_deactive
                    )
                )

                if (day.date == today && isThisMonth) {
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.teumteum_gray))
                }

                if (day.date == selectedEndDate && isThisMonth) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.main_1))
                }

                container.view.setOnClickListener {
                    if (!isThisMonth) return@setOnClickListener
                    val old = selectedEndDate
                    selectedEndDate = day.date
                    binding.calendarView02.notifyDateChanged(old)
                    binding.endDateTv.text = day.date.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
                    binding.calendarView02.notifyDateChanged(day.date)
                    toggleCalendarVisibility(show = false)
                }
            }
        }
    }

    private fun setupWeekdayLabels() {
        val container1 = binding.calendarWeekdaysRow01
        container1.removeAllViews()
        val container2 = binding.calendarWeekdaysRow02
        container2.removeAllViews()

        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val days = (0..6).map { firstDayOfWeek.plus(it.toLong()) }
        days.forEach { dow ->
            val tv1 = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                text = weekdayShortKorean(dow)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }
            container1.addView(tv1)

            val tv2 = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                text = weekdayShortKorean(dow)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }
            container2.addView(tv2)
        }
    }

    private fun weekdayShortKorean(dow: java.time.DayOfWeek): String = when (dow) {
        java.time.DayOfWeek.SUNDAY -> "일"
        java.time.DayOfWeek.MONDAY -> "월"
        java.time.DayOfWeek.TUESDAY -> "화"
        java.time.DayOfWeek.WEDNESDAY -> "수"
        java.time.DayOfWeek.THURSDAY -> "목"
        java.time.DayOfWeek.FRIDAY -> "금"
        java.time.DayOfWeek.SATURDAY -> "토"
    }

    private fun toggleCalendarVisibility(show: Boolean) {
        if (show) {
            binding.calendarHeaderLayout01.isVisible = isStartDateSelected
            binding.calendarHeaderLayout02.isVisible = !isStartDateSelected
            binding.timePickerStartContainer.isVisible = false
            binding.timePickerEndContainer.isVisible = false
            isCalendarVisible = true
        } else {
            binding.calendarHeaderLayout01.isVisible = false
            binding.calendarHeaderLayout02.isVisible = false
            isCalendarVisible = false
        }
    }

    private fun getTodoRequest(): EditTodoRequest {
        val title = binding.todoTitleEt.text.toString()
        val startTime = combineDateTime(binding.startDateTv, binding.startTimeTv)
        val endTime = combineDateTime(binding.endDateTv, binding.endTimeTv)

        val description = binding.detailTextEt.text.toString()
        val isPublic = binding.publicToggle01Iv.isChecked
        val includeTeum = binding.includeToggle01Iv.isChecked
        val remindAlarm = getSelectedRemindAlarms()

        return EditTodoRequest(
            title = title,
            startTime = startTime,
            endTime = endTime,
            description = description,
            isPublic = isPublic,
            includeTeum = includeTeum,
            remindAlarm = remindAlarm
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

    private fun getSelectedRemindAlarms(): List<ReminderAlarm>? {
        val result = mutableListOf<ReminderAlarm>()
        for (i in 0 until binding.alarmLayoutContainer.childCount) {
            val child = binding.alarmLayoutContainer.getChildAt(i)
            val toggle = child.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)
            val label  = child.findViewById<TextView>(R.id.alarm_set_tv).text.toString()
            val minute = alarmLabelToMinutes[label] ?: continue

            result.add(
                ReminderAlarm(
                    alarm = minute,
                    status = if (toggle.isChecked) AlarmStatus.ACTIVE else AlarmStatus.INACTIVE
                )
            )
        }
        return if (result.isEmpty()) null else result
    }

    private fun isModified(): Boolean {
        val currentTitle = binding.todoTitleEt.text.toString().trim()

        val currentStartTime = combineDateTime(binding.startDateTv, binding.startTimeTv)
        val currentEndTime = combineDateTime(binding.endDateTv, binding.endTimeTv)

        val currentDescription = binding.detailTextEt.text.toString().trim()
        val currentIsPublic = binding.publicToggle01Iv.isChecked
        val currentIncludeTeum = binding.includeToggle01Iv.isChecked
        val currentRemindAlarm = getSelectedRemindAlarms()

        Log.d("isModifiedCheck", """
        currentTitle: $currentTitle / originalTitle: $originalTitle / changed: ${currentTitle != originalTitle}
        currentStartTime: $currentStartTime / originalStartTime: $originalStartTime / changed: ${currentStartTime != originalStartTime}
        currentEndTime: $currentEndTime / originalEndTime: $originalEndTime / changed: ${currentEndTime != originalEndTime}
        currentDescription: $currentDescription / originalDescription: $originalDescription / changed: ${currentDescription != originalDescription}
        currentIsPublic: $currentIsPublic / originalIsPublic: $originalIsPublic / changed: ${currentIsPublic != originalIsPublic}
        currentIncludeTeum: $currentIncludeTeum / originalIncludeTeum: $originalIncludeTeum / changed: ${currentIncludeTeum != originalIncludeTeum}
        currentRemindAlarm: $currentRemindAlarm / originalRemindAlarm: $originalRemindAlarm / changed: ${currentRemindAlarm != originalRemindAlarm}
    """.trimIndent())

        return currentTitle != originalTitle ||
                currentStartTime != originalStartTime ||
                currentEndTime != originalEndTime ||
                currentDescription != originalDescription ||
                currentIsPublic != originalIsPublic ||
                currentIncludeTeum != originalIncludeTeum ||
                currentRemindAlarm != originalRemindAlarm
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
            binding.endTimeTv.text = timeText
            binding.timePickerEndContainer.isVisible = false
        }
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
        val layout = layoutInflater.inflate(R.layout.item_alarm, binding.alarmLayoutContainer, false)
        val labelText = layout.findViewById<TextView>(R.id.alarm_set_tv)
        labelText.text = label
        layout.tag = label
        binding.alarmLayoutContainer.addView(layout)

        // 내림차순 정렬
        val sortedChildren = (0 until binding.alarmLayoutContainer.childCount).map { i ->
            val child = binding.alarmLayoutContainer.getChildAt(i)
            val childLabel = child.tag as? String
                ?: child.findViewById<TextView>(R.id.alarm_set_tv).text.toString()
            val minute = alarmLabelToMinutes[childLabel] ?: Int.MIN_VALUE
            minute to child
        }.sortedByDescending { it.first }

        binding.alarmLayoutContainer.removeAllViews()
        sortedChildren.forEach { (_, child) -> binding.alarmLayoutContainer.addView(child) }
    }

    private fun removeAlarmItem(label: String) {
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

    private fun resetAlarmUI() {
        binding.alarmLayoutContainer.removeAllViews()
        selectedItems.clear()
    }

    private fun showTodoDeleteDialog() {
        val dialogBinding = DialogConfirmTodoDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.todoConfirmTv.setOnClickListener {
            dialogBinding.todoConfirmTv.isEnabled = false
            dialog.dismiss()
            viewModel.deleteTodo(todoId)
        }
        dialogBinding.todoCancelTv.setOnClickListener { dialog.dismiss() }

        applyDialogWindow(dialog)
        dialog.show()
    }

    private fun showTeumDeleteDialog() {
        val dialogBinding = DialogConfirmTeumDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.todoConfirmTv.setOnClickListener {
            dialogBinding.todoConfirmTv.isEnabled = false
            dialog.dismiss()
            friendViewModel.cancelTeumSchedule(todoId.toInt())
        }
        dialogBinding.todoCancelTv.setOnClickListener { dialog.dismiss() }

        applyDialogWindow(dialog)
        dialog.show()
    }

    private fun showAiDeleteDialog() {
        val dialogBinding = DialogConfirmAiContentDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.todoConfirmTv.setOnClickListener {
            dialogBinding.todoConfirmTv.isEnabled = false
            dialog.dismiss()
            viewModel.deleteTodo(todoId)
        }
        dialogBinding.todoCancelTv.setOnClickListener { dialog.dismiss() }

        applyDialogWindow(dialog)
        dialog.show()
    }

    private fun showWishDeleteDialog() {
        val dialogBinding = DialogConfirmWishDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.wishConfirmTv.setOnClickListener {
            dialogBinding.wishConfirmTv.isEnabled = false
            dialog.dismiss()
            viewModel.deleteTodo(todoId)
        }
        dialogBinding.wishCancelTv.setOnClickListener { dialog.dismiss() }

        applyDialogWindow(dialog)
        dialog.show()
    }

    private fun applyDialogWindow(dialog: AlertDialog) {
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnShowListener {
            dialog.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.width  = (resources.displayMetrics.widthPixels * 0.85).toInt()
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.y = (resources.displayMetrics.heightPixels * 0.37).toInt()
                layoutParams.dimAmount = 0.5f
                window.attributes = layoutParams
                window.setDimAmount(0.5f)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
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

    companion object {
        fun newInstance(todoId: Long): BottomSheetTodoEditFragment {
            return BottomSheetTodoEditFragment().apply {
                arguments = Bundle().apply {
                    putLong("todo_id", todoId)
                }
            }
        }
    }

    private fun applyReminders(reminds: List<ReminderAlarm>) {
        resetAlarmUI()
        reminds
            .sortedByDescending { it.alarm }
            .forEach { ra ->
                val label = minutesToLabel[ra.alarm] ?: return@forEach

                addAlarmItem(label)

                // 추가된 뷰 찾아 토글 상태 반영
                val child = (0 until binding.alarmLayoutContainer.childCount)
                    .asSequence()
                    .map { binding.alarmLayoutContainer.getChildAt(it) }
                    .firstOrNull { (it.tag as? String) == label }

                child?.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)?.isChecked =
                    (ra.status == AlarmStatus.ACTIVE)

                selectedItems.add(label)
            }
    }

    private fun getTodayFormatted(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        return today.format(formatter)
    }

    private fun setupObservers() {
        viewModel.todo.observe(viewLifecycleOwner) { todo ->
            if (todo == null) return@observe

            // 기본 색 저장
            if (_normalTextColor == null) {
                _normalTextColor = binding.todoTitleEt.currentTextColor
                _normalHintColor = binding.detailTextEt.currentHintTextColor
            }

            run {
                listOf(
                    binding.todoTitleEt, binding.startDateTv, binding.startTimeTv,
                    binding.endDateTv, binding.endTimeTv, binding.detailTextEt
                ).forEach { v ->
                    v.isEnabled = true
                    v.alpha = 1f
                }

                // 텍스트/힌트 색 저장
                val nt = _normalTextColor ?: binding.todoTitleEt.currentTextColor
                val nh = _normalHintColor ?: binding.detailTextEt.currentHintTextColor

                binding.timerIconIv.clearColorFilter()
                binding.publicIconIv.clearColorFilter()
                binding.includeIconIv.clearColorFilter()
                binding.detailTextIv.clearColorFilter()

                binding.todoTitleEt.setTextColor(nt)
                binding.startDateTv.setTextColor(nt)
                binding.startTimeTv.setTextColor(nt)
                binding.endDateTv.setTextColor(nt)
                binding.endTimeTv.setTextColor(nt)
                binding.publicSettingTv.setTextColor(nt)
                binding.includeReportTv.setTextColor(nt)
                binding.detailTextEt.setTextColor(nt)
                binding.detailTextEt.setHintTextColor(nh)

                // 토글 저장
                listOf(
                    binding.publicToggle01Iv, binding.includeToggle01Iv
                ).forEach { t ->
                    t.isEnabled = true
                    t.trackDrawable = ContextCompat.getDrawable(t.context, R.drawable.style_toggle_btn)?.mutate()
                    t.thumbDrawable = ContextCompat.getDrawable(t.context, R.drawable.style_toggle_thumb)?.mutate()
                }

                resetAlarmUI()
                applyReminders(todo.remindAlarm ?: emptyList())
            }

            binding.todoTitleEt.setText(todo.title)

            val startDateTime = parseApiDateTime(todo.startTime)
            val endDateTime   = parseApiDateTime(todo.endTime)

            val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
            val timeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)

            binding.startDateTv.text = startDateTime.toLocalDate().format(dateFormatter)
            binding.endDateTv.text = endDateTime.toLocalDate().format(dateFormatter)
            binding.startTimeTv.text = startDateTime.toLocalTime().format(timeFormatter)
            binding.endTimeTv.text = endDateTime.toLocalTime().format(timeFormatter)

            binding.detailTextEt.setText(todo.description)
            binding.publicToggle01Iv.isChecked = todo.isPublic
            binding.includeToggle01Iv.isChecked = todo.includeTeum

            val urls: List<String> = todo.profileUrl ?: emptyList()
            profileAdapter.submitList(urls)
            binding.profileImageRc.isVisible = urls.isNotEmpty()

            // 선택 여부 확인용 원본 저장
            originalTitle = todo.title
            combineDateTime(binding.startDateTv, binding.startTimeTv)
            originalStartTime = todo.startTime
            originalEndTime = todo.endTime

            originalDescription = todo.description
            originalIsPublic = todo.isPublic
            originalIncludeTeum = todo.includeTeum
            originalRemindAlarm = (todo.remindAlarm ?: emptyList()).map { it.alarm }

            // 반복일정은 알림 편집만 가능
            if (todo.type == ScheduleType.ROUTINE) {
                disableRoutineEditing()
            }

            // 약속된 틈은 일부 수정 가능(알림 편집, 공개 설정, 빈틈시간 기록 포함, 상세 내용)
            if (todo.type == ScheduleType.TEUM) {
                disableRoutineEditing()
            }

            parentFragmentManager.setFragmentResult("todo_get", Bundle())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 수정 성공
                launch {
                    viewModel.editSuccess.collect {
                        Log.d("TODO_EDIT_FRAGMENT", "투두가 성공적으로 수정되었습니다.")
                        parentFragmentManager.setFragmentResult("todo_edit_home", Bundle())
                        parentFragmentManager.setFragmentResult("todo_edit_calendar", Bundle())
                        dismissAllSheets()
                    }
                }

                // 삭제 성공
                launch {
                    viewModel.deleteSuccess.collect {
                        Log.d("TODO_EDIT_FRAGMENT", "투두가 성공적으로 삭제되었습니다.")
                        parentFragmentManager.setFragmentResult("todo_delete_home", Bundle())
                        parentFragmentManager.setFragmentResult("todo_delete_calendar", Bundle())
                        dismissAllSheets()
                    }
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Log.e("TODO_EDIT_FRAGMENT", errorMsg.toString())
        }

        // 수면패턴 or 틈요청 충돌 + 시간 유효성 검사
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerError.collect { err ->
                    when (err.code) {
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

    private fun parseApiDateTime(raw: String): LocalDateTime {
        return try {
            LocalDateTime.parse(raw) // 정상(0~23시)인 경우
        } catch (e: DateTimeParseException) {
            // 24:MM[:SS] 대응 (예: 2025-08-14T24:00 또는 2025-08-14T24:00:00)
            val m = Regex("""^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$""").matchEntire(raw)
                ?: throw e
            val date = LocalDate.parse(m.groupValues[1])
            val hour = m.groupValues[2].toInt()
            val minute = m.groupValues[3].toInt()
            val second = m.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
            if (hour == 24) date.plusDays(1).atTime(0, minute, second) else throw e
        }
    }

    // 채운 동그라미 배경
    private fun circleFill(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
        }
    }

    // DayView의 뷰 홀더
    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendar_day_tv)
    }

    private fun dismissAllSheets() {
        (requireActivity().supportFragmentManager.fragments).forEach { fragment ->
            if (fragment is BottomSheetDialogFragment) {
                fragment.dismissAllowingStateLoss()
            }
        }
    }

    private fun disableRoutineEditing() {
        val deactiveColor = ContextCompat.getColor(requireContext(), R.color.teumteum_deactive)

        binding.todoTitleEt.setTextColor(deactiveColor)
        binding.timerIconIv.setColorFilter(deactiveColor)
        binding.startDateTv.setTextColor(deactiveColor)
        binding.startTimeTv.setTextColor(deactiveColor)
        binding.endDateTv.setTextColor(deactiveColor)
        binding.endTimeTv.setTextColor(deactiveColor)
        binding.publicIconIv.setColorFilter(deactiveColor)
        binding.publicSettingTv.setTextColor(deactiveColor)
        binding.includeIconIv.setColorFilter(deactiveColor)
        binding.includeReportTv.setTextColor(deactiveColor)
        binding.detailTextIv.setColorFilter(deactiveColor)
        binding.detailTextEt.setTextColor(deactiveColor)
        binding.detailTextEt.setHintTextColor(deactiveColor)

        // 입력/선택 비활성화
        setViewsEnabled(
            enabled = false,
            binding.todoTitleEt,
            binding.startDateTv, binding.startTimeTv,
            binding.endDateTv, binding.endTimeTv,
            binding.detailTextEt,
        )

        // 토글 비활성화
        setTogglesEnabled(
            enabled = false,
            binding.publicToggle01Iv,
            binding.includeToggle01Iv
        )
    }

    private fun disableTeumEditing() {
        val deactiveColor = ContextCompat.getColor(requireContext(), R.color.teumteum_deactive)

        binding.todoTitleEt.setTextColor(deactiveColor)
        binding.timerIconIv.setColorFilter(deactiveColor)
        binding.startDateTv.setTextColor(deactiveColor)
        binding.startTimeTv.setTextColor(deactiveColor)
        binding.endDateTv.setTextColor(deactiveColor)
        binding.endTimeTv.setTextColor(deactiveColor)

        // 입력/선택 비활성화
        setViewsEnabled(
            enabled = false,
            binding.todoTitleEt,
            binding.startDateTv, binding.startTimeTv,
            binding.endDateTv, binding.endTimeTv,
        )
    }

    private fun setViewsEnabled(enabled: Boolean, vararg views: View) {
        views.forEach { v ->
            v.isEnabled = enabled
            v.alpha = if (enabled) 1f else 1f
        }
    }

    private fun setTogglesEnabled(enabled: Boolean, vararg toggles: SwitchCompat) {
        toggles.forEach { t ->
            t.isEnabled = enabled
            t.trackDrawable = ContextCompat.getDrawable(
                t.context,
                if (enabled) R.drawable.style_toggle_btn else R.drawable.style_toggle_disabled_btn
            )?.mutate()
            t.thumbDrawable = ContextCompat.getDrawable(
                t.context,
                if (enabled) R.drawable.style_toggle_thumb else R.drawable.style_toggle_disabled_thumb
            )?.mutate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        popupWindow?.dismiss()
        popupWindow = null
        _binding = null
    }
}
