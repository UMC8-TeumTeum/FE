package com.umc.teumteum.ui.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumReceivedItem
import com.umc.teumteum.databinding.FragmentFriend02ResponseBinding
import com.umc.teumteum.ui.friend.adapter.FriendResponseCardAdapter
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.drawable.toDrawable

@AndroidEntryPoint
class Friend02ResponseFragment  : Fragment() {

    private var _binding: FragmentFriend02ResponseBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendResponseCardAdapter
    private var teumItem: TeumReceivedItem? = null

    var teumList: List<TeumReceivedItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02ResponseBinding.inflate(inflater, container, false)
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

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .commit()
        }

        // 설정 버튼 클릭 시 팝업 띄우기
        binding.settingBtn.setOnClickListener { anchorView ->
            showOptionsPopup(anchorView)
        }

        // 넘겨받은 아이템
        teumItem = arguments?.getParcelable("teumItem")

        teumItem?.let {
            adapter = FriendResponseCardAdapter(listOf(it))
            binding.requestViewPager.adapter = adapter
            binding.dotsIndicator.setViewPager2(binding.requestViewPager)

            teumList = listOf(it)
        }

        // 수락 버튼
        binding.btnAccept.setOnClickListener {
            teumItem?.responseId?.let { responseId ->
                val bottomSheet = Friend02AcceptBottomSheetFragment.newInstance(responseId)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }

        // 거절 버튼
        binding.btnReject.setOnClickListener {
            teumItem?.responseId?.let { responseId ->
                val bottomSheet = Friend02RejectBottomSheetFragment.newInstance(responseId)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }
    }

    // 사용자 프로필 신고 팝업 표시
    private fun showOptionsPopup(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_friend_profile, null)
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
            anchorView.width - widthPx, // 오른쪽 정렬
            6                            // 바로 아래
        )

        // 클릭 리스너 설정
        popupView.findViewById<View>(R.id.btn_profile).setOnClickListener {
            popupWindow.dismiss()

            val current =
                teumList.getOrNull(binding.requestViewPager.currentItem)
                    ?: return@setOnClickListener

            val fragment = FriendProfileFollowingFragment().apply {
                arguments = Bundle().apply {
                    // 요청 보낸 사람 id
                    putInt("userId", current.senderUser.userId)
                    putString("imageUrl", current.senderUser.profileImageUrl)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

        popupView.findViewById<View>(R.id.btn_report).setOnClickListener {
            popupWindow.dismiss()

            val current = teumList.getOrNull(binding.requestViewPager.currentItem)
                ?: return@setOnClickListener

            // 신고 대상: 받은 틈 요청 자체
            val targetType = "TEUM_REQUEST"
            val targetId = current.requestId.toLong()

            FriendReportChoiceBottomSheet
                .newInstance(targetType, targetId)
                .show(parentFragmentManager, "FriendReportChoiceBottomSheet")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(teumList: ArrayList<TeumReceivedItem>): Friend02ResponseFragment {
            return Friend02ResponseFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", teumList)
                }
            }
        }
    }
}