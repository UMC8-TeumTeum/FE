package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.ItemFriendConflictCardBinding
import com.example.teumteum.data.remote.friend.model.TeumConflictItem
import com.google.android.material.imageview.ShapeableImageView

class ConflictPagerAdapter(
    private val items: List<TeumConflictItem>
) : RecyclerView.Adapter<ConflictPagerAdapter.ConflictViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConflictViewHolder {
        val binding = ItemFriendConflictCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConflictViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConflictViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ConflictViewHolder(
        private val binding: ItemFriendConflictCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeumConflictItem) {
            binding.title.text = item.title
            binding.description.text = item.description
            binding.tvName.text = "${item.receiverNickname}에게"
            binding.tvTime.text = "${item.startTime} ~ ${item.endTime}"

            Glide.with(binding.root.context)
                .load(item.receiverProfileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)
        }
    }
}
