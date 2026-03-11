package com.umc.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumReceivedItem
import com.umc.teumteum.databinding.FragmentFriend02RequestBinding
import com.umc.teumteum.ui.friend.adapter.FriendRequestCardAdapter
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.graphics.drawable.toDrawable

@AndroidEntryPoint
class Friend02RequestFragment : Fragment() {

    private var _binding: FragmentFriend02RequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    private val viewModel: FriendViewModel by activityViewModels()

    var teumList: List<TeumReceivedItem> = emptyList()

    private var pendingAcceptResponseId: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02RequestBinding.inflate(inflater, container, false)
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

        val receivedList = viewModel.receivedTeums.value.orEmpty()

        // 1. 유효한 요청만 필터링 (시간 지난 것 제거)
        val validList = filterValidTeumRequests(receivedList)

        // 2. 재요청 카드 제외
        val originalRequests = validList.filter { !it.resend }

        // 3. 정렬
        val sortedList = sortTeumList(originalRequests)

        val selected = viewModel.selectedTeum.value

        // 4. 선택된 요청을 맨 앞으로
        teumList = if (selected != null) {
            reorderWithSelectedFirstById(sortedList, selected.requestId)
        } else {
            sortedList
        }

        // 5. 어댑터 연결
        adapter = FriendRequestCardAdapter(teumList)
        binding.requestViewPager.adapter = adapter

        binding.requestViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val current = teumList.getOrNull(position) ?: return
                viewModel.selectTeum(current)
            }
        })

        // 6. 인디케이터
        binding.dotsIndicator.setViewPager2(binding.requestViewPager)

        viewModel.todoConflict.observe(viewLifecycleOwner) { response ->
            val responseId = pendingAcceptResponseId ?: return@observe
            pendingAcceptResponseId = null

            if (response == null) return@observe

            if (response.hasConflict) {
                // 겹침 있음 → 다른 바텀시트
                val conflictList = ArrayList(response.conflictingSchedules)
                val bottomSheet = FriendTodoBottomSheetFragment.newInstance(responseId, conflictList)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            } else {
                // 겹침 없음 → 기존 수락 바텀시트
                val bottomSheet = Friend02AcceptBottomSheetFragment.newInstance(responseId)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }

        // 7. 버튼 이벤트
        binding.btnAccept.setOnClickListener {
            val currentItem = binding.requestViewPager.currentItem
            val responseId = teumList.getOrNull(currentItem)?.responseId ?: return@setOnClickListener

            val teum = teumList.getOrNull(currentItem)
            val date = teum?.date.toString()
            val start = teum?.timeSlot?.start.toString()
            val end = teum?.timeSlot?.end.toString()

            pendingAcceptResponseId = responseId
            viewModel.checkTodoConflict(date, start, end)
        }

        binding.btnReject.setOnClickListener {
            val currentItem = binding.requestViewPager.currentItem
            val responseId = teumList.getOrNull(currentItem)?.responseId ?: return@setOnClickListener

            val bottomSheet = Friend02RejectBottomSheetFragment.newInstance(responseId)
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        // 8. 뒤로가기
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }

        // 설정 버튼 클릭 시 팝업 띄우기
        binding.settingBtn.setOnClickListener { anchorView ->
            showOptionsPopup(anchorView)
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

            val current = teumList.getOrNull(binding.requestViewPager.currentItem) ?: return@setOnClickListener

            // 신고 대상: 받은 틈 요청 자체
            val targetType = "TEUM_REQUEST"
            val targetId = current.requestId.toLong()

            FriendReportChoiceBottomSheet
                .newInstance(targetType, targetId)
                .show(parentFragmentManager, "FriendReportChoiceBottomSheet")
        }
    }

    // 미확인 → 최신순 정렬
    private fun sortTeumList(teumList: List<TeumReceivedItem>): List<TeumReceivedItem> {
        return teumList.sortedWith(
            compareBy<TeumReceivedItem> { it.read }      // false(미확인) 먼저
                .thenByDescending { it.requestId }       // 최신순
        )
    }

    // 선택된 요청을 가장 앞으로
    private fun reorderWithSelectedFirstById(
        sortedList: List<TeumReceivedItem>,
        selectedRequestId: Int
    ): List<TeumReceivedItem> {
        val selected = sortedList.firstOrNull { it.requestId == selectedRequestId }
            ?: return sortedList
        return listOf(selected) + sortedList.filter { it.requestId != selectedRequestId }
    }

    // 시간이 지나지 않은 요청 필터링
    private fun filterValidTeumRequests(list: List<TeumReceivedItem>): List<TeumReceivedItem> {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return list.filter { item ->
            try {
                val endDateTime = LocalDateTime.parse("${item.date} ${item.timeSlot.end}", formatter)
                endDateTime.isAfter(now)
            } catch (_: Exception) { false }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(teumList: ArrayList<TeumReceivedItem>): Friend02RequestFragment {
            return Friend02RequestFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", teumList)
                }
            }
        }
    }
}
