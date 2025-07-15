package com.example.teumteum.ui.friend

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriend02PossibleTimeBinding
import com.example.teumteum.ui.main.MainActivity
import java.util.*

class Friend02PossibleTimeFragment : Fragment() {

    private var _binding: FragmentFriend02PossibleTimeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02PossibleTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  바텀 네비게이션 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        // 1. ViewPager2 + Adapter 연결
        adapter = FriendRequestCardAdapter(getDummyList())
        binding.requestViewPager.adapter = adapter

        // 함께할래요 버튼 클릭 시 바텀시트 띄우기 + Toast 메시지
        binding.btnFind.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02SuggestFragment())
                .addToBackStack(null)      // 뒤로 가기 가능하게
                .commit()

            Toast.makeText(requireContext(),
                "함께할래요 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }


    }


    private fun getDummyList(): List<FriendRequestData> {
        return listOf(
            FriendRequestData(
                name = "이름",
                date = "25.05.02",
                time = "15:20 ~ 16:10",
                title = "강아지 산책 가자",
                desc = "모모랑 초코랑 종합천 한바퀴 쓰윽 돌고\n돌아오는 길에 호떡 먹자!"
            ),
            FriendRequestData(
                name = "보보",
                date = "25.05.03",
                time = "11:00 ~ 12:00",
                title = "산책 좋아하는 보보",
                desc = "동물병원 들렀다가 간식도 먹고 돌아오자!"
            )
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
