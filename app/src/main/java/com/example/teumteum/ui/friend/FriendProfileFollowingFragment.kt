package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.databinding.FragmentFriendProfileFollowingBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendProfileFollowingFragment : Fragment() {

    private var _binding: FragmentFriendProfileFollowingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private var targetUserId: Int = -1
    private var navigatedToFollow = false // 언팔로우 후 전환 중복 방지

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
        targetUserId = arguments?.getInt("userId") ?: -1

        binding.profileNicknameTv.text = name
        binding.profileFieldTv.text = field

        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .into(binding.profileIv)

        // 뒤로가기 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            val fromTab = arguments?.getString("fromTab", "following")
            val friendFragment = FriendFragment().apply {
                arguments = Bundle().apply {
                    putString("defaultTab", fromTab)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, friendFragment)
                .commit()

            (activity as? MainActivity)?.showBottomBar()
        }

        // 팔로잉 버튼 → 언팔로우
        binding.modifyProfileBtn.setOnClickListener {
            if (targetUserId != -1) {
                viewModel.unfollowUser(targetUserId)
            }
        }

        // star_btn 클릭 처리
        binding.starBtn.setOnClickListener {
            if (targetUserId != -1) {
                viewModel.toggleFavorite(targetUserId)
            }
        }

        // sendBtn 클릭 시 친구 저장 + FriendRoommateDateFragment로 이동
        binding.sendBtn.setOnClickListener {
            if (targetUserId != -1) {
                val dateFragment = FriendRoommateDateFragment().apply {
                    arguments = Bundle().apply {
                        putInt("targetUserId", targetUserId)
                        putString("targetNickname", binding.profileNicknameTv.text.toString())
                        putString("targetProfileUrl", arguments?.getString("imageUrl") ?: "")
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, dateFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }


        observeViewModel()
    }

    private fun observeViewModel() {
        // 언팔로우 결과 메시지
        viewModel.unfollowMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                if (!navigatedToFollow && msg.contains("성공적으로 완료")) {
                    navigatedToFollow = true

                    val followFragment = FriendProfileFollowFragment().apply {
                        arguments = Bundle().apply {
                            putInt("userId", targetUserId)
                            putString("name", binding.profileNicknameTv.text.toString())
                            putString("field", binding.profileFieldTv.text.toString())
                            putString("imageUrl", arguments?.getString("imageUrl") ?: "")
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, followFragment)
                        .commit()
                }
            }
        }

        // favoriteMap 관찰해서 버튼 아이콘 바꾸기
        viewModel.favoriteMap.observe(viewLifecycleOwner) { map ->
            val isFav = map[targetUserId] ?: false
            binding.starBtn.setImageResource(
                if (isFav) R.drawable.friend_profile_fill_star else R.drawable.friend_profile_star
            )
        }

        // 에러 메시지
        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
