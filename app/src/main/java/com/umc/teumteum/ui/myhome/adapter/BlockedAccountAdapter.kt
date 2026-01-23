package com.umc.teumteum.ui.myhome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.ui.myhome.data.BlockedAccount
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class BlockedAccountAdapter(
    private val onUnblockClick: (BlockedAccount) -> Unit
) : ListAdapter<BlockedAccount, BlockedAccountAdapter.BlockedViewHolder>(diffUtil) {

    inner class BlockedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profile: ShapeableImageView = view.findViewById(R.id.profileLayout)
        val name: TextView = view.findViewById(R.id.nameTv)
        val job: TextView = view.findViewById(R.id.jobTv)
        val unblockButton: MaterialButton = view.findViewById(R.id.unblock_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
        return BlockedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context

        holder.name.text = item.nickName
        holder.job.text = " · ${item.job}"

        //  Glide로 프로필 이미지 로드 (네가 쓰던 방식 그대로)
        Glide.with(context)
            .load(item.profileImageUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .into(holder.profile)

        holder.unblockButton.setOnClickListener {
            onUnblockClick(item)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BlockedAccount>() {
            override fun areItemsTheSame(
                oldItem: BlockedAccount,
                newItem: BlockedAccount
            ): Boolean = oldItem.userId == newItem.userId

            override fun areContentsTheSame(
                oldItem: BlockedAccount,
                newItem: BlockedAccount
            ): Boolean = oldItem == newItem
        }
    }
}
