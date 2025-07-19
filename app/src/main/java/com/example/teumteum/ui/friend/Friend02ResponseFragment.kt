package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriend02ResponseBinding
import com.example.teumteum.ui.main.MainActivity
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class Friend02ResponseFragment  : Fragment(){

    private var _binding: FragmentFriend02ResponseBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendResponseCardAdapter

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

        //  바텀 네비게이션 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        //  뒤로가기 버튼 처리
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }

        // 1. ViewPager2 + Adapter 연결
        adapter = FriendResponseCardAdapter(getDummyList())
        binding.requestViewPager.adapter = adapter

        // 2. DotsIndicator 연결
        val dotsIndicator: DotsIndicator = binding.dotsIndicator
        dotsIndicator.setViewPager2(binding.requestViewPager)

        // 3. 버튼 클릭 리스너
        // 이때는 시간이 안돼요 버튼 클릭 시
        binding.btnReject.setOnClickListener {
            // 바텀시트 띄우기
            val bottomSheet = Friend02RejectBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)

            Toast.makeText(requireContext(), "거절 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show()
        }


        // 함께할래요 버튼 클릭 시 바텀시트 띄우기 + Toast 메시지
        binding.btnAccept.setOnClickListener {
            // 바텀시트 띄우기
            val bottomSheet = Friend02AcceptBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)

            // Toast 메시지
            Toast.makeText(requireContext(), "함께할래요 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 더미 데이터 메서드
    private fun getDummyList(): List<FriendResponseData> = listOf(
        FriendResponseData(
            name = "최아연",
            date = "25.06.05",
            time = "15:20 ~ 16:10",
            title = "강아지 산책 가자",
            desc = "모모랑 초코랑 종합천 한바퀴 쓰윽 돌고 돌아오는 길에 호떡 먹자!",
            suggestionTime = "16:00 ~ 18:00"
        ),
        FriendResponseData(
            name = "보보",
            date = "25.06.06",
            time = "11:00 ~ 12:00",
            title = "산책 좋아하는 보보",
            desc = "동물병원 들렀다가 간식도 먹고 돌아오자!",
            suggestionTime = "11:00 ~ 12:00"
        )
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}