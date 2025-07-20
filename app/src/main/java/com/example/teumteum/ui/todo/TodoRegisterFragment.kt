package com.example.teumteum.ui.todo

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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

class TodoRegisterFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTodoRegisterBinding

    private var currentTargetTextView: TextView? = null
    private var popupWindow: PopupWindow? = null
    private val selectedItems = mutableSetOf("30분 전", "10분 전")
    private val alarmOptions = listOf("30분 전", "10분 전", "5분 전", "3분 전", "1분 전")

    private var isTodoSelected = true

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
            val isVisibleNow = binding.timePickerStartLl.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = true)
            }
            binding.timePickerStartLl.isVisible = !isVisibleNow
            binding.timePickerEndLl.isVisible = false
            currentTargetTextView = binding.startTimeTv.takeIf { !isVisibleNow }
        }

        binding.endTimeTv.setOnClickListener {
            val isVisibleNow = binding.timePickerEndLl.isVisible
            if (isVisibleNow) {
                applySelectedTime(isStart = false)
            }
            binding.timePickerEndLl.isVisible = !isVisibleNow
            binding.timePickerStartLl.isVisible = false
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
                binding.btnTodo.setImageResource(R.drawable.btn_todo_deactive_sv)
                binding.btnWish.setImageResource(R.drawable.btn_wish_active_sv)
                isTodoSelected = false

                childFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, WishRegisterFragment())
                    .commit()
            }
        }
    }

    private fun setupPickers() {
        binding.ampmPicker01Np.apply {
            minValue = 0
            maxValue = 1
            displayedValues = arrayOf("오전", "오후")
        }
        binding.hourPicker01Np.apply {
            minValue = 1
            maxValue = 12
            wrapSelectorWheel = true
        }
        binding.minutePicker01Np.apply {
            minValue = 0
            maxValue = 5
            displayedValues = arrayOf("00", "10", "20", "30", "40", "50")
            wrapSelectorWheel = true
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
            binding.timePickerStartLl.isVisible = false
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

            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val x = location[0]
            val y = location[1] + anchor.height

            showAtLocation(anchor.rootView, Gravity.TOP or Gravity.START, x + anchor.width - popupWidth, y + 16)
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

}