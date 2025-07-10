package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.Friend02ItemTeumCardBinding

class FriendRequestCardAdapter(private val requestList: List<FriendRequestData>) :
    RecyclerView.Adapter<FriendRequestCardAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = Friend02ItemTeumCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requestList[position])
    }

    override fun getItemCount(): Int = requestList.size

    inner class RequestViewHolder(private val binding: Friend02ItemTeumCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: FriendRequestData) {
            binding.tvName.text = data.name
            binding.tvDate.text = "${data.date}     |"
            binding.tvTime.text = data.time
            binding.tvTitle.text = data.title
            binding.tvDesc.text = data.desc
        }
    }
}
