package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.TeumReceivedItem
import com.example.teumteum.databinding.Friend02ItemTeumCardBinding

class TeumReceivedAdapter(private val teumList: List<TeumReceivedItem>) :
    RecyclerView.Adapter<TeumReceivedAdapter.TeumReceivedViewHolder>() {

    inner class TeumReceivedViewHolder(val binding: Friend02ItemTeumCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeumReceivedItem) {
            // 상단 정보
            val receiverText = if (item.receiverCount > 1) {
                "${item.senderUser.nickname} 외 ${item.receiverCount - 1}명"
            } else {
                item.senderUser.nickname
            }
            binding.tvName.text = receiverText
            binding.tvDate.text = item.date.replace("-", ".") + "     |"
            binding.tvTime.text = "${item.timeSlot.start} ~ ${item.timeSlot.end}"

            // 하단 정보
            binding.tvTitle.text = item.title
            binding.tvDesc.text = item.description

            Glide.with(binding.imgProfile.context)
                .load(item.senderUser.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .into(binding.imgProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeumReceivedViewHolder {
        val binding = Friend02ItemTeumCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TeumReceivedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeumReceivedViewHolder, position: Int) {
        holder.bind(teumList[position])
    }

    override fun getItemCount(): Int = teumList.size
}
