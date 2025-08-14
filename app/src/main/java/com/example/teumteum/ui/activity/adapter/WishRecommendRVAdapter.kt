package com.example.teumteum.ui.activity.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.activity.model.ActivityWishResult
import com.example.teumteum.databinding.ItemWishlistBinding
import com.example.teumteum.ui.activity.FillingSetting01Fragment

class WishRecommendRVAdapter(private var wishList: List<ActivityWishResult>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<WishRecommendRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishList[position]
        val binding = holder.binding

        binding.wishTitleTv.text = item.title
        binding.wishTimeTv.text =
            when (item.estimatedDuration.lowercase()) { "1h" -> "1h-" ; else -> item.estimatedDuration }

        binding.fillButton.setOnClickListener {
            val fragment = FillingSetting01Fragment().apply {
                arguments = Bundle().apply {
                    putString("source", "FillingActivity")
                    putLong("id", item.id)
                    putString("title", binding.wishTitleTv.text.toString())
                    putString("time", binding.wishTimeTv.text.toString())
                }
            }

            fragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = minOf(wishList.size, 3)

    fun updateList(newList: List<ActivityWishResult>) {
        wishList = newList
        notifyDataSetChanged()
    }
}