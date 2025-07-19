package com.example.teumteum.ui.todo

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.teumteum.R
import com.example.teumteum.data.local.TodoDao
import com.example.teumteum.databinding.FragmentTodoEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.teumteum.data.entities.TodoHomeItem
import com.example.teumteum.databinding.DialogConfirmTodoDeleteBinding
import com.example.teumteum.databinding.DialogConfirmTodoEditBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class TodoEditFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTodoEditBinding

    private var todoId: Int = -1
    private val selectedItems = mutableSetOf<String>()
    private val alarmOptions = listOf("30분 전", "10분 전", "5분 전", "3분 전", "1분 전")
    private var popupWindow: PopupWindow? = null

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

        setupPickers()

        binding.startTimeTv.setOnClickListener {
            val isVisibleNow = binding.timePickerStartLl.isVisible
            if (isVisibleNow) applySelectedTime(true)
            binding.timePickerStartLl.isVisible = !isVisibleNow
            binding.timePickerEndLl.isVisible = false
        }

        binding.endTimeTv.setOnClickListener {
            val isVisibleNow = binding.timePickerEndLl.isVisible
            if (isVisibleNow) applySelectedTime(false)
            binding.timePickerEndLl.isVisible = !isVisibleNow
            binding.timePickerStartLl.isVisible = false
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
                }
            }
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
        listOf(binding.minutePicker01Np, binding.minutePicker02Np).forEach {
            it.minValue = 0
            it.maxValue = 5
            it.displayedValues = arrayOf("00", "10", "20", "30", "40", "50")
            it.wrapSelectorWheel = true
        }
    }

    private fun applySelectedTime(isStart: Boolean) {
        val ampm = if (isStart) binding.ampmPicker01Np else binding.ampmPicker02Np
        val hour = if (isStart) binding.hourPicker01Np else binding.hourPicker02Np
        val minute = if (isStart) binding.minutePicker01Np else binding.minutePicker02Np

        val timeText = "${if (ampm.value == 0) "오전" else "오후"} ${hour.value}:${minute.displayedValues[minute.value]}"
        if (isStart) {
            binding.startTimeTv.text = timeText
            binding.timePickerStartLl.isVisible = false
        } else {
            binding.endTimeTv.text = timeText
            binding.timePickerEndLl.isVisible = false
        }
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


}
