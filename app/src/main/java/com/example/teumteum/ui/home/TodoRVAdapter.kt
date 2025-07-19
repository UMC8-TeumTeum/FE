package com.example.teumteum.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.entities.TodoHomeItem
import com.example.teumteum.databinding.ItemTodolistBinding
import com.example.teumteum.ui.todo.TodoEditFragment

class TodoRVAdapter(private val fragmentManager: FragmentManager, private val todoList: List<TodoHomeItem>) : RecyclerView.Adapter<TodoRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTodolistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemTodolistBinding = ItemTodolistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = todoList[position]
        val binding = holder.binding
        binding.tvTodoTitle.text = item.title
        binding.tvStartTime.text = convertTo24HourFormat(item.startTime)
        binding.tvEndTime.text = convertTo24HourFormat(item.endTime)

        binding.ivLock.setImageResource(
            if (item.isPublic) R.drawable.ic_unlock_sv else R.drawable.ic_lock_sv
        )

        binding.root.setOnClickListener {
            val bottomSheet = TodoEditFragment.newInstanceWithTodoDummy(item)
            bottomSheet.show(fragmentManager, bottomSheet.tag)
        }

        if (item.isAlarmOn == null) {
            binding.ivAlarm.visibility = View.GONE
            binding.ivAlarm.setOnClickListener(null)
        } else {
            binding.ivAlarm.visibility = View.VISIBLE
            binding.ivAlarm.setImageResource(
                if (item.isAlarmOn == true) R.drawable.ic_alarm_on_sv
                else R.drawable.ic_alarm_off_sv
            )

            binding.ivAlarm.setOnClickListener {
                item.isAlarmOn = !(item.isAlarmOn ?: false)
                binding.ivAlarm.setImageResource(
                    if (item.isAlarmOn == true) R.drawable.ic_alarm_on_sv
                    else R.drawable.ic_alarm_off_sv
                )
            }
        }
    }

    override fun getItemCount(): Int = todoList.size

    private fun convertTo24HourFormat(time: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("a h:mm", java.util.Locale.KOREAN)
            val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.KOREAN)
            val date = inputFormat.parse(time)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            time // 변환 실패 시 원본 반환
        }
    }
}