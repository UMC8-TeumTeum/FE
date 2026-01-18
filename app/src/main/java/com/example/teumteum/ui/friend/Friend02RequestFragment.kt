package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02RequestBinding
import com.example.teumteum.ui.friend.adapter.FriendRequestCardAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.myhome.MyProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        val receivedList = viewModel.receivedTeums.value.orEmpty()
        val selected = viewModel.selectedTeum.value

        // 1. 유효한 요청만 필터링 (시간 지난 것 제거)
        val validList = filterValidTeumRequests(receivedList)

        // 2. 재요청 카드 제외
        val originalRequests = validList.filter { !it.resend }

        // 3. 정렬
        val sortedList = sortTeumList(originalRequests)

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
                viewModel.selectTeum(current)   //선택된 아이템 갱신
                if(current.read == false){  //읽은 상태가 아니면
                    viewModel.readTeumRequest(current.responseId)   //읽기 요청
                }
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

        val xPos = (210 * density).toInt() // Left
        val yPos = (99 * density).toInt()  // Top

        popupWindow.showAtLocation(anchorView, Gravity.TOP or Gravity.START, xPos, yPos)

        // 클릭 리스너 설정
        popupView.findViewById<View>(R.id.btn_profile).setOnClickListener {
            popupWindow.dismiss()
            val targetFragment = MyProfileFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, targetFragment)
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

    //  미확인 → 최신순 정렬
    private fun sortTeumList(teumList: List<TeumReceivedItem>): List<TeumReceivedItem> {
        return teumList.sortedWith(
            compareBy<TeumReceivedItem> { it.read }      // false(미확인) 먼저
                .thenByDescending { it.requestId }       // 최신순
        )
    }

    //  선택된 요청을 가장 앞으로
    private fun reorderWithSelectedFirstById(
        sortedList: List<TeumReceivedItem>,
        selectedRequestId: Int
    ): List<TeumReceivedItem> {
        val selected = sortedList.firstOrNull { it.requestId == selectedRequestId }
            ?: return sortedList
        return listOf(selected) + sortedList.filter { it.requestId != selectedRequestId }
    }

    //  시간이 지나지 않은 요청 필터링
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
