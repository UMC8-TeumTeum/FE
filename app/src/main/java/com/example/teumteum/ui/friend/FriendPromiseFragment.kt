package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentFriendPromiseBinding
import com.example.teumteum.ui.main.MainActivity

class FriendPromiseFragment : Fragment() {

    private var _binding: FragmentFriendPromiseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendPromiseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottomNav 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        with(binding) {
            // 뒤로가기
            btnBack.setOnClickListener {
                requireActivity().onBackPressed()
            }

            // TODO: llMonthNav에 이전/다음 달 버튼 추가 & 리스너 연결

            // 초기 이벤트 카드 세팅 (예시)
            cardEvent.visibility  = View.VISIBLE
            btnEventStartTime.text      = "17:30"
            btnEventEndTime.text   = "18:30"
            tvEventDesc.text      = "홍대 소품샵 투어"

            // 날짜 선택 리스너
            calendarView1.setOnDateChangeListener { _, year, month, dayOfMonth ->
                Toast
                    .makeText(
                        requireContext(),
                        "${year}년 ${month + 1}월 ${dayOfMonth}일 선택됨",
                        Toast.LENGTH_SHORT
                    )
                    .show()

                // BottomSheet 띄우기
                PromiseDetailBottomSheet.newInstance(year, month, dayOfMonth)
                    .show(parentFragmentManager, PromiseDetailBottomSheet.TAG)

                // TODO: 선택된 날짜에 맞는 약속 데이터를 로드해서
                //       tvEventTime / tvEventEndTime / tvEventDesc 에 반영
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //  프래그먼트 종료 시 바텀네비 다시 보이기
        (activity as? MainActivity)?.showBottomBar()
        _binding = null
    }
}
