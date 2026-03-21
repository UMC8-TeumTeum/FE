package com.umc.teumteum.ui.friend

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentFriend01SearchBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt

@AndroidEntryPoint
class Friend01SearchFragment : Fragment() {

    private var _binding: FragmentFriend01SearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend01SearchBinding.inflate(inflater, container, false)

        // 처음 진입 시 검색어 초기화
        viewModel.currentSearchKeyword.value = null

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        viewModel.recentKeywords.observe(viewLifecycleOwner) { keywords ->
            updateSearchList(keywords)
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }

        // 최근 검색어 하나 삭제
        binding.btnDeleteRecent.setOnClickListener {
            viewModel.removeLastKeyword()
        }

        // 검색 엔터 입력 시
        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            val isSearchAction =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE

            val isEnterKey =
                event?.action == KeyEvent.ACTION_DOWN &&
                        event.keyCode == KeyEvent.KEYCODE_ENTER

            if (isSearchAction || isEnterKey) {
                handleSearchAction()
                true
            } else {
                false
            }
        }

        viewModel.currentSearchKeyword.observe(viewLifecycleOwner) { keyword ->
            if (!keyword.isNullOrEmpty()) {
                binding.searchEditText.setText(keyword)
                binding.searchEditText.setSelection(keyword.length)
            }
        }
    }

    private fun handleSearchAction() {
        val keyword = binding.searchEditText.text.toString().trim()
        if (keyword.isEmpty()) return

        hideKeyboard(binding.searchEditText)

        // 검색어 ViewModel에 저장
        viewModel.currentSearchKeyword.value = keyword

        viewModel.addRecentKeyword(keyword)

        val fragment = Friend01SearchResultFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun hideKeyboard(view: View) {
        val imm =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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
                setTextColor("#0F0F0F".toColorInt())
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
                setBackgroundColor("#EAEAEA".toColorInt())
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

    override fun onResume() {
        super.onResume()

        // 검색 결과 화면에서 돌아온 경우 검색창 비우기
        if (viewModel.currentSearchKeyword.value == null) {
            binding.searchEditText.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
