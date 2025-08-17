package com.example.teumteum.ui.todo.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.data.remote.todo.model.enums.AlarmStatus
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.example.teumteum.databinding.ItemTodolistBinding
import com.example.teumteum.ui.todo.TodoEditFragment
import com.example.teumteum.ui.wish.WishSetting01Fragment

class TodoRVAdapter(
    private val fragmentManager: FragmentManager,
    private var todoList: List<TodoListResult>,
    private val onToggleAlarm: (id: Long, toActive: Boolean) -> Unit,
) : RecyclerView.Adapter<TodoRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTodolistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemTodolistBinding =
            ItemTodolistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = todoList[position]
        val b = holder.binding
        b.tvTodoTitle.text = item.title
        b.tvStartTime.text = convertTo24HourFormat(item.startTime)
        b.tvEndTime.text = convertTo24HourFormat(item.endTime)

        b.ivLock.setImageResource(
            if (item.isPublic) R.drawable.ic_unlock_sv else R.drawable.ic_lock_sv
        )

        b.root.setCardBackgroundColor(
            ContextCompat.getColor(
                b.root.context,
                if (item.type == ScheduleType.ROUTINE) R.color.main_2 else R.color.white
            )
        )

        b.root.setOnClickListener {
            val args = Bundle().apply {
                putLong("todo_id", item.id)
                putString("schedule_type", item.type.toString())
            }
            TodoEditFragment().apply { arguments = args }
                .show(fragmentManager, "TodoEditBottomSheet")
        }

        if (item.alarmStatus == AlarmStatus.NONE) {
            b.ivAlarm.visibility = View.GONE
            b.ivAlarm.setOnClickListener(null)
        } else {
            b.ivAlarm.visibility = View.VISIBLE
            b.ivAlarm.setImageResource(
                if (item.alarmStatus == AlarmStatus.ACTIVE) R.drawable.ic_alarm_on_sv
                else R.drawable.ic_alarm_off_sv
            )

            b.ivAlarm.setOnClickListener {
                // 토글 후 상태
                val toActive = (item.alarmStatus != AlarmStatus.ACTIVE)
                item.alarmStatus = if (toActive) AlarmStatus.ACTIVE else AlarmStatus.INACTIVE

                b.ivAlarm.setImageResource(
                    if (toActive) R.drawable.ic_alarm_on_sv else R.drawable.ic_alarm_off_sv
                )

                // 해당 투두의 모든 알림 활성화/비활성화
                onToggleAlarm(item.id, toActive)
            }
        }
    }

    override fun getItemCount(): Int = todoList.size

    private fun convertTo24HourFormat(time: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.KOREAN)
            val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.KOREAN)
            val date = inputFormat.parse(time)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            time // 변환 실패 시 원본 반환
        }
    }

    fun updateList(newList: List<TodoListResult>) {
        todoList = newList
        notifyDataSetChanged()
    }
}