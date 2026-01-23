package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.FollowerResult
import com.umc.teumteum.databinding.Friend01ItemFollowerBinding

class FollowerAdapter(
    private var data: List<FollowerResult>,
    private val onProfileClick: ((FollowerResult) -> Unit)? = null,
    private val onSendClick: ((FollowerResult) -> Unit)? = null
) : RecyclerView.Adapter<FollowerAdapter.VH>() {

    fun updateData(newData: List<FollowerResult>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class VH(val binding: Friend01ItemFollowerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FollowerResult) = with(binding) {
            // 이름
            nameTv.text = item.nickname

            // 직업(없으면 GONE)
            val job = item.job.trim()
            if (job.isNotEmpty()) {
                jobTv.visibility = View.VISIBLE
                jobTv.text = " · $job"
            } else {
                jobTv.visibility = View.GONE
            }

            // 프로필 이미지
            Glide.with(root)
                .load(item.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(profileIv)

            // 클릭 리스너
            profileIv.setOnClickListener { onProfileClick?.invoke(item) }
            sendBtn.setOnClickListener { onSendClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = Friend01ItemFollowerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
