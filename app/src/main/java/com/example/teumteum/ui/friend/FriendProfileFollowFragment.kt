package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendProfileFollowBinding
import com.example.teumteum.data.remote.friend.dto.FriendProfileResult
import com.example.teumteum.data.remote.friend.dto.FriendProfileService
import com.example.teumteum.ui.friend.view.FriendProfileView
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendProfileFollowFragment : Fragment(), FriendProfileView {

    private var _binding: FragmentFriendProfileFollowBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var friendProfileService: FriendProfileService

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

        // 1. userId 전달받기
        val userId = arguments?.getInt("userId") ?: -1
        if (userId == -1) {
            Toast.makeText(requireContext(), "존재하지 않는 유저입니다.", Toast.LENGTH_SHORT).show()
            Log.e("FRIEND_PROFILE_FRAGMENT", "userId가 유효하지 않음")
            parentFragmentManager.popBackStack()
            return
        }

        // 2. 서비스 설정 및 호출
        friendProfileService.setFriendProfileView(this)
        friendProfileService.getFriendProfile(userId)

        // 3. 뒤로가기
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
            (activity as? MainActivity)?.showBottomBar()
        }

        // 4. 프로필 팔로잉 화면 이동
        binding.modifyProfileBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendProfileFollowingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.settingBtn.setOnClickListener {
            // TODO: 설정 화면 이동
        }
    }

    // 5. FriendProfileView 인터페이스 구현
    override fun onFriendProfileSuccess(result: FriendProfileResult) {
        val msg = "친구 프로필 조회 성공 (code: FRIEND2002)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("FRIEND_PROFILE_FRAGMENT", msg)

        binding.profileNicknameTv.text = result.name
        binding.profileFieldTv.text = result.field

        Glide.with(requireContext())
            .load(result.profileImageUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .into(binding.profileIv)

        binding.modifyProfileBtn.text = if (result.following) "팔로잉" else "팔로우"
    }

    override fun onFriendProfileFailure(code: String, message: String) {
        when (code) {
            "FRIEND4002" -> {
                val msg = "자기 자신의 프로필은 조회할 수 없습니다. (code: $code)"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.e("FRIEND_PROFILE_FRAGMENT", msg)
                parentFragmentManager.popBackStack()
            }
            "FRIEND4040" -> {
                val msg = "존재하지 않는 유저입니다. (code: $code)"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.e("FRIEND_PROFILE_FRAGMENT", msg)
                parentFragmentManager.popBackStack()
            }
            else -> {
                val msg = "친구 프로필 조회 실패 (code: $code, message: $message)"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.e("FRIEND_PROFILE_FRAGMENT", msg)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
