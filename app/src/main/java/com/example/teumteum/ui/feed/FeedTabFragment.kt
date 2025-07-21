package com.example.teumteum.ui.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFeedTabBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FeedTabFragment : Fragment() {

    private lateinit var binding: FragmentFeedTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedTabBinding.inflate(inflater, container, false)

        val types = listOf("latest", "following")
        val tabTitles = listOf("최신", "팔로잉")

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = types.size
            override fun createFragment(position: Int): Fragment {
                return ContentListFragment.newInstance(types[position])
            }
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val customView = LayoutInflater.from(requireContext()).inflate(R.layout.feed_custom_tab, null)
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

        // 초기 선택된 탭 적용
        binding.tabLayout.getTabAt(binding.viewPager.currentItem)?.let {
            updateTabStyle(it, true)
        }

        return binding.root
    }


    private fun updateTabStyle(tab: TabLayout.Tab, isSelected: Boolean) {
        val tabIndicator = tab.customView?.findViewById<View>(R.id.tab_indicator)
        val tabText = tab.customView?.findViewById<TextView>(R.id.tab_text)

        if (isSelected) {
            tabIndicator?.visibility = View.VISIBLE
            tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        } else {
            tabIndicator?.visibility = View.INVISIBLE
            tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.teumteum_deactive))
        }
    }
}