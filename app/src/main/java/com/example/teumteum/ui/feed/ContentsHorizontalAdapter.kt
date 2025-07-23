package com.example.teumteum.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.data.Contents
import com.example.teumteum.databinding.ItemContentsBinding

class ContentsHorizontalAdapter(
    private val items: List<Contents>
) : RecyclerView.Adapter<ContentsHorizontalAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemContentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contents) {
            binding.nicknameTv.text = item.username
            binding.fieldTv.text = "  •  ${item.userField}"
            binding.titleTv.text = item.title
            binding.filterTv.text = item.time.toString() + "m"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContentsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
