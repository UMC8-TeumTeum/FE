package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumReceivedItem
import com.umc.teumteum.databinding.Friend02ItemTeumCardBinding

class FriendRequestCardAdapter(private val teumList: List<TeumReceivedItem>) :
    RecyclerView.Adapter<FriendRequestCardAdapter.TeumViewHolder>() {

    private var imageList = listOf(
        R.drawable.friend_teum_logo,
        R.drawable.teumi_teuma_eat,
        R.drawable.teumi_teuma_ball,
        R.drawable.teumi_teuma_down,
        R.drawable.teumi_teuma_juice
    )

    inner class TeumViewHolder(private val binding: Friend02ItemTeumCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumReceivedItem) {
            val nickname = item.senderUser.nickname
            val receiverCount = item.receiverCount

            // 표시 이름 로직
            val displayName = if (receiverCount <= 1) {
                nickname
            } else {
                "$nickname 외 ${receiverCount - 1}명"
            }
            binding.tvName.text = displayName

//            binding.tvName.text = item.senderUser.nickname
            binding.tvDate.text = "${formatDate(item.date)}     |"
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

            binding.imgTeum.setImageResource(imageList[item.graphicId])
        }
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val parsed = java.time.LocalDate.parse(date) // "2025-08-20"
            parsed.format(java.time.format.DateTimeFormatter.ofPattern("yy.MM.dd")) // "25.08.20"
        } catch (e: Exception) {
            date // 실패하면 원본 그대로
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
