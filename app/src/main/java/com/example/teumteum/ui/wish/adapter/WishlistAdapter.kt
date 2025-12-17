package com.example.teumteum.ui.wish.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.ItemWishlistBinding
import com.example.teumteum.ui.wish.BottomSheetWishEditFragment
import com.example.teumteum.ui.wish.WishSetting01Fragment
import com.example.teumteum.utils.setOnSingleClickListener

class WishlistAdapter(private var wishlist: List<WishlistItem>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

    private val EDIT_SHEET_TAG = "WishEditBottomSheet"

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemWishlistBinding = ItemWishlistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlist[position]
        val binding = holder.binding
        binding.titleTv.text = item.title
        binding.timeTv.text = item.estimatedDuration

        binding.root.setOnSingleClickListener {
            // 이미 바텀시트 떠있으면 막기
            if (fragmentManager.findFragmentByTag(EDIT_SHEET_TAG) != null) return@setOnSingleClickListener

            BottomSheetWishEditFragment
                .newInstance(item.id)
                .show(fragmentManager, EDIT_SHEET_TAG)
        }

        binding.selectButton.setOnClickListener {
            val fragment = WishSetting01Fragment().apply {
                arguments = Bundle().apply {
                    putLong("wish_id", item.id)
                    putString("title", binding.titleTv.text.toString())
                    putString("time", binding.timeTv.text.toString())
                }
            }

            fragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun getItemCount(): Int = wishlist.size

    fun updateList(newList: List<WishlistItem>) {
        wishlist = newList
        notifyDataSetChanged()
    }
}