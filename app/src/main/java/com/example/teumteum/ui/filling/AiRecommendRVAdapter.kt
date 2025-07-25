package com.example.teumteum.ui.filling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.entities.AiRecommend
import com.example.teumteum.databinding.ItemWishlistBinding
import com.example.teumteum.ui.wish.WishSetting01Fragment

class AiRecommendRVAdapter( private var aiList: List<AiRecommend>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<AiRecommendRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = aiList[position]
        val binding = holder.binding
        binding.wishTitleTv.text = item.title
        binding.wishTimeTv.text = item.time

        binding.fillButton.setOnClickListener {
            val fragment = FillingSetting01Fragment().apply {
                arguments = Bundle().apply {
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

    override fun getItemCount(): Int = 3

    fun updateList(newList: List<AiRecommend>) {
        aiList = newList
        notifyDataSetChanged()
    }
}
