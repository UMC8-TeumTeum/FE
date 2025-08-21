package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.ItemAddedFriendBinding
import com.example.teumteum.ui.friend.data.AddedFriend

class AddedFriendAdapter(
    private var items: List<AddedFriend> = emptyList()
) : RecyclerView.Adapter<AddedFriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(private val binding: ItemAddedFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AddedFriend) {
            binding.nameTv.text = item.name ?: "이름 없음"

            Glide.with(binding.root)
                .load(item.profileImage)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemAddedFriendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<AddedFriend>) {
        items = newList
        notifyDataSetChanged()
    }
}