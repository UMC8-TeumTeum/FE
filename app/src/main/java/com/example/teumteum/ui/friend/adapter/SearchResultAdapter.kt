package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.FriendSearchResult
import com.google.android.material.imageview.ShapeableImageView

class SearchResultAdapter(
    private val results: List<FriendSearchResult>,
    private val onItemClick: (userId: Int) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileLayout)
        private val nameText: TextView = itemView.findViewById(R.id.nameTv)
        private val jobText: TextView = itemView.findViewById(R.id.jobTv)

        fun bind(item: FriendSearchResult) {
            nameText.text = item.nickname
            jobText.text = " · ${item.job}"

            Glide.with(itemView.context)
                .load(item.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .into(profileImage)

            itemView.setOnClickListener {
                onItemClick(item.userId)
            }

            profileImage.setOnClickListener {
                onItemClick(item.userId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size
}
