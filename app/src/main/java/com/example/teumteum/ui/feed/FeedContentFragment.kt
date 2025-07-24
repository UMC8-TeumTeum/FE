package com.example.teumteum.ui.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.data.Contents
import com.example.teumteum.data.Feed
import com.example.teumteum.databinding.FragmentFeedContentBinding

class FeedContentFragment : Fragment() {

    private lateinit var binding: FragmentFeedContentBinding
    private lateinit var adapter: FeedContentAdapter

    private var contentType: String? = null
    private var currentFilter: String = "all"
    private var pendingFilter: String? = null

    private var feeds: List<Feed> = emptyList()
    private var contents: List<Contents> = emptyList()

    private var onBookmarkClickListener: ((Feed) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentType = arguments?.getString(ARG_TYPE)
        pendingFilter = arguments?.getString(ARG_FILTER) ?: "all"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedContentBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = FeedContentAdapter { clickedFeed ->
            feeds = feeds.map { feed ->
                if (feed == clickedFeed) feed.copy(isBookMarked = !feed.isBookMarked) else feed
            }
            applyFilter(currentFilter)

            onBookmarkClickListener?.invoke(clickedFeed)
        }

        binding.recyclerView.adapter = adapter

        pendingFilter?.let {
            currentFilter = it
            pendingFilter = null
        }

        applyFilter(currentFilter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyFilter(currentFilter)
    }

    fun onFilterSelected(filter: String) {
        if (this::binding.isInitialized) {
            currentFilter = filter
            applyFilter(filter)
        } else {
            pendingFilter = filter
        }
    }

    fun setFeedData(data: List<Feed>) {
        feeds = data
    }

    fun setDummyContents(contentList: List<Contents>) {
        contents = contentList
    }

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "10m" -> feeds.filter { it.time == 10 }
            "20m" -> feeds.filter { it.time == 20 }
            "30m" -> feeds.filter { it.time == 30 }
            else -> feeds
        }

        val feedItemList = mutableListOf<FeedItem>()
        filtered.forEachIndexed { index, feed ->
            if (index == 2) {
                feedItemList.add(FeedItem.ContentsCard(contents))
            }
            feedItemList.add(FeedItem.FeedData(feed))
        }

        adapter.submitList(feedItemList)
    }

    fun setOnBookmarkClickListener(listener: (Feed) -> Unit) {
        onBookmarkClickListener = listener
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_FILTER = "filter"

        fun newInstance(type: String, filter: String): FeedContentFragment {
            return FeedContentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                    putString(ARG_FILTER, filter)
                }
            }
        }
    }
}
