package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.TeumReceivedItem
import com.example.teumteum.databinding.Friend02ItemTeumCardBinding

class FriendRequestCardAdapter(private val teumList: List<TeumReceivedItem>) :
    RecyclerView.Adapter<FriendRequestCardAdapter.TeumViewHolder>() {

    inner class TeumViewHolder(private val binding: Friend02ItemTeumCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumReceivedItem) {
            binding.tvName.text = item.senderUser.nickname
            binding.tvDate.text = item.date
            binding.tvTime.text = "${item.timeSlot.start} ~ ${item.timeSlot.end}"
            binding.tvTitle.text = item.title
            binding.tvDesc.text = item.description
            //  Glide로 프로필 이미지 불러오기
            Glide.with(binding.root)
                .load(item.senderUser.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .fallback(R.drawable.gray_teum)
                .into(binding.imgProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeumViewHolder {
        val binding = Friend02ItemTeumCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TeumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeumViewHolder, position: Int) {
        holder.bind(teumList[position])
    }

    override fun getItemCount(): Int = teumList.size
}
