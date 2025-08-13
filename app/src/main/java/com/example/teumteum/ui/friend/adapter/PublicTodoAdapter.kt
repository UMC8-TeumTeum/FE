package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.PublicTodoResult

class PublicTodoAdapter :
    ListAdapter<PublicTodoResult, PublicTodoAdapter.PublicTodoViewHolder>(
        diffCallback
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicTodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teum_event_card, parent, false)
        return PublicTodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicTodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PublicTodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvEndTime: TextView = itemView.findViewById(R.id.tvEndTime)
        private val tvEventDesc: TextView = itemView.findViewById(R.id.tvEventDesc)

        fun bind(item: PublicTodoResult) {
            tvStartTime.text = item.startTime
            tvEndTime.text = item.endTime
            tvEventDesc.text = item.title
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PublicTodoResult>() {
            override fun areItemsTheSame(oldItem: PublicTodoResult, newItem: PublicTodoResult) =
                oldItem.title == newItem.title && oldItem.startTime == newItem.startTime

            override fun areContentsTheSame(oldItem: PublicTodoResult, newItem: PublicTodoResult) =
                oldItem == newItem
        }
    }
}

