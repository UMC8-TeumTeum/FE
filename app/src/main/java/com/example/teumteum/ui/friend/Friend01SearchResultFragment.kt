package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.FriendSearchResult
import com.example.teumteum.data.remote.friend.dto.FriendSearchService
import com.example.teumteum.databinding.FragmentFriend01SearchResultBinding
import com.example.teumteum.ui.friend.adapter.SearchResultAdapter
import com.example.teumteum.ui.friend.view.FriendSearchView
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Friend01SearchResultFragment : Fragment(), FriendSearchView {

    private var _binding: FragmentFriend01SearchResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchResultAdapter

    @Inject
    lateinit var service: FriendSearchService

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

        (activity as? MainActivity)?.hideBottomBar()

        val keyword = arguments?.getString("searchKeyword") ?: return

        // RecyclerView 초기화
        adapter = SearchResultAdapter(emptyList()) { userId ->
            navigateToProfile(userId)
        }

        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecyclerView.adapter = adapter

        // 검색 서비스 실행
        service.setFriendSearchView(this)
        service.searchUser(keyword)

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onSearchSuccess(result: List<FriendSearchResult>) {
        if (result.isEmpty()) {
            val msg = "사용자 검색 실패 (code: USER4040, message: 존재하지 않는 사용자입니다.)"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            Log.e("SEARCH_RESULT_FRAGMENT", msg)
            return
        }

        val msg = "사용자 조회 성공 (code: USER2001)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("SEARCH_RESULT_FRAGMENT", msg)

        adapter = SearchResultAdapter(result) { userId ->
            navigateToProfile(userId)
        }
        binding.searchResultRecyclerView.adapter = adapter
    }

    override fun onSearchFailure(code: String, message: String) {
        val errorMsg = when (code) {
            "USER4040" -> "사용자 검색 실패 (code: $code, message: 존재하지 않는 사용자입니다.)"
            "NETWORK_ERROR" -> "사용자 검색 실패 (code: $code, message: 네트워크 오류)"
            else -> "사용자 검색 실패 (code: $code, message: $message)"
        }

        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        Log.e("SEARCH_RESULT_FRAGMENT", errorMsg)
    }


    // 프로필 선택 시 FriendProfileFollowFragment로 이동
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
