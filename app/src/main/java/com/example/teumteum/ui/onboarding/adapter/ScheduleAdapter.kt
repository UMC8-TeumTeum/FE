package com.example.teumteum.ui.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemScheduleBinding
import com.example.teumteum.ui.onboarding.data.Schedule

class ScheduleAdapter : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(DIFF_CALLBACK) {

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Schedule) {
            val startTime = item.startTime
            val endTime = item.endTime

            binding.timeStartTv.text = startTime.toString()
            binding.timeEndTv.text = endTime.toString()
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

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem === newItem
            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem == newItem
        }
    }
}
