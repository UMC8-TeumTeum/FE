package com.example.teumteum.ui.myhome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.ui.myhome.data.BlockedAccount
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class BlockedAccountAdapter(
    private val items: List<BlockedAccount>,
    private val onUnblockClick: (BlockedAccount) -> Unit
) : RecyclerView.Adapter<BlockedAccountAdapter.BlockedViewHolder>() {

    inner class BlockedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val profile = view.findViewById<ShapeableImageView>(R.id.profileLayout)
        val name = view.findViewById<TextView>(R.id.nameTv)
        val job = view.findViewById<TextView>(R.id.jobTv)
        val unblockButton = view.findViewById<MaterialButton>(R.id.unblock_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
        return BlockedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val item = items[position]

        holder.profile.setImageResource(item.profileRes)
        holder.name.text = item.name
        holder.job.text = " · ${item.job}"

        holder.unblockButton.setOnClickListener {
            onUnblockClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
