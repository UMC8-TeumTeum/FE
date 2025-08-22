package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02RequestBinding
import com.example.teumteum.ui.friend.adapter.FriendRequestCardAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.getValue

@AndroidEntryPoint
class Friend02RequestFragment : Fragment() {

    private var _binding: FragmentFriend02RequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    private val viewModel: FriendViewModel by activityViewModels()

    var teumList: List<TeumReceivedItem> = emptyList()

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

        // 7. 버튼 이벤트
        binding.btnAccept.setOnClickListener {
            val currentItem = binding.requestViewPager.currentItem
            val responseId = teumList.getOrNull(currentItem)?.responseId ?: return@setOnClickListener

            val bottomSheet = Friend02AcceptBottomSheetFragment.newInstance(responseId)
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
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
