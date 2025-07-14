package com.example.teumteum.ui.edit

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
import com.example.teumteum.R
import com.example.teumteum.data.entities.Todo
import com.example.teumteum.data.local.AppDatabase
import com.example.teumteum.data.local.TodoDao
import com.example.teumteum.databinding.FragmentTodoEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import androidx.lifecycle.lifecycleScope
import com.example.teumteum.databinding.DialogConfirmTodoDeleteBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoEditFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTodoEditBinding

    private lateinit var db: AppDatabase
    private lateinit var todoDao: TodoDao

    private var todoId: Int = -1
    private val selectedItems = mutableSetOf<String>()
    private val alarmOptions = listOf("30분 전", "10분 전", "5분 전", "3분 전", "1분 전")

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

        db = AppDatabase.getInstance(requireContext())!!
        todoDao = db.todoDao()

        lifecycleScope.launch {
            val todo = withContext(Dispatchers.IO) {
                todoDao.getTodoById(todoId)
            }
            bindTodoToUI(todo)
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

            val updatedTodo = Todo(
                title = titleText,
                startTime = binding.startTimeTv.text.toString(),
                endTime = binding.endTimeTv.text.toString(),
                alarms = selectedItems.joinToString(","),
                isPublic = binding.categoryToggle03Iv.isChecked,
                isIncluded = binding.categoryToggle04Iv.isChecked
            ).apply { id = todoId }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    todoDao.updateTodo(updatedTodo)
                }
                Toast.makeText(requireContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        binding.btnTodoDelete.setOnClickListener {
            deleteTodo(todoId)
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

    private fun bindTodoToUI(todo: Todo) {
        binding.todoTitleEt.setText(todo.title)
        binding.startTimeTv.text = todo.startTime
        binding.endTimeTv.text = todo.endTime
        binding.categoryToggle03Iv.isChecked = todo.isPublic
        binding.categoryToggle04Iv.isChecked = todo.isIncluded

        // 알람 설정
        val alarms = todo.alarms.split(",").map { it.trim() }
        selectedItems.clear()
        selectedItems.addAll(alarms)

        // UI 초기화
        binding.alarmLayoutContainer.removeAllViews()
        binding.alarmItem01Ll.visibility = View.GONE
        binding.alarmItem02Ll.visibility = View.GONE

        alarms.forEach { label ->
            addAlarmItem(label)
        }
    }

    private fun addAlarmItem(label: String) {
        when (label) {
            "30분 전" -> {
                // 설정된 알람일 때만 보이게
                if (selectedItems.contains("30분 전")) {
                    binding.alarmItem01Ll.visibility = View.VISIBLE
                }
            }
            "10분 전" -> {
                if (selectedItems.contains("10분 전")) {
                    binding.alarmItem02Ll.visibility = View.VISIBLE
                }
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
            "30분 전" -> binding.alarmItem01Ll.visibility = View.GONE
            "10분 전" -> binding.alarmItem02Ll.visibility = View.GONE
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

    private fun showAlarmPopupWindow(anchor: View) {
        val popupView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_dropdown)
            setPadding(4, 4, 4, 4)
            elevation = 16f
        }

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
        val popupWindow = PopupWindow(
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

    companion object {
        fun newInstance(todoId: Int): TodoEditFragment {
            return TodoEditFragment().apply {
                arguments = Bundle().apply {
                    putInt("todo_id", todoId)
                }
            }
        }
    }

    private fun deleteTodo(todoId: Int) {
        val dialogBinding = DialogConfirmTodoDeleteBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.todoConfirmTv.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(requireContext())
                    db?.todoDao()?.deleteTodoById(todoId)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    dismiss()  // BottomSheetDialogFragment 닫기
                }
            }
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
