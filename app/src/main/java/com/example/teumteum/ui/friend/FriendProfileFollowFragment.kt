
package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendProfileFollowBinding
import com.example.teumteum.ui.main.MainActivity

class FriendProfileFollowFragment : Fragment() {

    private var _binding: FragmentFriendProfileFollowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // XML 레이아웃 바인딩
        _binding = FragmentFriendProfileFollowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottomNav 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        // 뒤로가기 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            // 1) 이전 프래그먼트로 팝
            parentFragmentManager.popBackStack()
            // 2) 바텀바 다시 보여주기
            (activity as? MainActivity)?.showBottomBar()
        }

        // “팔로우/언팔로잉” 버튼 클릭 시 → 프로필 팔로잉 화면으로 이동
        binding.modifyProfileBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.main_frm,                              // Activity의 FragmentContainerView ID
                    FriendProfileFollowingFragment()            // 이동할 Fragment
                )
                .addToBackStack(null)
                .commit()
        }

        // 설정 버튼 클릭 등 필요 콜백
        binding.settingBtn.setOnClickListener {
            // TODO: 설정 화면으로 이동
        }

        // 프로필 정보, 일정 리스트 바인딩 로직 등
        // e.g. binding.profileNicknameTv.text = user.name
        // binding.scheduleCardContainer.visibility = ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
