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
            // 이미지는 필요에 따라 설정
            binding.profile1.setImageResource(friend.imageResId)

            // 체크박스 상태에 따라 UI 변경
            val drawableRes = if (friend.isChecked) {
                R.drawable.check_box // 체크됨 상태 (SVG 아이콘)
            } else {
                R.drawable.friend_roommate_checkbox_unchecked // 체크 안 됨
            }
            binding.checkBox.buttonDrawable = ContextCompat.getDrawable(binding.root.context, drawableRes)

            // 클릭 리스너: 체크 상태 변경
            binding.checkBox.setOnClickListener {
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

