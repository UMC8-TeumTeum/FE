package com.example.teumteum.ui.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.Friend02ItemRequestTeumCardBinding

class FriendResponseCardAdapter(
    private val responseList: List<FriendResponseData>
) : RecyclerView.Adapter<FriendResponseCardAdapter.ResponseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResponseViewHolder {
        // 1) 정확한 바인딩 클래스 이름으로 inflate
        val binding = Friend02ItemRequestTeumCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResponseViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ResponseViewHolder,
        position: Int
    ) {
        holder.bind(responseList[position])
    }

    override fun getItemCount(): Int = responseList.size

    inner class ResponseViewHolder(
        // 2) 여기도 똑같은 바인딩 클래스
        private val binding: Friend02ItemRequestTeumCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: FriendResponseData) {
            binding.apply {
                tvName.text = "나 > ${data.name}"
                binding.tvDate.text = "${data.date}     |"
                tvTime.text = data.time
                tvTitle.text = data.title
                tvDesc.text = data.desc
                tvSuggestionTime.text =
                    "친구가 원하는 시간 | ${data.suggestionTime}"

            }
        }
    }
}
