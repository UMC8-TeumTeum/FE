package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendProfileFollowingBinding
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

        (activity as? MainActivity)?.hideBottomBar()

        // 전달받은 프로필 정보
        val name = arguments?.getString("name") ?: ""
        val field = arguments?.getString("field") ?: ""
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        binding.profileNicknameTv.text = name
        binding.profileFieldTv.text = field

        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .into(binding.profileIv)

        // 뒤로가기 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            // 이전 프래그먼트로 돌아가기
            parentFragmentManager.popBackStack()
            // bottomNav 다시 보여주기
            (activity as? MainActivity)?.showBottomBar()
        }

        // TODO: 팔로잉 리스트 로직 추가
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
