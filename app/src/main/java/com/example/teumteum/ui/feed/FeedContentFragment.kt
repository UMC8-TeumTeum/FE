package com.example.teumteum.ui.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.data.Feed
import com.example.teumteum.databinding.FragmentFeedContentBinding
import kotlin.collections.filter
import kotlin.collections.sortedByDescending

class ContentListFragment : Fragment() {

    private lateinit var binding: FragmentFeedContentBinding
    private var contentType: String? = null
    private lateinit var adapter: FeedContentAdapter
    private var fullList: List<Feed> = emptyList()
    private var currentFilter: String = "all" // 현재 필터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentType = arguments?.getString(ARG_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedContentBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fullList = when (contentType) {
            "latest" -> getDummyItems().sortedByDescending { it.createdAt }
            "following" -> getDummyItems().filter { it.isFromFollowedUser }
            else -> getDummyItems()
        }

        adapter = FeedContentAdapter()
        binding.recyclerView.adapter = adapter

        // 초기에는 전체 필터 적용
        applyFilter(currentFilter)

        return binding.root
    }

    fun onFilterSelected(filter: String) {
        currentFilter = filter
        applyFilter(filter)
    }


    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "10m" -> fullList.filter { isWithinMinutes(it.createdAt, 10) }
            "20m" -> fullList.filter { isWithinMinutes(it.createdAt, 20) }
            "30m" -> fullList.filter { isWithinMinutes(it.createdAt, 30) }
            else -> fullList
        }
        adapter.submitList(filtered)
    }


    private fun isWithinMinutes(createdAt: Long, minutes: Int): Boolean {
        val diffMillis = System.currentTimeMillis() - createdAt
        return diffMillis <= minutes * 60 * 1000
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