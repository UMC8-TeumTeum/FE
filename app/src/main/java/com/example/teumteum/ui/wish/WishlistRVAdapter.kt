package com.example.teumteum.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.databinding.ItemWishlistBinding
import com.example.teumteum.ui.signup.OnBoardingProfileFragment

class WishlistRVAdapter(private var wishlist: List<WishItem>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<WishlistRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemWishlistBinding = ItemWishlistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlist[position]
        val binding = holder.binding
        binding.wishTitleTv.text = item.title
        binding.wishTimeTv.text = item.time

        binding.root.setOnClickListener {
            val bottomSheet = WishEditFragment.newInstanceWithWishDummy(item)
            bottomSheet.show(fragmentManager, bottomSheet.tag)
        }

        binding.fillButton.setOnClickListener {
            val fragment = WishSetting01Fragment().apply {
                arguments = Bundle().apply {
                    putString("title", binding.wishTitleTv.text.toString())
                }
            }

            fragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun getItemCount(): Int = wishlist.size

    fun updateList(newList: List<WishItem>) {
        wishlist = newList.toMutableList()
        notifyDataSetChanged()
    }
}