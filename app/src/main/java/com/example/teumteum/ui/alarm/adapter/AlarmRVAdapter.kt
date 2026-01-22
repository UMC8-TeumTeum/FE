package com.example.teumteum.ui.alarm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.teumteum.R
import com.example.teumteum.data.remote.alarm.dto.NotificationResponse
import com.example.teumteum.databinding.ItemHomeAlarmBinding
import com.example.teumteum.utils.TimeUtils

class AlarmRVAdapter(
    private val items: MutableList<NotificationResponse>,
    private val onItemClick: (NotificationResponse) -> Unit
) : RecyclerView.Adapter<AlarmRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHomeAlarmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeAlarmBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val b = holder.binding

        // 1) 텍스트
        b.alarmNameTv.text = item.friendNickname
        b.alarmContentTv.text = item.content
        b.alarmTimeTv.text = TimeUtils.toRelativeTime(item.createdAt)

        // 2) 프로필 이미지
        val url = item.getProfileImageUrl()
        if (url.isNullOrBlank()) {
            b.profileIv.setImageResource(R.drawable.gray_teum)
        } else {
            Glide.with(b.root)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(b.profileIv)
        }

        // 3) 읽음/미읽음 스타일
        if (item.isRead) {
            val gray = ContextCompat.getColor(b.root.context, R.color.teumteum_gray)
            b.alarmNameTv.setTextColor(gray)
            b.alarmContentTv.setTextColor(gray)
            b.alarmTimeTv.setTextColor(gray)
            b.profileIv.imageAlpha = 100
        } else {
            val black = ContextCompat.getColor(b.root.context, R.color.black)
            b.alarmNameTv.setTextColor(black)
            b.alarmContentTv.setTextColor(black)
            b.alarmTimeTv.setTextColor(black)
            b.profileIv.imageAlpha = 255
        }

        // 클릭 시(읽음처럼 보이기)
        b.root.setOnClickListener {
            if (!item.isRead) {
                val gray = ContextCompat.getColor(b.root.context, R.color.teumteum_gray)
                b.alarmNameTv.setTextColor(gray)
                b.alarmContentTv.setTextColor(gray)
                b.alarmTimeTv.setTextColor(gray)
                b.profileIv.imageAlpha = 100
            }
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun replaceAll(newItems: List<NotificationResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun append(newItems: List<NotificationResponse>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }
}
