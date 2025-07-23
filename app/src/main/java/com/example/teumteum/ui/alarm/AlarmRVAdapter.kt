package com.example.teumteum.ui.alarm

import com.example.teumteum.databinding.ItemHomeAlarmBinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.entities.AlarmItem

class AlarmRVAdapter(
    private val fragmentManager: FragmentManager,
    private val alarmList: MutableList<AlarmItem>
) : RecyclerView.Adapter<AlarmRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHomeAlarmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemHomeAlarmBinding =
            ItemHomeAlarmBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = alarmList[position]
        val binding = holder.binding
        binding.alarmNameTv.text = item.personName
        binding.alarmTitleTv.text = item.title
        binding.alarmTimeTv.text = item.elapsedTime

        binding.root.setOnClickListener {
            item.isRead = true

            val grayColor = ContextCompat.getColor(binding.root.context, R.color.teumteum_gray)

            binding.alarmNameTv.setTextColor(grayColor)
            binding.alarmTitleTv.setTextColor(grayColor)
            binding.alarmTimeTv.setTextColor(grayColor)

            binding.profileIv.colorFilter = null
            binding.profileIv.imageAlpha = 100
        }
    }

override fun getItemCount(): Int = alarmList.size
}