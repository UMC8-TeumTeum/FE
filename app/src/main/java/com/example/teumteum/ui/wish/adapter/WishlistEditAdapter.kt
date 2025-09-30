package com.example.teumteum.ui.wish.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.ItemWishlistEditBinding

class WishlistEditRVAdapter(private val wishlist: MutableList<WishlistItem>) : RecyclerView.Adapter<WishlistEditRVAdapter.ViewHolder>() {

    // 페이징/갱신 사이클 동안 선택 상태 유지용 id 집합
    private val selectedIds = mutableSetOf<Long>()

    init {
        // 초기 데이터에 체크된 항목 반영
        selectedIds.addAll(wishlist.filter { it.isChecked }.map { it.id })
        setHasStableIds(true)
    }

    inner class ViewHolder(val binding: ItemWishlistEditBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemId(position: Int): Long = wishlist[position].id

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemWishlistEditBinding = ItemWishlistEditBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlist[position]
        val binding = holder.binding

        binding.titleTv.text = item.title
        binding.timeTv.text = item.estimatedDuration

        // 리스너 중복 호출 방지
        binding.checkbox.setOnCheckedChangeListener(null)

        // 선택 집합 기준으로 체크 상태 복원
        val checked = selectedIds.contains(item.id)
        binding.checkbox.isChecked = checked
        item.isChecked = checked
        setCheckBoxTint(binding.checkbox, checked)

        binding.checkbox.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                selectedIds.add(item.id)
            } else {
                selectedIds.remove(item.id)
            }
            item.isChecked = isChecked
            setCheckBoxTint(button, isChecked)
        }
    }

    override fun getItemCount(): Int = wishlist.size

    fun cancelAllCheckedItems() {
        selectedIds.clear()
        wishlist.forEach { it.isChecked = false }
        notifyDataSetChanged()
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val context = checkBox.context
        val colorRes = if (isChecked) R.color.main_1 else R.color.teumteum_deactive
        checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    }


    // 체크된 항목을 제거하고, 제거된 항목들의 id를 반환 (Fragment에서 서버로 삭제 요청 보낼 때 사용)
    fun markCheckedItemsAsDeletedAndReturnIds(): List<Long> {
        val toRemoveIdx = mutableListOf<Int>()
        val removedIds = mutableListOf<Long>()
        wishlist.forEachIndexed { index, it ->
            if (selectedIds.contains(it.id) && !it.isDeleted) {
                it.isDeleted = true
                toRemoveIdx.add(index)
                removedIds.add(it.id)
            }
        }
        if (toRemoveIdx.isEmpty()) return emptyList()

        for (i in toRemoveIdx.asReversed()) {
            val removed = wishlist.removeAt(i)
            selectedIds.remove(removed.id)
            notifyItemRemoved(i)
        }
        return removedIds
    }

}
