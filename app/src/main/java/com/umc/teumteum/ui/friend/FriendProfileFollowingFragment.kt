package com.umc.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.PublicTodoResult
import com.umc.teumteum.databinding.FragmentFriendProfileFollowingBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable

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

        // 네비게이션 바
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, bottomInset)
            insets
        }

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

        if (targetUserId != -1) {
            viewModel.getFriendProfile(targetUserId)
            viewModel.loadFriendTeumTime(targetUserId)
            viewModel.loadSharedTeumTime(targetUserId)
            viewModel.fetchRecentPublicTodos(targetUserId)
        }

        viewModel.teumTimeText.observe(viewLifecycleOwner) {
            binding.profileTimerTv.text = it
        }

        // 서로의 빈틈(함께한) 시간
        viewModel.sharedTeumTimeText.observe(viewLifecycleOwner) { text ->
            binding.nicknameTv.text = text
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
            (activity as? MainActivity)?.showBottomBar()
        }

        // 설정 버튼 클릭 시 팝업 띄우기
        binding.settingBtn.setOnClickListener { anchorView ->
            showOptionsPopup(anchorView)
        }

        // 팔로잉 버튼 → 언팔로우
        binding.modifyProfileBtn.setOnClickListener {
            if (targetUserId != -1) {
                viewModel.unfollowUser(targetUserId)
            }
        }

        binding.starBtn.setOnClickListener {
            if (targetUserId != -1) {
                // viewModel 공용 토글 사용
                viewModel.toggleFavorite(targetUserId)
            }
        }

        // 친구 프로필_함께 한 시간 화면
        binding.arrowIv.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("targetUserId", targetUserId)
                putString("totalSharedTime", binding.nicknameTv.text.toString())
            }

            val fragment = SharedTeumTimeFragment().apply {
                arguments = bundle
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 친구 저장 + 프레그먼트 이동
        binding.sendBtn.setOnClickListener {
            if (targetUserId != -1) {
                val dateFragment = FriendRoommateDateFragment().apply {
                    arguments = Bundle().apply {
                        putInt("targetUserId", targetUserId)
                        putString("targetNickname", binding.profileNicknameTv.text.toString())
                        putString("targetProfileUrl", imageUrl)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, dateFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.seeMoreTv.setOnClickListener {
            val nickname = binding.profileNicknameTv.text?.toString().orEmpty()

            val frag = FriendTodoListFragment().apply {
                arguments = Bundle().apply {
                    putString("nickname", nickname)
                    putInt("userId", targetUserId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, frag)
                .addToBackStack(null)
                .commit()
        }
        observeViewModel()
    }

    // 신고/차단 팝업 표시
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

        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.elevation = 0f

        // anchorView 기준으로 위치 계산
        popupWindow.showAsDropDown(
            anchorView,
            anchorView.width - widthPx,
            20
        )

        popupView.findViewById<View>(R.id.btn_block).setOnClickListener {
            popupWindow.dismiss()

            if (targetUserId == -1) {
                Log.e("FriendProfile", "차단 실패: targetUserId 없음")
                return@setOnClickListener
            }

            val userName = binding.profileNicknameTv.text.toString()

            BottomSheetFriendBlockFragment
                .newInstance(targetUserId, userName)
                .show(parentFragmentManager, "BottomSheetFriendBlockFragment")
        }

        popupView.findViewById<View>(R.id.btn_report).setOnClickListener {
            popupWindow.dismiss()

            if (targetUserId == -1) return@setOnClickListener

            BottomSheetFriendReportChoiceFragment
                .newInstance("USER", targetUserId.toLong())
                .show(parentFragmentManager, "BottomSheetFriendReportChoiceFragment")
        }
    }

    private fun observeViewModel() {
        // 차단 성공 → 버튼 상태 변경
        viewModel.blockComplete.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    binding.modifyProfileBtn.text = "차단됨"
                    binding.modifyProfileBtn.isEnabled = false
                    binding.modifyProfileBtn.alpha = 0.5f

                    binding.modifyProfileBtn.setTextColor(requireContext().getColor(R.color.text_primary))

                    binding.starBtn.isEnabled = false
                    binding.sendBtn.isEnabled = false
                    binding.settingBtn.isEnabled = false
                }
            }
        }

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

        viewModel.friendProfile.observe(viewLifecycleOwner) { profile ->
            if (profile.userId == targetUserId) {
                binding.profileNicknameTv.text = profile.name
                binding.profileFieldTv.text = profile.field
                Glide.with(requireContext())
                    .load(profile.profileImageUrl)
                    .placeholder(R.drawable.gray_teum)
                    .error(R.drawable.gray_teum)
                    .into(binding.profileIv)
                updateStarIcon()
            }
        }

        viewModel.favoriteMap.observe(viewLifecycleOwner) {
            updateStarIcon()
        }

        viewModel.recentTodos.observe(viewLifecycleOwner) { list ->
            bindRecentTodos(list)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.d("FRIEND_PROFILE_FOLLOWING_FRAGMENT", msg)
            }
        }
    }

    // 최종 표시값 계산: 오버라이드 > 서버값 > 기본(false)
    private fun updateStarIcon() {
        val serverFav = viewModel.friendProfile.value
            ?.takeIf { it.userId == targetUserId }
            ?.favorite

        val overrideFav = viewModel.favoriteMap.value?.get(targetUserId)

        val displayFav = overrideFav ?: serverFav ?: false

        binding.starBtn.setImageResource(
            if (displayFav) R.drawable.friend_profile_fill_star
            else R.drawable.friend_profile_star
        )
    }

    private fun bindRecentTodos(list: List<PublicTodoResult>) {
        val l = list.take(2)

        // 컨테이너 보이기/숨기기
        binding.scheduleCardContainer.visibility = if (l.isNotEmpty()) View.VISIBLE else View.GONE

        if (l.isEmpty()) return

        // 첫 번째 카드
        val first = l[0]
        binding.schedule1TimeStartTv.text = first.startTime
        binding.schedule1TimeEndTv.text   = first.endTime
        binding.schedule1TitleTv.text     = first.title

        // 두 번째 카드
        if (l.size >= 2) {
            val second = l[1]
            binding.schedule2Cl.visibility = View.VISIBLE
            binding.schedule2TimeStartTv.text = second.startTime
            binding.schedule2TimeEndTv.text   = second.endTime
            binding.schedule2TitleTv.text     = second.title
        } else {
            binding.schedule2Cl.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
