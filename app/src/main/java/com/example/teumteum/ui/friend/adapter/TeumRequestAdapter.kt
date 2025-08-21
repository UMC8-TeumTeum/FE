package com.example.teumteum.ui.friend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumRequestDateResult
import com.example.teumteum.data.remote.friend.model.UserMiniDto
import com.example.teumteum.databinding.ItemRequestHistoryBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.imageview.ShapeableImageView

class TeumRequestAdapter(
    private var itemList: List<TeumRequestDateResult> = emptyList()
) : RecyclerView.Adapter<TeumRequestAdapter.TeumRequestViewHolder>() {

    inner class TeumRequestViewHolder(val binding: ItemRequestHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: TeumRequestDateResult,
            position: Int,
            fullList: List<TeumRequestDateResult>
        ) {
            // 🔹 초기화
            binding.tvStatus.visibility = View.GONE
            binding.tvResendNotice.visibility = View.GONE

            //  재요청 카드
            if (data.isResend == true) {
                binding.tvResendNotice.visibility = View.VISIBLE
                binding.tvResendNotice.text = "재요청 기록이 있어요"
                binding.topContainer.setBackgroundResource(R.drawable.bg_top_rounded)
            }

            //  취소된 원본 카드 (뒤에 재요청이 없는 경우)
            else if (
                data.isCancelled &&
                data.accepted.isNullOrEmpty() &&   // ✅ 수락자가 없을 때만
                (position + 1 >= fullList.size || fullList[position + 1].isResend != true)
            ) {
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = "약속이 취소되었어요"
                binding.topContainer.setBackgroundResource(R.drawable.bg_top_rounded_cancel)
            }

            //  그 외 기본 상태
            else {
                binding.topContainer.setBackgroundResource(R.drawable.bg_top_rounded)
            }

            // 🔹 원본/재요청 구분선
            binding.originalDivider.visibility = View.GONE
            val isOriginal = data.isResend == false
            val nextIsResend =
                if (position + 1 < fullList.size) fullList[position + 1].isResend == true else false
            if (isOriginal && nextIsResend) {
                binding.originalDivider.visibility = View.VISIBLE
            }

            // 🔹 프로필, 이름
            Glide.with(binding.profileIv.context)
                .load(data.requester.profileImageUrl)
                .placeholder(com.example.teumteum.R.drawable.gray_teum)
                .error(com.example.teumteum.R.drawable.gray_teum)
                .fallback(com.example.teumteum.R.drawable.gray_teum)
                .into(binding.profileIv)

            binding.tvName.text = data.requester.nickname ?: "이름없음"

            // 🔹 날짜/시간
            val dateFormatted = data.date.replace("-", ".").substring(2) // "25.09.02"
            binding.tvDate.text = "$dateFormatted     |"
            binding.tvTime.text = "${data.timeSlot.start} ~ ${data.timeSlot.end}"

            // 🔹 제목 / 설명
            binding.title.text = data.title
            binding.description.text = data.description

            // 🔹 상태별 FlexboxLayout 처리
            updateSectionRow(
                binding.rowAccept,
                binding.labelAccept,
                binding.containerAccepted,
                data.accepted
            )
            updateSectionRow(
                binding.rowCancel,
                binding.labelCancel,
                binding.containerCancelled,
                data.cancelled
            )
            updateSectionRow(
                binding.rowNoAnswer,
                binding.labelNoAnswer,
                binding.containerPending,
                data.pending
            )
        }

        private fun updateSectionRow(
            row: View,
            label: View,
            container: FlexboxLayout,
            users: List<UserMiniDto>
        ) {
            if (users.isNullOrEmpty()) {
                row.visibility = View.GONE
            } else {
                row.visibility = View.VISIBLE
                label.visibility = View.VISIBLE
                container.visibility = View.VISIBLE

                // marginTop 동적 설정
                val params = row.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = 24.dp(row.context)
                row.layoutParams = params

                setUserList(container, users)
            }
        }

        fun Int.dp(context: Context): Int =
            (this * context.resources.displayMetrics.density).toInt()

        private fun setUserList(container: FlexboxLayout, users: List<UserMiniDto>) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(container.context)
            for (user in users) {
                val imageView = inflater.inflate(
                    com.example.teumteum.R.layout.item_user_circle,
                    container,
                    false
                ) as ShapeableImageView
                Glide.with(imageView.context)
                    .load(user.profileImageUrl)
                    .placeholder(com.example.teumteum.R.drawable.gray_teum)
                    .error(com.example.teumteum.R.drawable.gray_teum)
                    .fallback(com.example.teumteum.R.drawable.gray_teum)
                    .into(imageView)

                container.addView(imageView)
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
        holder.bind(itemList[position], position, itemList)
    }

    override fun getItemCount(): Int = itemList.size

    fun submitList(newList: List<TeumRequestDateResult>) {
        itemList = newList
        notifyDataSetChanged()
    }
}

