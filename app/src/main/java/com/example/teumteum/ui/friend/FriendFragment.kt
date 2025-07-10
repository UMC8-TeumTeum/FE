package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendBinding

class FriendFragment : Fragment() {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 추천 카드 RecyclerView 설정
        val recommendAdapter = RecommendAdapter { // 카드 클릭 시
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02RequestFragment()) // 프래그먼트 교체
                .addToBackStack(null)
                .commit()
        }

        binding.recommendRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendRecyclerView.adapter = recommendAdapter

        binding.btnSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend01SearchFragment()) // container는 main_container로 되어 있어야 함
                .addToBackStack(null) // 뒤로 가기 가능하도록
                .commit()
        }


        val dummyFollowingList = listOf(
            FollowUser("문혜원", "UX 디자이너"),
            FollowUser("하수연", "UX 디자이너"),
            FollowUser("장채미", "UX 디자이너"),
            FollowUser("이솔민", "UX 디자이너"),
            FollowUser("이솔", "UX 디자이너"),
            FollowUser("이솔", "UX 디자이너"),
            FollowUser("이솔", "UX 디자이너")

        )

        val followingAdapter = FollowingAdapter(dummyFollowingList)
        binding.followingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.followingRecyclerView.adapter = followingAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}