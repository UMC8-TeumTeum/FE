package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.data.remote.friend.model.PublicTodoResult
import com.example.teumteum.databinding.FragmentFriendProfileFollowingBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

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

        //  빈틈 시간 조회
        if (targetUserId != -1) {
            viewModel.loadFriendTeumTime(targetUserId)
            viewModel.loadSharedTeumTime(targetUserId)   //  서로의(함께한) 시간 추가
        }

        //  빈틈 시간 옵저브
        viewModel.teumTimeText.observe(viewLifecycleOwner) {
            binding.profileTimerTv.text = it
        }

        // 서로의 빈틈(함께한) 시간
        viewModel.sharedTeumTimeText.observe(viewLifecycleOwner) { text ->
            binding.nicknameTv?.text = text
        }

        // 프로필/시간 조회 아래에 붙이기
        if (targetUserId != -1) {
            viewModel.fetchRecentPublicTodos(targetUserId)
        }

        // 뒤로가기 버튼 클릭 시
        binding.backBtn.setOnClickListener {
            // 이전 프래그먼트로 돌아가기
            parentFragmentManager.popBackStack()
            // bottomNav 다시 보여주기
            (activity as? MainActivity)?.showBottomBar()
        }

        // 팔로잉 버튼 → 언팔로우
        binding.modifyProfileBtn.setOnClickListener {
            if (targetUserId != -1) {
                viewModel.unfollowUser(targetUserId)
            }
        }

        // star_btn 클릭 처리
        binding.starBtn.setOnClickListener {
            if (targetUserId != -1) {
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


        // sendBtn 클릭 시 친구 저장 + FriendRoommateDateFragment로 이동
        binding.sendBtn.setOnClickListener {
            if (targetUserId != -1) {
                val dateFragment = FriendRoommateDateFragment().apply {
                    arguments = Bundle().apply {
                        putInt("targetUserId", targetUserId)
                        putString("targetNickname", binding.profileNicknameTv.text.toString())
                        putString("targetProfileUrl", arguments?.getString("imageUrl") ?: "")
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, dateFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 더보기 버튼 선택시
        binding.seeMoreTv.setOnClickListener {
            val nickname = binding.profileNicknameTv.text?.toString().orEmpty()

            val frag = FriendTodoListFragment().apply {
                arguments = Bundle().apply {
                    putString("nickname", nickname)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, frag)   // 컨테이너 id 프로젝트에 맞게 확인
                .addToBackStack(null)
                .commit()
        }


        observeViewModel()
    }

    private fun observeViewModel() {
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

        // favoriteMap 관찰해서 버튼 아이콘 바꾸기
        viewModel.favoriteMap.observe(viewLifecycleOwner) { map ->
            val isFav = map[targetUserId] ?: false
            binding.starBtn.setImageResource(
                if (isFav) R.drawable.friend_profile_fill_star else R.drawable.friend_profile_star
            )
        }

        //  최근 공개 투두 관찰
        viewModel.recentTodos.observe(viewLifecycleOwner) { list ->
            bindRecentTodos(list) // 아래 함수
        }

        // 에러 메시지
        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    //  화면 내에 추가
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
