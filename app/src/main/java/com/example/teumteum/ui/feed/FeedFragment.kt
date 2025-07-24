package com.example.teumteum.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.teumteum.R
import com.example.teumteum.data.Contents
import com.example.teumteum.data.Feed
import com.example.teumteum.databinding.FragmentFeedBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private val tabTitles = listOf("최신", "팔로잉")
    private val tabTypes = listOf("latest", "following")
    private val fragmentList = mutableListOf<FeedContentFragment>()
    private var selectedFilter: String = "all"
    private var feeds: MutableList<Feed> = mutableListOf()
    private var contents: MutableList<Contents> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        feeds = getDummyItems().toMutableList()
        contents = getDummyContents().toMutableList()
        setupTabs()
        setupFilterButtons()
    }

    private fun setupTabs() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabTypes.size

            override fun createFragment(position: Int): Fragment {
                val type = tabTypes[position]
                val fragment = FeedContentFragment.newInstance(type, selectedFilter)

                val filteredFeeds = when (type) {
                    "latest" -> feeds
                    "following" -> feeds.filter { it.isFromFollowedUser }
                    else -> emptyList()
                }

                fragment.setFeedData(filteredFeeds)
                fragment.setDummyContents(contents)

                fragment.setOnBookmarkClickListener { clickedFeed ->
                    feeds = feeds.map { feed ->
                        if (feed.username == clickedFeed.username && feed.title == clickedFeed.title) {
                            feed.copy(isBookMarked = !feed.isBookMarked)
                        } else feed
                    }.toMutableList()

                    fragmentList.forEachIndexed { idx, frag ->
                        val filteredFeeds = when (tabTypes[idx]) {
                            "latest" -> feeds
                            "following" -> feeds.filter { it.isFromFollowedUser }
                            else -> emptyList()
                        }
                        frag.setFeedData(filteredFeeds)
                        frag.onFilterSelected(selectedFilter)
                    }
                }


                if (fragmentList.size <= position) {
                    fragmentList.add(fragment)
                } else {
                    fragmentList[position] = fragment
                }

                return fragment
            }
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val customView = LayoutInflater.from(requireContext())
                .inflate(R.layout.feed_custom_tab, null)
            customView.findViewById<TextView>(R.id.tab_text).text = tabTitles[position]
            tab.customView = customView
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabStyle(tab, true)
                sendFilter(selectedFilter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabStyle(tab, false)
                sendFilter(selectedFilter)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                sendFilter(selectedFilter)
            }
        })

        binding.tabLayout.getTabAt(binding.viewPager.currentItem)?.let {
            updateTabStyle(it, true)
        }
    }

    private fun updateTabStyle(tab: TabLayout.Tab, isSelected: Boolean) {
        val tabText = tab.customView?.findViewById<TextView>(R.id.tab_text)
        tabText?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isSelected) R.color.text_primary else R.color.teumteum_deactive
            )
        )
    }

    private fun setupFilterButtons() {
        val filterButtons = listOf(
            binding.btnAll to "all",
            binding.btn10m to "10m",
            binding.btn20m to "20m",
            binding.btn30m to "30m"
        )

        filterButtons.forEach { (button, filter) ->
            button.setOnClickListener {
                selectedFilter = filter
                updateFilterButtonStates(button)
                sendFilter(filter)
            }
        }

        selectedFilter = "all"
        updateFilterButtonStates(binding.btnAll)
        sendFilter("all")
    }

    private fun updateFilterButtonStates(selected: MaterialButton) {
        val allButtons = listOf(binding.btnAll, binding.btn10m, binding.btn20m, binding.btn30m)

        allButtons.forEach { button ->
            val isSelected = (button == selected)
            button.isSelected = isSelected

            if (isSelected) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                button.strokeWidth = 0
            } else {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teumteum_bg))
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.teumteum_line)
                button.strokeWidth = 1
            }
        }
    }

    private fun sendFilter(filter: String) {
        fragmentList.forEach { it.onFilterSelected(filter) }
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
}