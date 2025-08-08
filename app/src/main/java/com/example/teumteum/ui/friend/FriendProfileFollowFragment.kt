package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.databinding.FragmentFriendProfileFollowBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendProfileFollowFragment : Fragment() {

    private var _binding: FragmentFriendProfileFollowBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendProfileFollowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        val userId = arguments?.getInt("userId") ?: -1
        if (userId == -1) {
            Toast.makeText(requireContext(), "존재하지 않는 유저입니다.", Toast.LENGTH_SHORT).show()
            Log.e("FRIEND_PROFILE_FRAGMENT", "userId가 유효하지 않음")
            parentFragmentManager.popBackStack()
            return
        }

        // 프로필 정보 요청
        viewModel.getFriendProfile(userId)

        // 성공 시 프로필 바인딩
        viewModel.friendProfile.observe(viewLifecycleOwner) { result ->
            Log.d("FRIEND_PROFILE_FRAGMENT", "프로필 조회 성공")

            binding.profileNicknameTv.text = result.name
            binding.profileFieldTv.text = result.field

            Glide.with(requireContext())
                .load(result.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .into(binding.profileIv)

            binding.modifyProfileBtn.text = if (result.following) "팔로잉" else "팔로우"

            //  이미 팔로우 상태면 자동 이동
            if (result.following) {
                navigateToFollowing(result)
            }
        }

        // 팔로우 결과 처리
        viewModel.followMessage.observe(viewLifecycleOwner) { message ->
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//            Log.d("FOLLOW_RESULT", message)

            if (message.contains("성공") || message.contains("완료")) {
                binding.modifyProfileBtn.text = "팔로잉"

                val result = viewModel.friendProfile.value
                if (result != null) {
                    val bundle = Bundle().apply {
                        putInt("userId", userId)
                        putString("name", result.name)
                        putString("field", result.field)
                        putString("imageUrl", result.profileImageUrl)
                    }

                    val followingFragment = FriendProfileFollowingFragment().apply {
                        arguments = bundle
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, followingFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        // 실패 메시지 처리
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e("FRIEND_PROFILE_FRAGMENT", it)

                if (it.contains("자기 자신의 프로필") || it.contains("존재하지 않는 유저")) {
                    parentFragmentManager.popBackStack()
                    (activity as? MainActivity)?.showBottomBar()
                }
            }
        }

        // 뒤로가기 버튼
        binding.backBtn.setOnClickListener {
            // FriendFragment로 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .commit()

            (activity as? MainActivity)?.showBottomBar()
        }


        // 설정 버튼
        binding.settingBtn.setOnClickListener {
            // TODO: 설정 화면으로 이동 예정
        }

        // 팔로우 or 팔로잉 버튼 클릭 시
        binding.modifyProfileBtn.setOnClickListener {
            val currentText = binding.modifyProfileBtn.text.toString()
            val result = viewModel.friendProfile.value

            if (currentText == "팔로우") {
                viewModel.followUser(userId)
            } else {
                // 이미 팔로잉 상태면 바로 이동
                navigateToFollowing(result)
            }
        }
    }

    private fun navigateToFollowing(result: FriendProfileResult?) {
        if (result != null) {
            val bundle = Bundle().apply {
                putInt("userId", result.userId)
                putString("name", result.name)
                putString("field", result.field)
                putString("imageUrl", result.profileImageUrl)
                putBoolean("fromAutoNavigation", true)
            }

            val followingFragment = FriendProfileFollowingFragment().apply {
                arguments = bundle
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, followingFragment)
                //  뒤로가기 스택에 안 쌓음 → 바로 friendFragment로 돌아감
                .commit()
        } else {
            Toast.makeText(requireContext(), "프로필 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
