package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.databinding.FragmentFriendRoommateFriendBinding
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRoommateFriendFragment : Fragment() {

    private var _binding: FragmentFriendRoommateFriendBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: String? = null
    private var targetUserId: Int = -1
    private var targetNickname: String? = null
    private var targetProfileUrl: String? = null
    private var myNickname: String? = null
    private var myProfileUrl: String? = null
    private var addedFriends: List<FriendProfileResult> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedDate     = arguments?.getString("selected_date")
        targetUserId     = arguments?.getInt("targetUserId", -1) ?: -1
        targetNickname   = arguments?.getString("targetNickname")
        targetProfileUrl = arguments?.getString("targetProfileUrl")
        myNickname       = arguments?.getString("myNickname")
        myProfileUrl     = arguments?.getString("myProfileUrl")
    }

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

        // 좌측(상대)
        binding.profileNicknameTv1.text = targetNickname ?: "상대"
        Glide.with(this)
            .load(targetProfileUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.profileIv1)

        // 우측(나)
        binding.profileNicknameTv2.text = myNickname ?: "나"
        Glide.with(this)
            .load(myProfileUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.profileIv2)

        // 카드 3: "나"
        binding.profileName1.text = myNickname ?: "나"
        Glide.with(this)
            .load(myProfileUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.profile1)

        // 카드 2: "상대"
        binding.profileName2.text = targetNickname ?: "상대"
        Glide.with(this)
            .load(targetProfileUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.profile2)

        // 친구 추가 버튼 클릭 시 프래그먼트 이동
        binding.addFriendBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("excludeUserId", targetUserId) // 매칭 대상 ID 전달
            }
            val fragment = FriendRoommateFriendAddFragment().apply {
                arguments = bundle
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }


        parentFragmentManager.setFragmentResultListener("selectedFriends", viewLifecycleOwner) { _, bundle ->
            val selectedFriends = bundle.getParcelableArrayList<FriendProfileResult>("friends") ?: emptyList()
            addedFriends = selectedFriends
        }


        //FriendRoommateTimeFragment 로 이동
        binding.matchBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selected_date", selectedDate)
                putString("myNickname", myNickname)
                putString("myProfileUrl", myProfileUrl)
                putString("targetNickname", targetNickname)
                putString("targetProfileUrl", targetProfileUrl)
                putParcelableArrayList("addedFriends", ArrayList(addedFriends))
            }

            val fragment = FriendRoommateTimeFragment().apply { arguments = bundle }
            parentFragmentManager.beginTransaction()
                .replace((view.parent as ViewGroup).id, fragment)
                .addToBackStack(null)
                .commit()
        }


        // 뒤로가기 버튼 처리
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // TODO: 이곳에 추가 로직 구현
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
