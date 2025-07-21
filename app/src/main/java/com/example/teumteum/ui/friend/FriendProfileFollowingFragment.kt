package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentFriendProfileFollowingBinding
import com.example.teumteum.ui.main.MainActivity

class FriendProfileFollowingFragment : Fragment() {

    private var _binding: FragmentFriendProfileFollowingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendProfileFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottomNav 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        // 뒤로가기 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            // 이전 프래그먼트로 돌아가기
            parentFragmentManager.popBackStack()
            // bottomNav 다시 보여주기
            (activity as? MainActivity)?.showBottomBar()
        }

        // TODO: 이곳에 팔로잉 리스트나 기타 UI 초기화 로직을 추가하세요.
        // 예:
        // binding.followingRecyclerView.apply {
        //   adapter = YourFollowingListAdapter(...)
        //   layoutManager = LinearLayoutManager(requireContext())
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
