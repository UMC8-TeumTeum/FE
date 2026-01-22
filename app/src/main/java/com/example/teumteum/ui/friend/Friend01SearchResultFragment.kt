package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendSearchResult
import com.example.teumteum.databinding.FragmentFriend01SearchResultBinding
import com.example.teumteum.ui.friend.adapter.SearchResultAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Friend01SearchResultFragment : Fragment() {

    private var _binding: FragmentFriend01SearchResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchResultAdapter
    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend01SearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하단 바 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        // 리사이클러뷰 초기화
        adapter = SearchResultAdapter(emptyList()) { userId: Int ->
            navigateToProfile(userId)
        }

        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecyclerView.adapter = adapter

        // 검색 결과 관찰
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            val list = results.orEmpty()
            applySearchResultEmptyState(list.isEmpty())
            adapter.updateData(list)
        }

        // 메시지 (성공/실패) 관찰
        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            Log.d("SEARCH_RESULT_FRAGMENT", "성공: $msg")
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.e("SEARCH_RESULT_FRAGMENT", "오류: $msg")
                applySearchResultEmptyState(true)
            }
        }

        viewModel.currentSearchKeyword.observe(viewLifecycleOwner) { keyword ->
            if (!keyword.isNullOrEmpty()) {
                binding.searchEditText.setText(keyword)
                binding.searchEditText.setSelection(keyword.length)

                // 검색 API 호출
                viewModel.searchUser(keyword)
            }
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey =
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER

            if (isSearchAction || isEnterKey) {
                val keyword = binding.searchEditText.text.toString().trim()
                if (keyword.isNotEmpty()) {

                    // 검색어 저장
                    viewModel.currentSearchKeyword.value = keyword
                    viewModel.addRecentKeyword(keyword)

                    // 같은 Fragment에서 검색만 다시
                    viewModel.searchUser(keyword)
                }
                true
            } else false
        }

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            viewModel.currentSearchKeyword.value = null
            parentFragmentManager.popBackStack()
        }

        // 시스템 뒤로가기까지 커버
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 검색 결과 화면을 나갈 때 검색어 제거
                    viewModel.currentSearchKeyword.value = null
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    private fun navigateToProfile(userId: Int) {
        val fragment = FriendProfileFollowFragment().apply {
            arguments = Bundle().apply {
                putInt("userId", userId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun applySearchResultEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.searchResultNotExistsCl.visibility = View.VISIBLE
            binding.searchResultRecyclerView.visibility = View.GONE
        } else {
            binding.searchResultNotExistsCl.visibility = View.GONE
            binding.searchResultRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
