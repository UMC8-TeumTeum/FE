package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.SharedTeumItem
import com.umc.teumteum.databinding.ItemSharedTeumLeftBinding
import com.umc.teumteum.databinding.ItemSharedTeumRightBinding

class SharedTeumAdapter(
) : ListAdapter<SharedTeumItem, RecyclerView.ViewHolder>(diff) {

    companion object {
        private const val TYPE_LEFT = 0
        private const val TYPE_RIGHT = 1

        val diff = object : DiffUtil.ItemCallback<SharedTeumItem>() {
            override fun areItemsTheSame(old: SharedTeumItem, new: SharedTeumItem): Boolean {
                // 서버에 ID가 없다면 조합키로 식별
                val oldKey = "${old.date}|${old.time.start}-${old.time.end}|${old.sender.userId}|${old.title}"
                val newKey = "${new.date}|${new.time.start}-${new.time.end}|${new.sender.userId}|${new.title}"
                return oldKey == newKey
            }
            override fun areContentsTheSame(old: SharedTeumItem, new: SharedTeumItem) = old == new
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.isSender) TYPE_RIGHT else TYPE_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_RIGHT) {
            RightVH(ItemSharedTeumRightBinding.inflate(inf, parent, false))
        } else {
            LeftVH(ItemSharedTeumLeftBinding.inflate(inf, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is LeftVH -> holder.bind(item)
            is RightVH -> holder.bind(item)
        }
    }

    inner class LeftVH(private val b: ItemSharedTeumLeftBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: SharedTeumItem) {
            b.nameTv.text = item.sender.nickname ?: "알 수 없음"
            b.dateTv.text = item.date
            b.timeRangeTv.text = "${item.time.start} ~ ${item.time.end}"
            b.messageTitleTv.text = item.title
            b.separatorTv.text = "|"
            b.messageContentTv.text = item.description

            Glide.with(b.profileIv)
                .load(item.sender.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(b.profileIv)
        }
    }

    inner class RightVH(private val b: ItemSharedTeumRightBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: SharedTeumItem) {
            b.nameTv.text = item.sender.nickname ?: "알 수 없음"
            b.dateTv.text = item.date
            b.timeRangeTv.text = "${item.time.start} ~ ${item.time.end}"
            b.messageTitleTv.text = item.title
            b.separatorTv.text = "|"
            b.messageContentTv.text = item.description

            Glide.with(b.profileIv)
                .load(item.sender.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(b.profileIv)
        }
    }
}
