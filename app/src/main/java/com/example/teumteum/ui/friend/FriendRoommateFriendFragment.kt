package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateFriendBinding
import com.example.teumteum.ui.main.MainActivity

class FriendRoommateFriendFragment : Fragment() {

    private var _binding: FragmentFriendRoommateFriendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRoommateFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedDate = arguments?.getString("selected_date")
        binding.dateView.text = selectedDate ?: "날짜 없음"

        // 하단 바 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        // 친구 추가 버튼 클릭 시 프래그먼트 이동
        binding.addFriendBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateFriendAddFragment())  // 메인 프레임에 추가 프래그먼트 붙이기
                .addToBackStack(null) // 뒤로가기 가능하게
                .commit()
        }

        //나중에 수정 필요
        binding.matchBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateMatchingDetailFragment())
                .addToBackStack(null)  // 뒤로가기 버튼으로 돌아올 수 있게
                .commit()
        }


        // 뒤로가기 버튼 처리
        binding.btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateDateFragment())
                .addToBackStack(null)
                .commit()
        }

        // TODO: 이곳에 추가 로직 구현
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
