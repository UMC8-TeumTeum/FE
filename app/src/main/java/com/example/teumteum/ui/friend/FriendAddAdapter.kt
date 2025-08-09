package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.databinding.ItemFriendCheckboxBinding

class FriendAddAdapter(private val friends: List<Friend>) :
    RecyclerView.Adapter<FriendAddAdapter.FriendAddViewHolder>() {

    inner class FriendAddViewHolder(private val binding: ItemFriendCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.nameTextView.text = friend.name
            binding.profile1.setImageResource(friend.imageResId)

            // 체크 상태에 맞춰 이미지 변경
            val imageRes = if (friend.isChecked) {
                R.drawable.check_box // 체크됨
            } else {
                R.drawable.uncheck_box // 체크 안됨
            }
            binding.checkBoxBtn.setImageResource(imageRes)

            // 클릭 시 상태 토글
            binding.checkBoxBtn.setOnClickListener {
                friend.isChecked = !friend.isChecked
                notifyItemChanged(adapterPosition)
            }
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


