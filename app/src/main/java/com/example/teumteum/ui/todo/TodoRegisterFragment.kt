package com.example.teumteum.ui.todo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.teumteum.databinding.FragmentTodoRegisterBinding
import com.example.teumteum.R
import com.example.teumteum.data.entities.Todo
import com.example.teumteum.data.local.AppDatabase

import androidx.lifecycle.lifecycleScope
import com.example.teumteum.ui.wish.WishRegisterFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.calendar.MonthlyCalendarFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodoRegisterFragment : BottomSheetDialogFragment(), IDateClickListener {

    private lateinit var binding: FragmentTodoRegisterBinding

    private var currentTargetTextView: TextView? = null
    private var popupWindow: PopupWindow? = null
    private val selectedItems = mutableSetOf("30분 전", "10분 전")
    private val alarmOptions = listOf("30분 전", "10분 전", "5분 전", "3분 전", "1분 전")

    private var isTodoSelected = true

    private var isCalendarVisible = false
    private var calendarFragmentStart: MonthlyCalendarFragment? = null
    private var calendarFragmentEnd: MonthlyCalendarFragment? = null
    private var isStartDateSelected = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            binding.timePickerEndLl.isVisible = false
            currentTargetTextView = binding.startTimeTv.takeIf { !isVisibleNow }
        }

        binding.endTimeTv.setOnClickListener {
            val isVisibleNow = binding.timePickerEndLl.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = false)
            }
            binding.timePickerEndLl.isVisible = !isVisibleNow
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

            val titleText = binding.todoTitleEt.text.toString().trim()

            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val todo = Todo(
                title = titleText,
                startTime = binding.startTimeTv.text.toString(),
                endTime = binding.endTimeTv.text.toString(),
                alarms = selectedItems.joinToString(","),
                isPublic = binding.categoryToggle03Iv.isChecked,
                isIncluded = binding.categoryToggle04Iv.isChecked
            )

            val db = AppDatabase.getInstance(requireContext())

            db?.let {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        it.todoDao().insert(todo)
                    }

                    Toast.makeText(requireContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }

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
                    child.setIncludeFontPadding(false)
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
        }
        binding.hourPicker02Np.apply {
            minValue = 1
            maxValue = 12
            wrapSelectorWheel = true
        }
        binding.minutePicker02Np.apply {
            minValue = 0
            maxValue = 5
            displayedValues = arrayOf("00", "10", "20", "30", "40", "50")
            wrapSelectorWheel = true
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
            binding.timePickerEndLl.isVisible = false
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

}