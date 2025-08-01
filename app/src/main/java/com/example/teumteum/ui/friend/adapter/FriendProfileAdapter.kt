package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemFriendProfileCardBinding
import com.example.teumteum.ui.friend.data.FriendProfileData

class FriendProfileAdapter(
    private val profiles: List<FriendProfileData>,
    private val onSendClick: (FriendProfileData) -> Unit
) : RecyclerView.Adapter<FriendProfileAdapter.FriendProfileViewHolder>() {

    inner class FriendProfileViewHolder(val binding: ItemFriendProfileCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: FriendProfileData) {
            binding.nameTextView.text = profile.name
            binding.profileImageView.setImageResource(profile.imageRes)
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
