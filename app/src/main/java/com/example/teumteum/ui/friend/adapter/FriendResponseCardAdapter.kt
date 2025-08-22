package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.Friend02ItemRequestTeumCardBinding

class FriendResponseCardAdapter(
    private val responseList: List<TeumReceivedItem>
) : RecyclerView.Adapter<FriendResponseCardAdapter.ResponseViewHolder>() {

    private var imageList = listOf(
        R.drawable.friend_teum_logo,
        R.drawable.teumi_teuma_eat,
        R.drawable.teumi_teuma_ball,
        R.drawable.teumi_teuma_down,
        R.drawable.teumi_teuma_juice
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResponseViewHolder {
        val binding = Friend02ItemRequestTeumCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResponseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        holder.bind(responseList[position])
    }

    override fun getItemCount(): Int = responseList.size

    inner class ResponseViewHolder(
        private val binding: Friend02ItemRequestTeumCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumReceivedItem) {
            binding.apply {
                // 이름: "나 > 상대방 이름"
                tvName.text = "나 > ${item.senderUser.nickname}"

                // 내가 보낸 날짜/시간
                tvDate.text = "${formatDate(item.originalDate)}     |"
                tvTime.text = "${item.originalTimeSlot?.start} ~ ${item.originalTimeSlot?.end}"

                // 제목 & 설명
                tvTitle.text = item.title
                tvDesc.text = item.description

                // 상대가 재요청한 시간
                tvSuggestionTime.text = "${item.timeSlot.start} ~ ${item.timeSlot.end}"

                // 프로필 이미지
                Glide.with(root.context)
                    .load(item.senderUser.profileImageUrl)
                    .placeholder(R.drawable.gray_teum)
                    .circleCrop()
                    .into(imgProfile)

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
    }
}
