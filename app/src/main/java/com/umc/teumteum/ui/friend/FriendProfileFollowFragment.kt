package com.umc.teumteum.ui.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.FriendProfileResult
import com.umc.teumteum.data.remote.friend.model.TeumTimeResult
import com.umc.teumteum.databinding.FragmentFriendProfileFollowBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendProfileFollowFragment : Fragment() {

    private var _binding: FragmentFriendProfileFollowBinding? = null
    private val binding get() = _binding!!

    private var targetUserId: Int = -1
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

        // 네비게이션 바
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, bottomInset)
            insets
        }

        targetUserId = arguments?.getInt("userId") ?: -1
        val userId = targetUserId


        // 프로필 정보 요청
        viewModel.getFriendProfile(userId) { profile ->
            binding.profileNicknameTv.text = profile.name
            binding.profileFieldTv.text = profile.field

            Glide.with(requireContext())
                .load(profile.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .into(binding.profileIv)

            binding.modifyProfileBtn.text = if (profile.following) "팔로잉" else "팔로우"
        }

        // 빈틈 시간 조회
        viewModel.loadFriendTeumTime(userId)
        viewModel.teumTimeText.observe(viewLifecycleOwner) {
            binding.profileTimerTv.text = it
        }

        // 최근 공개 투두 조회
        viewModel.fetchRecentPublicTodos(userId)

        // 옵저버 등록
        observeViewModel()

        // 뒤로가기 버튼
        binding.backBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .commit()
            (activity as? MainActivity)?.showBottomBar()
        }

        // 설정 버튼 클릭 시 팝업 띄우기
        binding.settingBtn.setOnClickListener { anchorView ->
            showOptionsPopup(anchorView)
        }

        // 팔로우/팔로잉 버튼 클릭
        binding.modifyProfileBtn.setOnClickListener {
            val currentText = binding.modifyProfileBtn.text.toString()
            val result = viewModel.friendProfile.value

            if (currentText == "팔로우") {
                viewModel.followUser(userId)
            } else {
                navigateToFollowing(result)
            }
        }
    }

    // 차단 신고 팝업 표시
    private fun showOptionsPopup(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_friend_options, null)
        val density = resources.displayMetrics.density

        val widthPx = (160 * density).toInt()
        val heightPx = ViewGroup.LayoutParams.WRAP_CONTENT

        val popupWindow = PopupWindow(
            popupView,
            widthPx,
            heightPx,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 0f


        // anchorView 기준으로 위치 계산
        popupWindow.showAsDropDown(
            anchorView,
            anchorView.width - widthPx, // 오른쪽 정렬
            6                            // 바로 아래
        )

        // 차단 버튼 클릭
        popupView.findViewById<View>(R.id.btn_block).setOnClickListener {
            popupWindow.dismiss()

            if (targetUserId == -1) {
                Log.e("FriendProfile", "차단 실패: targetUserId 없음")
                return@setOnClickListener
            }

            val userName = binding.profileNicknameTv.text.toString()

            FriendBlockBottomSheet
                .newInstance(targetUserId, userName)
                .show(parentFragmentManager, "FriendBlockBottomSheet")
        }

        // 신고 버튼
        popupView.findViewById<View>(R.id.btn_report).setOnClickListener {
            popupWindow.dismiss()

            if (targetUserId == -1) return@setOnClickListener

            FriendReportChoiceBottomSheet
                .newInstance("USER", targetUserId.toLong())
                .show(parentFragmentManager, "FriendReportChoiceBottomSheet")
        }
    }

    private fun observeViewModel() {
        // 프로필 LiveData
        viewModel.friendProfile.observe(viewLifecycleOwner) { result ->
            binding.profileNicknameTv.text = result.name
            binding.profileFieldTv.text = result.field

            Glide.with(requireContext())
                .load(result.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .into(binding.profileIv)

            binding.modifyProfileBtn.text = if (result.following) "팔로잉" else "팔로우"

            if (result.following) {
                viewModel.friendProfile.removeObservers(viewLifecycleOwner)
                navigateToFollowing(result)
            }
        }


        // 팔로우 성공 메시지
        viewModel.followMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                if (msg.contains("성공") || msg.contains("완료")) {
                    navigateToFollowing(viewModel.friendProfile.value!!)
                }
            }
        }

        // 에러 메시지
        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.e("FRIEND_PROFILE_FRAGMENT", msg)
                if (msg.contains("자기 자신의 프로필") || msg.contains("존재하지 않는 유저")) {
                    parentFragmentManager.popBackStack()
                    (activity as? MainActivity)?.showBottomBar()
                }
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
                .commit()
        }
    }

    private fun TeumTimeResult.toKoreanDuration(): String {
        val parts = buildList {
            if (days > 0) add("${days}일")
            if (hours > 0) add("${hours}시간")
            if (minutes > 0) add("${minutes}분")
        }
        return if (parts.isEmpty()) "0분" else parts.joinToString(" ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
