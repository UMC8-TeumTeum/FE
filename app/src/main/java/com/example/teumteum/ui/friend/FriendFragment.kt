// FriendFragment.kt 수정본
package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriendBinding
import com.example.teumteum.ui.friend.adapter.FollowerAdapter
import com.example.teumteum.ui.friend.adapter.FollowingAdapter
import com.example.teumteum.ui.friend.adapter.RecommendAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendFragment : Fragment() {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    // ViewModel은 activityViewModels()로 공유
    private val viewModel: FriendViewModel by activityViewModels()

    private lateinit var recommendAdapter: RecommendAdapter
    private lateinit var followingAdapter: FollowingAdapter
    private lateinit var followerAdapter: FollowerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recommendAdapter = RecommendAdapter(
            onCardClick = { item: TeumReceivedItem, position: Int ->
                //틈 읽음 처리
                Log.d("CARD_CLICK", "카드 클릭됨, responseId=${item.responseId}")
                viewModel.readTeumRequest(item.responseId)

                val fragment = Friend02RequestFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList("teumList", ArrayList(viewModel.receivedTeums.value ?: emptyList()))
                        putInt("selectedPosition", position)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        followingAdapter = FollowingAdapter(
            data = emptyList(),
            onProfileClick = { user ->
                val fragment = FriendProfileFollowFragment().apply {
                    arguments = Bundle().apply { putInt("userId", user.userId) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onSendClick = { user ->
                val f = FriendRoommateDateFragment().apply {
                    arguments = Bundle().apply {
                        putInt("targetUserId", user.userId)
                        putString("targetNickname", user.nickname)
                        putString("targetProfileUrl", user.profileImageUrl)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, f)
                    .addToBackStack(null)
                    .commit()
            },
            onStarClick = { userId ->
                viewModel.toggleFavorite(userId)
            }
        )

        followerAdapter = FollowerAdapter(emptyList())

        // 리사이클러뷰 세팅
        binding.followingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = followingAdapter
        }

        binding.followerRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = followerAdapter
        }

        binding.recommendRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendAdapter
        }

        // 탭 클릭 리스너
        binding.tabFollowing.setOnClickListener {
            binding.tabFollowing.setTextColor(Color.parseColor("#0F0F0F"))
            binding.tabFollower.setTextColor(Color.parseColor("#B1B2B3"))
            binding.followingRecyclerView.visibility = View.VISIBLE
            binding.followerRecyclerView.visibility = View.GONE

            // 팔로잉 목록 조회
            viewModel.getFollowingUsers()
        }

        binding.tabFollower.setOnClickListener {
            binding.tabFollowing.setTextColor(Color.parseColor("#B1B2B3"))
            binding.tabFollower.setTextColor(Color.parseColor("#0F0F0F"))
            binding.followingRecyclerView.visibility = View.GONE
            binding.followerRecyclerView.visibility = View.VISIBLE

            // 팔로워 목록 조회
            viewModel.getFollowerUsers()
        }

        binding.btnAlarm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendTeumRequestFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend01SearchFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.viewPromiseBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendPromiseFragment())
                .addToBackStack(null)
                .commit()
        }

        // ViewModel 옵저버 세팅
        viewModel.followingUsers.observe(viewLifecycleOwner) { list ->
            followingAdapter.updateData(list)
        }

        viewModel.favoriteMap.observe(viewLifecycleOwner) { favMap ->
            followingAdapter.setFavoriteMap(favMap)
        }

        //  추가: 팔로워 목록 옵저버
        viewModel.followerUsers.observe(viewLifecycleOwner) { list ->
            followerAdapter.updateData(list)
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.receivedTeums.observe(viewLifecycleOwner) { teumList ->
            if (teumList.isNotEmpty()) {
                Log.d("RECEIVED_FRAGMENT", "틈 요청 조회 성공 - 개수: ${teumList.size}")
                binding.emptyTeumRequestLayout.visibility = View.GONE
                binding.recommendRecyclerView.visibility = View.VISIBLE
                recommendAdapter.setTeumList(teumList)
            } else {
                Log.d("RECEIVED_FRAGMENT", "틈 요청이 없습니다.")
                binding.emptyTeumRequestLayout.visibility = View.VISIBLE
                binding.recommendRecyclerView.visibility = View.GONE
            }
        }

        viewModel.getTeumRequests()

        // 탭 팔로잉 기본 선택
        binding.tabFollowing.performClick()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
        // 복귀 시 팔로잉 탭 유지
        binding.tabFollowing.performClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

