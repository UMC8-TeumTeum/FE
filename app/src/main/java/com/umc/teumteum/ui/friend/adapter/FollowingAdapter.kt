package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.FollowingResult
import com.umc.teumteum.databinding.Friend01ItemFollowingBinding

class FollowingAdapter(
    private var data: List<FollowingResult>,
    private val onProfileClick: (FollowingResult) -> Unit,
    private val onSendClick: (FollowingResult) -> Unit,
    private val onStarClick: (Int) -> Unit
) : RecyclerView.Adapter<FollowingAdapter.ViewHolder>() {

    private var favoriteMap: Map<Int, Boolean> = emptyMap()

    inner class ViewHolder(val binding: Friend01ItemFollowingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FollowingResult) {
            binding.nameTv.text = item.nickname

            // 직업(없으면 GONE)
            val job = item.job.trim()
            if (job.isNotEmpty()) {
                binding.jobTv.visibility = View.VISIBLE
                binding.jobTv.text = " · $job"
            } else {
                binding.jobTv.visibility = View.GONE
            }

            Glide.with(binding.profileIv)
                .load(item.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv)

            binding.profileIv.setOnClickListener { onProfileClick(item) }
            binding.sendBtn.setOnClickListener { onSendClick(item) }

            // 즐겨찾기 상태에 따른 아이콘 변경
            val isFav = favoriteMap[item.userId] ?: item.isFavorite
            binding.starIv.setImageResource(
                if (isFav) R.drawable.friend01_fill_star else R.drawable.friend01_star
            )

            binding.starIv.setOnClickListener {
                onStarClick(item.userId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Friend01ItemFollowingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<FollowingResult>) {
        data = newData
        notifyDataSetChanged()
    }

    fun setFavoriteMap(map: Map<Int, Boolean>) {
        favoriteMap = map
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }
}


