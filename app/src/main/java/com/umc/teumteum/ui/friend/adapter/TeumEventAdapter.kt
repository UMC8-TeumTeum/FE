package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.teumteum.data.remote.friend.model.TeumScheduledResult
import com.umc.teumteum.databinding.ItemTeumEventCardBinding

class TeumEventAdapter(
    private var events: List<TeumScheduledResult>,
    private val onItemClick: (teumId: Int) -> Unit  //  클릭 리스너 추가
) : RecyclerView.Adapter<TeumEventAdapter.TeumViewHolder>() {

    inner class TeumViewHolder(val binding: ItemTeumEventCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumScheduledResult) {
            val time = item.time.firstOrNull()

            binding.tvStartTime.text = time?.start ?: "-"
            binding.tvEndTime.text = time?.end ?: "-"
            binding.tvEventDesc.text = item.title

            //  클릭 이벤트 설정
            binding.root.setOnClickListener {
                onItemClick(item.teumId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTeumEventCardBinding.inflate(inflater, parent, false)
        return TeumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeumViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateData(newList: List<TeumScheduledResult>) {
        events = newList
        notifyDataSetChanged()
    }
}
