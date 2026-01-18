package com.example.teumteum.ui.friend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.databinding.ItemTeumImageBinding

class TeumImagePagerAdapter(
    private val items: List<Int>
) : RecyclerView.Adapter<TeumImagePagerAdapter.VH>() {

    inner class VH(val binding: ItemTeumImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTeumImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.imgTeum.setImageResource(items[position])
    }

    override fun getItemCount(): Int = items.size
}
