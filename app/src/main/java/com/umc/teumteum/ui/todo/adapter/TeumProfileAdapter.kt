package com.umc.teumteum.ui.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.databinding.ItemProfileImageBinding

class TeumProfileAdapter :
    ListAdapter<String, TeumProfileAdapter.ProfileVH>(diff) {

    companion object {
        private val diff = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }

    inner class ProfileVH(private val binding: ItemProfileImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            Glide.with(binding.profileIv)
                .load(url)
                .placeholder(R.drawable.gray_teum) // 로딩 중
                .error(R.drawable.gray_teum)       // 실패 시
                .centerCrop()
                .into(binding.profileIv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProfileImageBinding.inflate(inflater, parent, false)
        return ProfileVH(binding)
    }

    override fun onBindViewHolder(holder: ProfileVH, position: Int) {
        holder.bind(getItem(position))
    }
}