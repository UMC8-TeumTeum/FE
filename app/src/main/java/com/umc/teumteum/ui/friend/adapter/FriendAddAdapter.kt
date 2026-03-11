package com.umc.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.FriendProfileResult
import com.umc.teumteum.data.remote.friend.model.MutualFriendItem
import com.umc.teumteum.databinding.ItemFriendCheckboxBinding


class FriendAddAdapter(
    private var friends: List<MutualFriendItem> = emptyList()
) : RecyclerView.Adapter<FriendAddAdapter.FriendAddViewHolder>() {

    // 선택 상태 저장 (userId 기준)
    private val selectedIds = mutableSetOf<Int>()

    fun updateList(newItems: List<MutualFriendItem>, preselected: Set<Int> = emptySet()) {
        friends = newItems
        selectedIds.clear()
        // 목록에 존재하는 아이디만 반영
        val idSet = newItems.map { it.userId }.toSet()
        selectedIds.addAll(preselected.intersect(idSet))
        notifyDataSetChanged()
    }

    inner class FriendAddViewHolder(private val binding: ItemFriendCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: MutualFriendItem) {
            binding.nameTextView.text = friend.nickname

            // 프로필 이미지 로드
            Glide.with(binding.profile1.context)
                .load(friend.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profile1)

            // 현재 선택 상태 반영
            val checked = selectedIds.contains(friend.userId)
            binding.checkBoxBtn.isSelected = checked
            binding.checkBoxBtn.setBackgroundResource(
                if (checked) R.drawable.check_box else R.drawable.uncheck_box
            )

            // 토글 로직 (버튼/아이템 어디 눌러도 작동)
            val toggle = {
                if (selectedIds.contains(friend.userId)) {
                    selectedIds.remove(friend.userId)
                    binding.checkBoxBtn.isSelected = false
                    binding.checkBoxBtn.setBackgroundResource(R.drawable.uncheck_box)
                } else {
                    selectedIds.add(friend.userId)
                    binding.checkBoxBtn.isSelected = true
                    binding.checkBoxBtn.setBackgroundResource(R.drawable.check_box)
                }
            }

            binding.checkBoxBtn.setOnClickListener { toggle() }
            binding.root.setOnClickListener { toggle() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendAddViewHolder {
        val binding = ItemFriendCheckboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendAddViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendAddViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int = friends.size

    fun getSelectedUserIdsWithInfo(): List<FriendProfileResult> {
        return friends.filter { selectedIds.contains(it.userId) }
            .map { friend ->
                FriendProfileResult(
                    userId = friend.userId,
                    name = friend.nickname,
                    profileImageUrl = friend.profileImageUrl,
                    field = "", // MutualFriendItem에 field 없음 → 기본값
                    following = false,
                    favorite = false
                )
            }
    }
}
