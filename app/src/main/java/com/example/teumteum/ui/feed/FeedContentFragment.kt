package com.example.teumteum.ui.feed

import FeedContentAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.data.Feed
import com.example.teumteum.databinding.FragmentFeedContentBinding
import kotlin.collections.filter
import kotlin.collections.sortedByDescending

class ContentListFragment : Fragment() {

    private lateinit var binding: FragmentFeedContentBinding
    private var contentType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentType = arguments?.getString(ARG_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedContentBinding.inflate(inflater, container, false)

        val dummyData = getDummyItems()
        val filteredList = when (contentType) {
            "latest" -> dummyData.sortedByDescending { it.createdAt }
            "following" -> dummyData.filter { it.isFromFollowedUser }
            else -> dummyData
        }

        val adapter = FeedContentAdapter()
        binding.recyclerView.adapter = adapter
        adapter.submitList(filteredList)

        return binding.root
    }

    fun getDummyItems(): List<Feed> {
        val now = System.currentTimeMillis()

        return listOf(
            Feed(1, "user1", "이건 첫 번째 피드입니다.", now - 1000 * 60 * 2, true),
            Feed(2, "user2", "두 번째 피드 내용입니다.", now - 1000 * 60 * 60, false),
            Feed(3, "user3", "세 번째입니다. 최신 글!", now - 1000 * 30, true),
            Feed(4, "user4", "안녕하세요~", now - 1000 * 60 * 10, false),
            Feed(5, "user1", "팔로잉한 유저의 게시물!", now - 1000 * 60 * 5, true)
        )
    }

    companion object {
        private const val ARG_TYPE = "type"

        fun newInstance(type: String): ContentListFragment {
            return ContentListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }
}