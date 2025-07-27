package com.example.teumteum.ui.wish.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.dto.WishlistItem
import com.example.teumteum.databinding.ItemWishlistEditBinding

class WishlistEditRVAdapter(private val wishlist: MutableList<WishlistItem>) : RecyclerView.Adapter<WishlistEditRVAdapter.ViewHolder>() {

    private val isCheckedList = MutableList(wishlist.size) { false }

    inner class ViewHolder(val binding: ItemWishlistEditBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemWishlistEditBinding = ItemWishlistEditBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlist[position]
        val binding = holder.binding
        binding.tvWishTitle.text = item.title
        binding.wishTimeTv.text = item.estimatedDuration

        binding.wishCheckbox.setOnCheckedChangeListener(null)
        binding.wishCheckbox.isChecked = isCheckedList[position]
        setCheckBoxTint(binding.wishCheckbox, isCheckedList[position])

        // 리스너 설정
        binding.wishCheckbox.setOnCheckedChangeListener { button, isChecked ->
            isCheckedList[position] = isChecked
            setCheckBoxTint(button, isChecked)
        }
    }

    override fun getItemCount(): Int = wishlist.size

    fun getCheckedWishIdsAndRemove(): List<Long> {
        val checkedIds = mutableListOf<Long>()
        val indicesToRemove = wishlist.indices.filter { isCheckedList[it] }.reversed()

        indicesToRemove.forEach {
            checkedIds.add(wishlist[it].id)  // 서버에 보낼 id 저장
            wishlist.removeAt(it)
            isCheckedList.removeAt(it)
        }

        notifyDataSetChanged()
        return checkedIds
    }

    fun cancelAllCheckedItems() {
        for (i in isCheckedList.indices) {
            isCheckedList[i] = false
        }
        notifyDataSetChanged()
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val context = checkBox.context
        val colorRes = if (isChecked) R.color.main_1 else R.color.teumteum_deactive
        checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    }
}