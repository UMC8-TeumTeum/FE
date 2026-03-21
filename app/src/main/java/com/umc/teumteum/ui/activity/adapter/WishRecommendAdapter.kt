package com.umc.teumteum.ui.activity.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.activity.model.ActivityWishResult
import com.umc.teumteum.databinding.ItemWishlistBinding
import com.umc.teumteum.ui.activity.BottomSheetActivityContentFragment
import com.umc.teumteum.ui.activity.FillingSetting01Fragment
import com.umc.teumteum.utils.setOnSingleClickListener

class WishRecommendAdapter(private var wishList: List<ActivityWishResult>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<WishRecommendAdapter.ViewHolder>() {

    private val CONTENT_SHEET_TAG = "ContentBottomSheetFragment"

    inner class ViewHolder(val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishList[position]
        val b = holder.binding

        b.titleTv.text = item.title
        b.timeTv.text =
            when (item.estimatedDuration.lowercase()) { "1h" -> "1h-" ; else -> item.estimatedDuration }

        b.root.setOnSingleClickListener {
            // 이미 바텀시트 떠있으면 막기
            if (fragmentManager.findFragmentByTag(CONTENT_SHEET_TAG) != null) return@setOnSingleClickListener

            val args = Bundle().apply {
                putLong("wish_id", item.id)
                putString("title", item.title)
                putString("content", item.content)
                putString("time", item.estimatedDuration)
            }
            BottomSheetActivityContentFragment().apply { arguments = args }
                .show(fragmentManager, CONTENT_SHEET_TAG)
        }

        b.selectButton.setOnClickListener {
            val fragment = FillingSetting01Fragment().apply {
                arguments = Bundle().apply {
                    putLong("wish_id", item.id)
                    putString("title", b.titleTv.text.toString())
                    putString("time", b.timeTv.text.toString())
                }
            }

            fragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = minOf(wishList.size, 3)
}