package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.data.remote.friend.model.TeumRequestDateResult
import com.example.teumteum.data.remote.friend.model.UserMiniDto
import com.example.teumteum.databinding.ItemRequestHistoryBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.imageview.ShapeableImageView

class TeumRequestAdapter(
    private var itemList: List<TeumRequestDateResult> = emptyList()
) : RecyclerView.Adapter<TeumRequestAdapter.TeumRequestViewHolder>() {

    inner class TeumRequestViewHolder(val binding: ItemRequestHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: TeumRequestDateResult, position: Int, fullList: List<TeumRequestDateResult>) {
            // 취소 여부
            binding.tvStatus.visibility = if (data.isCancelled) View.VISIBLE else View.GONE

            // 기본 숨김
            binding.originalDivider.visibility = View.GONE

            // 조건: 현재 아이템이 원본 요청이고, 다음 아이템이 재요청일 경우
            val isOriginal = data.isResend == false
            val nextIsResend = if (position + 1 < fullList.size) fullList[position + 1].isResend == true else false

            if (isOriginal && nextIsResend) {
                binding.originalDivider.visibility = View.VISIBLE
            }

            // 프로필 이미지
            Glide.with(binding.profileIv.context)
                .load(data.requester.profileImageUrl)
                .placeholder(com.example.teumteum.R.drawable.gray_teum)
                .error(com.example.teumteum.R.drawable.gray_teum)
                .fallback(com.example.teumteum.R.drawable.gray_teum)
                .into(binding.profileIv)

            binding.tvName.text = data.requester.nickname ?: "이름없음"

            // 날짜/시간
            val dateFormatted = data.date.replace("-", ".").substring(2) // "25.09.02"
            binding.tvDate.text = "$dateFormatted     |"
            binding.tvTime.text = "${data.timeSlot.start} ~ ${data.timeSlot.end}"

            // 제목 / 설명
            binding.title.text = data.title
            binding.description.text = data.description

            // 상태별 FlexboxLayout
            setUserList(binding.containerAccepted, data.accepted)
            setUserList(binding.containerCancelled, data.cancelled)
            setUserList(binding.containerPending, data.pending)
        }

        private fun setUserList(container: FlexboxLayout, users: List<UserMiniDto>) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(container.context)
            for (user in users) {
                val imageView = inflater.inflate(
                    com.example.teumteum.R.layout.item_user_circle,
                    container,
                    false
                ) as ShapeableImageView
                Glide.with(imageView.context)
                    .load(user.profileImageUrl)
                    .placeholder(com.example.teumteum.R.drawable.gray_teum)
                    .error(com.example.teumteum.R.drawable.gray_teum)
                    .fallback(com.example.teumteum.R.drawable.gray_teum)
                    .into(imageView)

                container.addView(imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeumRequestViewHolder {
        val binding = ItemRequestHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TeumRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeumRequestViewHolder, position: Int) {
        holder.bind(itemList[position], position, itemList)
    }

    override fun getItemCount(): Int = itemList.size

    fun submitList(newList: List<TeumRequestDateResult>) {
        itemList = newList
        notifyDataSetChanged()
    }
}
