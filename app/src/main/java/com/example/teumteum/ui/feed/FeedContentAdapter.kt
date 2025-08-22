package com.example.teumteum.ui.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.ui.feed.data.Contents
import com.example.teumteum.ui.feed.data.Feed
import com.example.teumteum.databinding.ItemContentsCardBinding
import com.example.teumteum.databinding.ItemFeedBinding
import kotlin.math.roundToInt

sealed class FeedItem {
    data class FeedData(val feed: Feed) : FeedItem()
    data class ContentsCard(val contentsList: List<Contents>) : FeedItem()
}

class FeedContentAdapter(
    private val onBookmarkClick: (Feed) -> Unit
    ) : ListAdapter<FeedItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val VIEW_TYPE_FEED = 0
        private const val VIEW_TYPE_CONTENTS = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                return when {
                    oldItem is FeedItem.FeedData && newItem is FeedItem.FeedData ->
                        oldItem.feed.username == newItem.feed.username &&
                                oldItem.feed.title     == newItem.feed.title
                    oldItem is FeedItem.ContentsCard && newItem is FeedItem.ContentsCard ->
                        oldItem.contentsList == newItem.contentsList
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is FeedItem.FeedData     -> VIEW_TYPE_FEED
        is FeedItem.ContentsCard -> VIEW_TYPE_CONTENTS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_FEED -> {
                val binding = ItemFeedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                FeedViewHolder(binding)
            }
            VIEW_TYPE_CONTENTS -> {
                val binding = ItemContentsCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ContentsCardViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 아이템별 좌우 마진 적용
        val lp = holder.itemView.layoutParams as RecyclerView.LayoutParams
        val side20 = holder.itemView.context.dp(20)
        when (getItemViewType(position)) {
            VIEW_TYPE_CONTENTS -> {
                lp.leftMargin = 0
                lp.rightMargin = 0
            }
            else -> { // 일반 피드: 좌우 20dp 여백
                lp.leftMargin = side20
                lp.rightMargin = side20
            }
        }
        holder.itemView.layoutParams = lp

        when (val item = getItem(position)) {
            is FeedItem.FeedData     -> (holder as FeedViewHolder).bind(item.feed)
            is FeedItem.ContentsCard -> (holder as ContentsCardViewHolder).bind(item.contentsList)
        }
    }

    inner class FeedViewHolder(private val binding: ItemFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Feed) = with(binding) {
            nicknameTv.text   = item.username
            fieldTv.text      = "  •  ${item.userField}"
            feedTitle.text    = item.title
            feedBody.text     = item.contents
            filterTv.text     = "${item.time}m"
            feedBorderTitleTv.text = item.borderTitle
            feedBorderLinkTv.text  = item.borderLink
            val bookmarkRes = if (item.isBookMarked) {
                R.drawable.ic_bookmark_on
            } else {
                R.drawable.ic_bookmark_off
            }
            bookmarkIv.setImageResource(bookmarkRes)

            bookmarkIv.setOnClickListener {
                onBookmarkClick(item)
            }
            profileIv.setImageResource(item.userProfileImage)
            if (item.contentsImage != 0) {
                feedBorderImage.isVisible = true
                feedBorderImage.setImageResource(item.contentsImage)
            } else {
                feedBorderImage.isVisible = false
            }
        }
    }

    inner class ContentsCardViewHolder(private val binding: ItemContentsCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contentsList: List<Contents>) {
            val innerAdapter = ContentsHorizontalAdapter(contentsList)
            binding.horizontalRecycler.apply {
                layoutManager = LinearLayoutManager(
                    binding.root.context, LinearLayoutManager.HORIZONTAL, false
                )
                adapter = innerAdapter
            }
        }
    }
}

private fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).roundToInt()