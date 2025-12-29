package com.example.teumteum.ui.myhome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemScheduleBinding
import com.example.teumteum.ui.myhome.data.MyRoutine

class MyRoutineAdapter : ListAdapter<MyRoutine, MyRoutineAdapter.MyRoutineViewHolder>(DIFF_CALLBACK) {

    inner class MyRoutineViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyRoutine) {
            val startTime = item.startTime
            val endTime = item.endTime

            binding.timeStartTv.text = startTime.toString()
            binding.timeEndTv.text = endTime.toString()
            binding.titleTv.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRoutineViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyRoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyRoutineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MyRoutine>() {
            override fun areItemsTheSame(oldItem: MyRoutine, newItem: MyRoutine) = oldItem === newItem
            override fun areContentsTheSame(oldItem: MyRoutine, newItem: MyRoutine) = oldItem == newItem
        }
    }
}