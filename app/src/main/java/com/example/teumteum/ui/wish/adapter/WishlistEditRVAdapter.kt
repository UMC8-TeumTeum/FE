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

    inner class ViewHolder(val binding: ItemWishlistEditBinding) : RecyclerView.ViewHolder(binding.root)

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

        binding.wishCheckbox.isChecked = item.isChecked
        setCheckBoxTint(binding.wishCheckbox, item.isChecked)

        binding.wishCheckbox.setOnCheckedChangeListener { button, isChecked ->
            item.isChecked = isChecked
            setCheckBoxTint(button, isChecked)
        }
    }

    override fun getItemCount(): Int = wishlist.size

    fun cancelAllCheckedItems() {
        wishlist.forEach { it.isChecked = false }
        notifyDataSetChanged()
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val context = checkBox.context
        val colorRes = if (isChecked) R.color.main_1 else R.color.teumteum_deactive
        checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    }

    fun markCheckedItemsAsDeleted(): Int {
        val deletedItems = wishlist.filter { it.isChecked && !it.isDeleted }
        deletedItems.forEach {
            it.isDeleted = true
            it.isChecked = false
        }
        notifyDataSetChanged()
        return deletedItems.size
    }

}