package com.example.teumteum.Friend

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriend01SearchBinding

class Friend01SearchFragment : Fragment() {

    private var _binding: FragmentFriend01SearchBinding? = null
    private val binding get() = _binding!!

    private val recentKeywords = mutableListOf("미나리", "애플", "벨라")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend01SearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSearchList()

        //  뒤로가기 버튼 → FriendFragment로 이동
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, FriendFragment())
                .addToBackStack(null)
                .commit()
        }

        //  삭제 버튼 → 최근 검색어 하나씩 제거
        binding.btnDeleteRecent.setOnClickListener {
            if (recentKeywords.isNotEmpty()) {
                recentKeywords.removeAt(recentKeywords.size - 1)
                updateSearchList()
            }
        }

        // 🔍 키보드 검색(Enter) 시 검색어 추가
        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey = event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER

            if (isSearchAction || isEnterKey) {
                val keyword = binding.searchEditText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    recentKeywords.add(keyword)
                    binding.searchEditText.text.clear()
                    updateSearchList()
                }
                true
            } else {
                false
            }
        }
    }

    private fun updateSearchList() {
        binding.recentSearchList.removeAllViews()

        for ((index, keyword) in recentKeywords.withIndex()) {
            val textView = TextView(requireContext()).apply {
                text = keyword
                textSize = 16f
                setPadding(0, 12, 0, 12)
                setTextColor(Color.parseColor("#0F0F0F"))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.noto_sans_kr_medium)
            }

            binding.recentSearchList.addView(textView)

            // 마지막 키워드 뒤에는 divider 추가하지 않음
            if (index < recentKeywords.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                    )
                    setBackgroundColor(Color.parseColor("#EAEAEA"))
                }
                binding.recentSearchList.addView(divider)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
