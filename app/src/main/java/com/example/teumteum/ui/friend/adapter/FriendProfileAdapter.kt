package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.databinding.ItemFriendProfileCardBinding

class FriendProfileAdapter(
    private val profiles: List<FriendProfileResult>,
    private val onSendClick: (FriendProfileResult) -> Unit
) : RecyclerView.Adapter<FriendProfileAdapter.FriendProfileViewHolder>() {

    inner class FriendProfileViewHolder(val binding: ItemFriendProfileCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: FriendProfileResult) {
            binding.nameTextView.text = profile.name

            Glide.with(binding.profileImageView.context)
                .load(profile.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileImageView)

            binding.sendButton.setOnClickListener {
                onSendClick(profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendProfileViewHolder {
        val binding = ItemFriendProfileCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size
}
