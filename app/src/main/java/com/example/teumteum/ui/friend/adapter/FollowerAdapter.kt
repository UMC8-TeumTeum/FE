package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.databinding.Friend01ItemFollowerBinding
import com.example.teumteum.ui.friend.data.FollowerData

class FollowerAdapter(private val followerList: List<FollowerData>) :
    RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder>() {

    inner class FollowerViewHolder(val binding: Friend01ItemFollowerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FollowerData) {
            binding.nameTv.text = item.name
            binding.jobTv.text = " · ${item.job}"
            binding.sendBtn.setOnClickListener {
                // 비행기 버튼 동작
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val binding = Friend01ItemFollowerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        holder.bind(followerList[position])
    }

    override fun getItemCount(): Int = followerList.size

}


