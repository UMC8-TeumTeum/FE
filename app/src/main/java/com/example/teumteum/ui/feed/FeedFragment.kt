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
import com.example.teumteum.databinding.FragmentFeedBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private val tabTitles = listOf("최신", "팔로잉")
    private val tabTypes = listOf("latest", "following")
    private val fragmentList = mutableListOf<FeedContentFragment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTabs()
        setupFilterButtons()
    }

    //탭 연결
    private fun setupTabs() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabTypes.size
            override fun createFragment(position: Int): Fragment {
                val fragment = FeedContentFragment.newInstance(tabTypes[position])
                if (fragmentList.size <= position) {
                    fragmentList.add(fragment)
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
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabStyle(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // 초기 스타일 적용
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

    //시간 필터 버튼 초기세팅
    private fun setupFilterButtons() {
        val filterButtons = listOf(
            binding.btnAll to "all",
            binding.btn10m to "10m",
            binding.btn20m to "20m",
            binding.btn30m to "30m"
        )

        filterButtons.forEach { (button, filter) ->
            button.setOnClickListener {
                updateFilterButtonStates(button)
                sendFilter(filter)
            }
        }

        // 초기값 선택
        updateFilterButtonStates(binding.btnAll)
        sendFilter("all")

    }

    //시간 필터 버튼
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
        val current = binding.viewPager.currentItem
        if (current < fragmentList.size) {
            fragmentList[current].onFilterSelected(filter)
        }
    }
}