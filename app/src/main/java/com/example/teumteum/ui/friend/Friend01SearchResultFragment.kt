package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

//    private var toast: Toast? = null

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

        // 전달받은 검색 키워드로 유저 검색 요청
        val keyword = arguments?.getString("searchKeyword") ?: return
        viewModel.searchUser(keyword)

        // 리사이클러뷰 초기화
        adapter = SearchResultAdapter(emptyList()) { userId: Int ->
            navigateToProfile(userId)
        }

        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecyclerView.adapter = adapter

        // 검색 결과 관찰
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.updateData(results)
        }

        // 메시지 (성공/실패) 관찰
        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            Log.d("SEARCH_RESULT_FRAGMENT", "성공: $msg")
//            toast?.cancel() // 이전 토스트 제거
//            toast = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT)
//            toast?.show()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                // Toast로 띄우거나 Log 출력
                // Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.e("SEARCH_RESULT_FRAGMENT", "오류: $msg")
            }
        }

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
