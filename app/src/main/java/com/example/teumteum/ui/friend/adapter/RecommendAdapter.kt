package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.Friend01ItemRecommendCardBinding

class RecommendAdapter(
    private val onCardClick: (TeumReceivedItem, Int) -> Unit  // ✅ position 추가
) : RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>() {

    private var teumList: List<TeumReceivedItem> = emptyList()

    fun setTeumList(list: List<TeumReceivedItem>) {
        teumList = list
        notifyDataSetChanged()
    }

    inner class RecommendViewHolder(val binding: Friend01ItemRecommendCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // ✅ position도 같이 받도록 수정
        fun bind(item: TeumReceivedItem, position: Int) {
            val nickname = item.senderUser.nickname
            val receiverCount = item.receiverCount

            val displayName = if (receiverCount <= 1) {
                nickname
            } else {
                "$nickname 외 ${receiverCount - 1}명"
            }

            binding.tvName.text = displayName
            binding.tvDesc.text = item.title

            Glide.with(binding.root.context)
                .load(item.senderUser.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .fallback(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)

            // ✅ position 함께 넘기기
            binding.root.setOnClickListener {
                onCardClick(item, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = Friend01ItemRecommendCardBinding.inflate(inflater, parent, false)
        return RecommendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        holder.bind(teumList[position], position)  // ✅ position 전달
    }

    override fun getItemCount(): Int = teumList.size
}

