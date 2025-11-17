package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemFriendConflictCardBinding

class ConflictPagerAdapter(private val items: List<ConflictItem>) :
    RecyclerView.Adapter<ConflictPagerAdapter.ConflictViewHolder>() {

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

    // --- ViewHolder ---
    // (ViewHolder는 Adapter 내부에 inner class로 두는 것이 일반적입니다)
    inner class ConflictViewHolder(private val itemBinding: ItemFriendConflictCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: ConflictItem) {
            // 새 XML ID에 맞게 바인딩
            itemBinding.title.text = item.title
            itemBinding.description.text = item.description
            itemBinding.tvName.text = item.userName
            itemBinding.tvTime.text = item.time

            // TODO: 프로필 이미지 로드 (예: Glide, Coil)
            // Glide.with(itemView.context).load(item.profileUrl).into(itemBinding.profileIv)
        }
    }
}