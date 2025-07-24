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
    private var contentType: String? = null
    private lateinit var adapter: FeedContentAdapter
    private var fullList: List<Feed> = emptyList()
    private var currentFilter: String = "all"
    private var pendingFilter: String? = null

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

        fullList = when (contentType) {
            "latest" -> getDummyItems()
            "following" -> getDummyItems().filter { it.isFromFollowedUser }
            else -> getDummyItems()
        }

        adapter = FeedContentAdapter()
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

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "10m" -> fullList.filter { it.time == 10 }
            "20m" -> fullList.filter { it.time == 20 }
            "30m" -> fullList.filter { it.time == 30 }
            else -> fullList
        }

        val feedItemList = mutableListOf<FeedItem>()

        filtered.forEachIndexed { index, feed ->
            if (index == 2) {
                feedItemList.add(FeedItem.ContentsCard(getDummyContents()))
            }
            feedItemList.add(FeedItem.FeedData(feed))
        }

        adapter.submitList(feedItemList)
    }

    fun getDummyItems(): List<Feed> {
        return listOf(
            Feed(
                username = "미나리",
                userField = "UX디자이너",
                title = "무인 세차장 속 숨겨진 기능들",
                contents = "무인 세차장에도 사용자를 고려한 숨겨진 원리들이 있다는 것 아시나요? 집 앞 드라이브 나갈 겸 세차 하고 오세요!",
                borderTitle = "무인 세차장 속 ux 비밀",
                borderLink = "https://www.youtube.com/watch",
                time = 10,
                isBookMarked = true,
                isFromFollowedUser = true
            ),
            Feed(
                username = "애플",
                userField = "개발자",
                title = "최근 애플에서 발표한 기술들",
                contents = "매거진에서 화제가 된 애플 기술을 가지고 왔습니다~\n" +
                        "애플의 발전은 정말 무궁무진 한 것 같아요!",
                borderTitle = "애플 신기능 발표",
                borderLink = "https://www.youtube.com/watch",
                time = 20,
                isBookMarked = false,
                isFromFollowedUser = true
            ),
            Feed(
                username = "해빗",
                userField = "UX디자이너",
                title = "지구온난화로 인한 이상기후",
                contents = "지구온난화로 인해 지구 곳곳에 이상기후가 관측되고 있어요. 모두의 관심이 필요합니다. 한번씩 보고 가주세요!",
                borderTitle = "지구온난화 다큐멘터리",
                borderLink = "https://www.youtube.com/watch",
                time = 20,
                isBookMarked = false,
                isFromFollowedUser = true
            ),
            Feed(
                username = "지니",
                userField = "개발자",
                title = "지구온난화로 인한 이상기후",
                contents = "지구온난화로 인해 지구 곳곳에 이상기후가 관측되고 있어요. 모두의 관심이 필요합니다. 한번씩 보고 가주세요!",
                borderTitle = "지구온난화 다큐멘터리",
                borderLink = "https://www.youtube.com/watch",
                time = 10,
                isBookMarked = true,
                isFromFollowedUser = false
            )
        )
    }

    fun getDummyContents(): List<Contents> {
        return listOf(
            Contents(
                username = "나루",
                userField = "개발자",
                title = "개발 용어 모음집",
                time = 10
            ),
            Contents(
                username = "결이",
                userField = "개발자",
                title = "디자이너와 소통하는 법",
                time = 10
            )
        )
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
