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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.teumteum.R

import com.example.teumteum.data.remote.todo.model.RegisterTodoRequest
import com.example.teumteum.data.remote.todo.model.ReminderAlarm
import com.example.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.example.teumteum.databinding.BottomSheetTodoRegisterBinding
import com.example.teumteum.ui.wish.BottomSheetWishRegisterFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.example.teumteum.ui.todo.viewModel.TodoViewModel
import com.example.teumteum.utils.applyPickerValue
import com.example.teumteum.utils.disableScroll
import com.example.teumteum.utils.dpToPx
import com.example.teumteum.utils.parseKoreanAmPmTimeToPickerValue
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class BottomSheetTodoRegisterFragment : BottomSheetDialogFragment()  {

    private var _binding: BottomSheetTodoRegisterBinding? = null
    private val binding get() = _binding!!

    private var currentTargetTextView: TextView? = null
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

    private var isTodoSelected = true

    private var isCalendarVisible = false
    private var isStartDateSelected = true
    private var selectedStartDate: LocalDate = LocalDate.now()
    private var selectedEndDate: LocalDate = LocalDate.now()
    private val today: LocalDate = LocalDate.now()

    private val viewModel: TodoViewModel by activityViewModels()
    private val myHomeViewModel: MyHomeViewModel by activityViewModels()

    private val minuteOptions = arrayOf("00", "10", "20", "30", "40", "50")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTodoRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // HomeFragment에서 넘겨준 인자 읽기
        val argDateStr = arguments?.getString("defaultDate")

        // SharedPreferences에 저장된 선택 날짜(백업 경로)
        val spDateStr = requireContext()
            .getSharedPreferences("CALENDAR-APP", Context.MODE_PRIVATE)
            .getString("SELECTED-DATE", null)

        // 변환: arguments > sharedPref > 오늘(LocalDate.now())
        val baseDate: LocalDate = listOfNotNull(argDateStr, spDateStr)
            .firstOrNull()
            ?.let { kotlin.runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()

        selectedStartDate = baseDate
        selectedEndDate = baseDate

        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
        val baseDateText = baseDate.format(formatter)

        // 시작/종료 날짜 기본값 세팅
        binding.startDateTv.text = baseDateText
        binding.endDateTv.text = baseDateText

        resetAlarmUI()
        setupPickers()

        setupStartCalendar()
        setupEndCalendar()
        disableCalendarScroll()

        setupWeekdayLabels()
        setupObservers()
        setupClickListeners()

        viewModel.getOnboardingReminders()

        // 원래 스크롤뷰 패딩 저장
        val originalBottomPadding = binding.registerScroll.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            // 버튼 실제 높이
            val btnH = binding.btnTodoRegister.height

            // 스크롤 영역: 키보드 + 버튼 높이만큼 바닥 패딩
            binding.registerScroll.setPadding(
                binding.registerScroll.paddingLeft,
                binding.registerScroll.paddingTop,
                binding.registerScroll.paddingRight,
                if (imeVisible) originalBottomPadding + btnH else originalBottomPadding
            )

            // 키보드 올라왔을 때 보이는 흰색 영역 제거
            binding.btnTodoRegister.visibility = if (imeVisible) View.GONE else View.VISIBLE

            insets
        }

        myHomeViewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            Log.d("ProfileImageCheck", "Image URL: $imageUrl")
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
    }

    private fun setupClickListeners() {
        binding.startTimeTv.setOnClickListener {
            if (isCalendarVisible) {
                toggleCalendarVisibility(show = false)
            }

            val isVisibleNow = binding.timePickerStartContainer.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = true)
            } else {
                parseKoreanAmPmTimeToPickerValue(
                    timeText = binding.startTimeTv.text.toString(),
                    minuteOptions = minuteOptions
                )?.let { v ->
                    binding.ampmPicker01Np.applyPickerValue(
                        hourPicker = binding.hourPicker01Np,
                        minutePicker = binding.minutePicker01Np,
                        value = v
                    )
                }
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
            } else {
                parseKoreanAmPmTimeToPickerValue(
                    timeText = binding.endTimeTv.text.toString(),
                    minuteOptions = minuteOptions
                )?.let { v ->
                    binding.ampmPicker02Np.applyPickerValue(
                        hourPicker = binding.hourPicker02Np,
                        minutePicker = binding.minutePicker02Np,
                        value = v
                    )
                }
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
                clearTodoSheet()

                binding.btnWish.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                binding.btnWish.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                binding.btnTodo.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teumteum_bg))
                binding.btnTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                isTodoSelected = false

                (requireView().findViewById<ViewGroup>(R.id.register_fragment_container)).removeAllViews()

                val tx = childFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .disallowAddToBackStack()
                    .replace(R.id.register_fragment_container, BottomSheetWishRegisterFragment(), "WishRegister")

                tx.commitNowAllowingStateLoss()
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

    private fun disableCalendarScroll() {
        binding.calendarView01.disableScroll()
        binding.calendarView02.disableScroll()
    }

    private fun setupStartCalendar() {
        val currentMonth = YearMonth.now()
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
        val currentMonth = YearMonth.now()
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

    private fun clearTodoSheet() {
        binding.todoTitleEt.setText("")
        binding.detailTextEt.setText("")

        binding.startTimeTv.text = "시작 시간"
        binding.endTimeTv.text  = "종료 시간"
        binding.timePickerStartContainer.isVisible = false
        binding.timePickerEndContainer.isVisible   = false
        currentTargetTextView = null

        toggleCalendarVisibility(show = false)

        binding.publicToggle01Iv.isChecked = false
        binding.includeToggle01Iv.isChecked = false

        resetAlarmUI()

        if (popupWindow?.isShowing == true) {
            popupWindow?.dismiss()
        }

        try {
            requireActivity().currentFocus?.clearFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        } catch (_: Exception) { }
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

        val screenW = resources.displayMetrics.widthPixels
        val popupWidth = (screenW * 0.6f).toInt()

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

            val moveRightPx = anchor.dpToPx(10)
            showAsDropDown(
                anchor,
                (-popupWidth + anchor.width) + moveRightPx,
                anchor.dpToPx(8)
            )
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

    private fun getSelectedRemindAlarms(): List<ReminderAlarm>? {
        val map = linkedMapOf<Int, AlarmStatus>()

        // 동적으로 추가된 알림들 모두 포함
        for (i in 0 until binding.alarmLayoutContainer.childCount) {
            val child = binding.alarmLayoutContainer.getChildAt(i)
            val toggle = child.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)
            val label  = child.findViewById<TextView>(R.id.alarm_set_tv).text.toString()
            val minute = alarmLabelToMinutes[label] ?: continue

            map[minute] = if (toggle.isChecked) AlarmStatus.ACTIVE else AlarmStatus.INACTIVE
        }

        if (map.isEmpty()) return null

        // 정렬하여 ReminderAlarm 리스트로 변환
        return map.entries
            .sortedBy { it.key }
            .map { (min, st) -> ReminderAlarm(alarm = min, status = st) }
    }

    private fun parseKoreanAmPmTime(timeText: String): LocalTime {
        val parts = timeText.trim().split(" ")
        val ampm = parts[0] // 오전/오후
        val (hStr, mStr) = parts[1].split(":")
        var hour = hStr.toInt()
        val minute = mStr.toInt()

        if (ampm == "오후" && hour != 12) hour += 12
        if (ampm == "오전" && hour == 12) hour = 0

        return LocalTime.of(hour, minute)
    }

    private fun getTodoRequest(): RegisterTodoRequest {
        val title = binding.todoTitleEt.text.toString()

        val startLocalTime = parseKoreanAmPmTime(binding.startTimeTv.text.toString())
        val endLocalTime = parseKoreanAmPmTime(binding.endTimeTv.text.toString())

        val startDateTime = LocalDateTime.of(selectedStartDate, startLocalTime)
        val endDateTime = LocalDateTime.of(selectedEndDate, endLocalTime)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        val description = binding.detailTextEt.text.toString()
        val isPublic = binding.publicToggle01Iv.isChecked
        val includeTeum = binding.includeToggle01Iv.isChecked
        val remindAlarm = getSelectedRemindAlarms()

        return RegisterTodoRequest(
            title = title,
            startTime = startDateTime.format(formatter),
            endTime = endDateTime.format(formatter),
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

        val request = getTodoRequest()
        viewModel.registerTodo(request)
    }

    private fun resetAlarmUI() {
        // 동적 항목 비우기
        binding.alarmLayoutContainer.removeAllViews()
        selectedItems.clear()
    }

    private fun applyRemindersFromMinutes(minutes: List<Int>) {
        resetAlarmUI()

        val sorted = minutes.sortedDescending()
        sorted.forEach { m ->
            val label = minutesToLabel[m] ?: return@forEach
            selectedItems.add(label)

            addAlarmItem(label)

            val child = (0 until binding.alarmLayoutContainer.childCount)
                .map { binding.alarmLayoutContainer.getChildAt(it) }
                .firstOrNull { it.tag == label }
            child?.findViewById<SwitchCompat>(R.id.alarm_toggle_tv)?.isChecked = true
        }
    }

    private fun setupObservers() {

        viewModel.onBoardingReminders.observe(viewLifecycleOwner) { minutes: List<Int> ->
            applyRemindersFromMinutes(minutes)
        }

        // 성공 이벤트는 Flow 수집으로 1회성 처리
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerSuccess.collect {
                    Log.d("TODO_REGISTER_FRAGMENT", "투두가 성공적으로 등록되었습니다.")
                    parentFragmentManager.setFragmentResult("todo_register_home", Bundle())

                    // 모든 바텀시트 닫기
                    (requireActivity().supportFragmentManager.fragments).forEach { fragment ->
                        if (fragment is BottomSheetDialogFragment) {
                            fragment.dismissAllowingStateLoss()
                        }
                    }
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Log.e("TODO_REGISTER_FRAGMENT", errorMsg.toString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        popupWindow?.dismiss()
        popupWindow = null
        _binding = null
    }
}