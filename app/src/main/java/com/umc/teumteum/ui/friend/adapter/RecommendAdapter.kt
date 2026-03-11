package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumReceivedItem
import com.umc.teumteum.databinding.Friend01ItemRecommendCardBinding
import com.umc.teumteum.databinding.Friend01ItemRecommendCardReadBinding

class RecommendAdapter(
    private val onCardClick: (TeumReceivedItem, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var teumList: List<TeumReceivedItem> = emptyList()

    fun setTeumList(list: List<TeumReceivedItem>) {
        teumList = list
        notifyDataSetChanged()
    }

    //읽지 않은 요청
    inner class RecommendViewHolder(val binding: Friend01ItemRecommendCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumReceivedItem, position: Int) {
            val nickname = item.senderUser.nickname
            val receiverCount = item.receiverCount

            val displayName = if (item.resend) {
                "나 > $nickname"   // 재요청이면 "나 > 상대방 이름"
            } else {
                if (receiverCount <= 1) nickname else "$nickname 외 ${receiverCount - 1}명"
            }
            binding.tvName.text = displayName


            //  재요청이면 고정 텍스트
            binding.tvDesc.text = if (item.resend) {
                "시간 제안이 도착했어요"
            } else {
                item.title
            }

            Glide.with(binding.root.context)
                .load(item.senderUser.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .fallback(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)

            binding.root.setOnClickListener {
                onCardClick(item, position)
            }
        }
    }

    //읽은 요청
    inner class RecommendReadViewHolder(val binding: Friend01ItemRecommendCardReadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumReceivedItem, position: Int) {
            val nickname = item.senderUser.nickname
            val receiverCount = item.receiverCount

            val displayName = if (item.resend) {
                "나 > $nickname"   // 재요청이면 "나 > 상대방 이름"
            } else {
                if (receiverCount <= 1) nickname else "$nickname 외 ${receiverCount - 1}명"
            }
            binding.tvName.text = displayName


            //  재요청이면 고정 텍스트
            binding.tvDesc.text = if (item.resend) {
                "시간 제안이 도착했어요"
            } else {
                item.title
            }

            Glide.with(binding.root.context)
                .load(item.senderUser.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .fallback(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)

            binding.root.setOnClickListener {
                onCardClick(item, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_READ -> {
                val binding = Friend01ItemRecommendCardReadBinding.inflate(inflater, parent, false)
                RecommendReadViewHolder(binding)
            }
            else -> {
                val binding = Friend01ItemRecommendCardBinding.inflate(inflater, parent, false)
                RecommendViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = teumList[position]
        when (holder) {
            is RecommendReadViewHolder -> holder.bind(item, position)
            is RecommendViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (teumList[position].read) VIEW_TYPE_READ else VIEW_TYPE_UNREAD
    }

    companion object {
        private const val VIEW_TYPE_READ = 0
        private const val VIEW_TYPE_UNREAD = 1
    }

    override fun getItemCount(): Int = teumList.size
}

