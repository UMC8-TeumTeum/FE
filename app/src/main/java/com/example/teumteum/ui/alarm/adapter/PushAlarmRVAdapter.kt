package com.example.teumteum.ui.alarm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.remote.alarm.PushAlarmItem
import com.example.teumteum.databinding.ItemPushAlarmBinding

class PushAlarmRVAdapter(private val pushAlarmList: MutableList<PushAlarmItem>) : RecyclerView.Adapter<PushAlarmRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPushAlarmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPushAlarmBinding =
            ItemPushAlarmBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = pushAlarmList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = pushAlarmList[position]
        val binding = holder.binding
        binding.alarmTitleTv.text = item.title
        binding.alarmContentTv.text = item.content ?: ""
        binding.alarmTimeTv.text = item.timeAgo

    }

}