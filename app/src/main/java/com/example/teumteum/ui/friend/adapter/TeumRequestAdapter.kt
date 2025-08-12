package com.example.teumteum.ui.friend.adapter

import android.graphics.Color
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.databinding.ItemRequestHistoryBinding

class TeumRequestAdapter(private val itemList: List<TeumRequestItem>) :
    RecyclerView.Adapter<TeumRequestAdapter.TeumRequestViewHolder>() {

    inner class TeumRequestViewHolder(val binding: ItemRequestHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeumRequestItem) {
            binding.tvName.text = item.name
            binding.tvDate.text = "${item.date}     |"
            binding.tvTime.text = item.time
            binding.title.text = item.title
            binding.description.text = item.description
            binding.profileIv.setImageResource(item.profileImageRes)

            // api 연결 후 다시 해봐야 됨
            if (item.isCanceled) {
                // 약속 취소 상태일 때
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = "약속이 취소되었어요"

                // 상단 배경 색 변경
                binding.root.findViewById<LinearLayout>(R.id.topContainer)
                    ?.setBackgroundColor(Color.parseColor("#D3D3D3"))

            } else {
                // 기본 정상 상태
                binding.tvStatus.visibility = View.GONE

                binding.root.findViewById<LinearLayout>(R.id.topContainer)
                    ?.setBackgroundColor(Color.parseColor("#7770FE"))
                binding.tvName.setTextColor(Color.WHITE)
                binding.tvDate.setTextColor(Color.WHITE)
                binding.tvTime.setTextColor(Color.WHITE)
                binding.title.setTextColor(Color.parseColor("#0F0F0F"))
                binding.description.setTextColor(Color.parseColor("#788084"))
            }
        }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeumRequestViewHolder {
            val binding = ItemRequestHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TeumRequestViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TeumRequestViewHolder, position: Int) {
            holder.bind(itemList[position])
        }

        override fun getItemCount(): Int = itemList.size
    }