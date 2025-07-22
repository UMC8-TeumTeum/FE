package com.example.teumteum.ui.alarm

import com.example.teumteum.databinding.ItemHomeAlarmBinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.entities.AlarmItem

class AlarmRVAdapter(private val fragmentManager: FragmentManager, private val alarmList: MutableList<AlarmItem>) : RecyclerView.Adapter<AlarmRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHomeAlarmBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemHomeAlarmBinding = ItemHomeAlarmBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = alarmList[position]
        val binding = holder.binding
        binding.alarmNameTv.text = item.personName
        binding.alarmTitleTv.text = item.title

    }

    override fun getItemCount(): Int = alarmList.size
}