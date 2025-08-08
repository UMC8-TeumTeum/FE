package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FollowingResult
import com.example.teumteum.databinding.Friend01ItemFollowingBinding

class FollowingAdapter(
    private var data: List<FollowingResult>,
    private val onProfileClick: (FollowingResult) -> Unit,
    private val onSendClick: (FollowingResult) -> Unit
) : RecyclerView.Adapter<FollowingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: Friend01ItemFollowingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FollowingResult) {
            binding.nameTv.text = item.nickname
            binding.jobTv.text = item.job

            Glide.with(binding.profileIv)
                .load(item.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)

            binding.profileIv.setOnClickListener { onProfileClick(item) }
            binding.sendBtn.setOnClickListener { onSendClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Friend01ItemFollowingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun updateData(newData: List<FollowingResult>) {
        data = newData
        notifyDataSetChanged()
    }
}

