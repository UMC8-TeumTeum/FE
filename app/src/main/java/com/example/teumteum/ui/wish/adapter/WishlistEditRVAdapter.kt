package com.example.teumteum.ui.wish.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.ItemWishlistEditBinding

class WishlistEditRVAdapter(private val wishlist: MutableList<WishlistItem>) : RecyclerView.Adapter<WishlistEditRVAdapter.ViewHolder>() {

    // 페이징/갱신 사이클 동안 선택 상태 유지용
    private val selectedIds = mutableSetOf<Long>()

    init {
        // 초기 데이터에 체크된 항목이 있으면 동기화
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

        if (item.isDeleted) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            return
        }

        binding.tvWishTitle.text = item.title
        binding.wishTimeTv.text = item.estimatedDuration

        // 리스너 중복 호출 방지
        binding.wishCheckbox.setOnCheckedChangeListener(null)

        // 선택 집합 기준으로 체크 상태 복원
        val checked = selectedIds.contains(item.id)
        binding.wishCheckbox.isChecked = checked
        item.isChecked = checked
        setCheckBoxTint(binding.wishCheckbox, checked)

        binding.wishCheckbox.setOnCheckedChangeListener { button, isChecked ->
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

    fun markCheckedItemsAsDeleted(): Int {
        val deletedItems = wishlist.filter { selectedIds.contains(it.id) && !it.isDeleted }
        deletedItems.forEach {
            it.isDeleted = true
            it.isChecked = false
            selectedIds.remove(it.id)
        }
        notifyDataSetChanged()
        return deletedItems.size
    }
}
