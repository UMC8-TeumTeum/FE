package com.example.teumteum.ui.friend

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriend01SearchBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Friend01SearchFragment : Fragment() {

    private var _binding: FragmentFriend01SearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend01SearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        //  최근 검색어 목록 관찰
        viewModel.recentKeywords.observe(viewLifecycleOwner, Observer { keywords ->
            updateSearchList(keywords)
        })

        //  뒤로가기
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }

        //  최근 검색어 하나 삭제
        binding.btnDeleteRecent.setOnClickListener {
            viewModel.removeLastKeyword()
        }

        //  검색 엔터 입력 시
        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey = event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER

            if (isSearchAction || isEnterKey) {
                val keyword = binding.searchEditText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    viewModel.addRecentKeyword(keyword)
                    binding.searchEditText.text.clear()

                    // 👉 검색 수행 및 결과 프래그먼트로 이동
                    val bundle = Bundle().apply {
                        putString("searchKeyword", keyword)
                    }
                    val fragment = Friend01SearchResultFragment()
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                true
            } else {
                false
            }
        }
    }

    //  최근 검색어 리스트 업데이트
    private fun updateSearchList(keywords: List<String>) {
        applyRecentSearchEmptyState(keywords.isEmpty())
        binding.recentSearchList.removeAllViews()

        if (keywords.isEmpty()) return

        for (keyword in keywords) {
            val textView = TextView(requireContext()).apply {
                text = keyword
                textSize = 16f
                setPadding(0, 2, 0, 2)
                setTextColor(Color.parseColor("#0F0F0F"))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.noto_sans_kr_medium)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            binding.recentSearchList.addView(textView)

            val dividerHeightPx = (1.2 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dividerHeightPx
                ).apply {
                    topMargin = (0.5f * resources.displayMetrics.density).toInt()
                }
                setBackgroundColor(Color.parseColor("#EAEAEA"))
            }
            binding.recentSearchList.addView(divider)
        }
    }

    private fun applyRecentSearchEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recentSearchNotExistsCl.visibility = View.VISIBLE
            binding.recentSearchList.visibility = View.GONE
        } else {
            binding.recentSearchNotExistsCl.visibility = View.GONE
            binding.recentSearchList.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
