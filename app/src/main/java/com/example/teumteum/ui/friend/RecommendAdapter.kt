package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.TeumReceivedItem
import com.example.teumteum.databinding.Friend01ItemRecommendCardBinding

class RecommendAdapter(
    private val onCardClick: (TeumReceivedItem) -> Unit
) : RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>() {

    private var teumList: List<TeumReceivedItem> = emptyList()

    fun setTeumList(list: List<TeumReceivedItem>) {
        teumList = list
        notifyDataSetChanged()
    }

    inner class RecommendViewHolder(val binding: Friend01ItemRecommendCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumReceivedItem) {
            // 이름과 설명 바인딩
            binding.tvName.text = item.senderUser.nickname
            binding.tvDesc.text = item.title

            // Glide로 프로필 이미지 바인딩
            Glide.with(binding.root.context)
                .load(item.senderUser.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .fallback(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)

            // 카드 클릭 이벤트
            binding.root.setOnClickListener {
                onCardClick(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = Friend01ItemRecommendCardBinding.inflate(inflater, parent, false)
        return RecommendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        holder.bind(teumList[position])
    }

    override fun getItemCount(): Int = teumList.size
}
