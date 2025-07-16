package com.example.teumteum.ui.wish

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.databinding.ItemWishlistBinding

class WishlistRVAdapter(private val wishlist: List<WishItem>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<WishlistRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemWishlistBinding = ItemWishlistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlist[position]
        val binding = holder.binding
        binding.tvWishTitle.text = item.title

        binding.root.setOnClickListener {
            val bottomSheet = WishEditFragment.newInstanceWithDummy(item)
            bottomSheet.show(fragmentManager, bottomSheet.tag)
        }
    }

    override fun getItemCount(): Int = wishlist.size
}