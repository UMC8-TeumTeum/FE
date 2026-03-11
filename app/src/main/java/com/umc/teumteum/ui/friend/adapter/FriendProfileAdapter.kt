package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.FriendProfileResult
import com.umc.teumteum.databinding.ItemFriendProfileCardBinding

class FriendProfileAdapter(
    private val profiles: List<FriendProfileResult>,
    private val onToggleExclude: (userId: Int) -> Unit
) : RecyclerView.Adapter<FriendProfileAdapter.FriendProfileViewHolder>() {

    // ViewModel에서 내려준 제외된 ID 집합을 들고 있다가 바인딩에 반영
    private var excludedIds: Set<Int> = emptySet()

    // 외부에서 제외 집합을 갱신해주면 전체 싱크
    fun setExcludedIds(newSet: Set<Int>) {
        excludedIds = newSet
        notifyDataSetChanged()
    }

    inner class FriendProfileViewHolder(val binding: ItemFriendProfileCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: FriendProfileResult) {
            binding.nameTextView.text = profile.name

            Glide.with(binding.profileImageView.context)
                .load(profile.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileImageView)

            val isExcluded = excludedIds.contains(profile.userId)
            applyDimUi(isExcluded)
        }

        // 제외된 유저면 카드/프로필/텍스트를 흐리게 보여주기
        private fun applyDimUi(isExcluded: Boolean) {
            val alphaWhenExcluded = 0.4f
            val alphaNormal = 1f

            val targetAlpha = if (isExcluded) alphaWhenExcluded else alphaNormal

            // 카드 전체 흐림
            binding.root.alpha = targetAlpha
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendProfileViewHolder {
        val binding = ItemFriendProfileCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size
}
