package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemFriendCheckboxBinding

class FriendAddAdapter(private val friends: List<Friend>) :
    RecyclerView.Adapter<FriendAddAdapter.FriendAddViewHolder>() {

    inner class FriendAddViewHolder(private val binding: ItemFriendCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.nameTextView.text = friend.name
            // 이미지는 필요에 따라 설정
            binding.profile1.setImageResource(friend.imageResId)
            // 체크박스 상태 유지하거나 이벤트 처리하려면 여기서 추가
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendAddViewHolder {
        val binding = ItemFriendCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendAddViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendAddViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int = friends.size
}

