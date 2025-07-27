package com.example.teumteum.ui.wish.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.databinding.ItemWishlistEditBinding

class WishlistEditRVAdapter(private val wishlist: MutableList<WishItem>) : RecyclerView.Adapter<WishlistEditRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWishlistEditBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemWishlistEditBinding = ItemWishlistEditBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlist[position]
        val binding = holder.binding
        binding.tvWishTitle.text = item.title

        binding.wishCheckbox.isChecked = item.isChecked

        setCheckBoxTint(binding.wishCheckbox, item.isChecked)

        binding.wishCheckbox.setOnCheckedChangeListener { button, isChecked ->
            item.isChecked = isChecked
            setCheckBoxTint(button, isChecked)
        }
    }

    override fun getItemCount(): Int = wishlist.size

    fun deleteCheckedItems(): Int {
        val removedCount = wishlist.count { it.isChecked }
        wishlist.removeAll { it.isChecked }
        notifyDataSetChanged()
        return removedCount
    }

    fun cancelAllCheckedItems() {
        wishlist.forEach { it.isChecked = false }
        notifyDataSetChanged()
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val context = checkBox.context
        if (isChecked) {
            // 체크됨 → 배경색처럼 보이도록 tint를 main_1, 체크 ✔는 흰색이 되게
            checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.main_1))
        } else {
            // 미체크 → 회색 테두리처럼 보이게
            checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.teumteum_deactive))
        }
    }
}