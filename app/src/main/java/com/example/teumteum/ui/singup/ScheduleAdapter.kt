package com.example.teumteum.ui.singup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.Schedule
import com.example.teumteum.databinding.ItemScheduleBinding

class ScheduleAdapter : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(DIFF_CALLBACK) {

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Schedule) {
            val startTime = formatTime(item.startTime)
            val endTime = formatTime(item.endTime)
            binding.timeStartTv.text = startTime
            binding.timeEndTv.text = endTime
            binding.titleTv.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatTime(time: String): String {
        val cleaned = time.replace("오전 ", "").replace("오후 ", "")
        val parts = cleaned.split(":")
        if (parts.size != 2) return cleaned // 예외 처리: 형식이 이상하면 원본 반환

        val hour = parts[0].toIntOrNull() ?: return cleaned
        val minute = parts[1].toIntOrNull() ?: return cleaned

        return String.format("%02d:%02d", hour, minute)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem === newItem
            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem == newItem
        }
    }
}
