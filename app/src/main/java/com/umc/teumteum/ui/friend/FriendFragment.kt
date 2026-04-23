package com.umc.teumteum.ui.friend

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumReceivedItem
import com.umc.teumteum.databinding.FragmentFriendBinding
import com.umc.teumteum.ui.friend.adapter.FollowerAdapter
import com.umc.teumteum.ui.friend.adapter.FollowingAdapter
import com.umc.teumteum.ui.friend.adapter.RecommendAdapter
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendFragment : Fragment() {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private lateinit var recommendAdapter: RecommendAdapter
    private lateinit var followingAdapter: FollowingAdapter
    private lateinit var followerAdapter: FollowerAdapter

    private val Int.dp: Int get() =
        (this * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 내 정보(닉네임, 프로필) 가져오기
        viewModel.fetchMyInfo()

        val defaultTab = arguments?.getString("defaultTab", "following")

        if (defaultTab == "follower") {
            binding.tabFollower.performClick()
        } else {
            binding.tabFollowing.performClick()
        }

        recommendAdapter = RecommendAdapter(
            onCardClick = { item: TeumReceivedItem, position: Int ->
                Log.d("CARD_CLICK", "카드 클릭됨, responseId=${item.responseId}")
                viewModel.readTeumRequest(item.responseId)

                val fragment = if (item.resend) {
                    // 재요청 카드: response 화면
                    Friend02ResponseFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable("teumItem", item)
                        }
                    }
                } else {
                    // 원본 요청 카드: request 화면
                    Friend02RequestFragment().apply {
                        arguments = Bundle().apply {
                            val originalRequests = viewModel.receivedTeums.value
                                ?.filter { !it.resend }   // 재요청 제거
                                ?: emptyList()

                            putParcelableArrayList("teumList", ArrayList(originalRequests))
                            putInt("selectedPosition", position)
                        }
                    }
                }

                if(!item.read) {
                    viewModel.readTeumRequest(item.responseId)
                }

                // 선택된 아이템 저장
                viewModel.selectTeum(item)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        val lm = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendRecyclerView.layoutManager = lm

        // 중복 추가 방지: 기존 데코 제거
        while (binding.recommendRecyclerView.itemDecorationCount > 0) {
            binding.recommendRecyclerView.removeItemDecorationAt(0)
        }

        // 아이템 간격 12dp, RTL 대응
        binding.recommendRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val pos = parent.getChildAdapterPosition(view)
                if (pos == RecyclerView.NO_POSITION) return

                val isRtl = ViewCompat.getLayoutDirection(parent) == ViewCompat.LAYOUT_DIRECTION_RTL
                val isLast = pos == state.itemCount - 1

                val space = 12.dp

                // 아이템 사이 간격만 부여
                if (!isLast) {
                    if (isRtl) outRect.left = space else outRect.right = space
                } else {
                    // 마지막 아이템은 간격 없음
                    outRect.set(0, 0, 0, 0)
                }
            }
        })

        followingAdapter = FollowingAdapter(
            data = emptyList(),
            onProfileClick = { user ->
                val fragment = FriendProfileFollowFragment().apply {
                    arguments = Bundle().apply {
                        putInt("userId", user.userId)
                        putString("fromTab", "following")
                    }
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

        // 팔로워 어댑터
        followerAdapter = FollowerAdapter(
            data = emptyList(),
            onProfileClick = { user ->
                viewModel.getFriendProfile(user.userId) { profile ->
                    val fragment = if (profile.following) {
                        FriendProfileFollowingFragment().apply {
                            arguments = Bundle().apply {
                                putInt("userId", profile.userId)
                                putString("name", profile.name)
                                putString("field", profile.field)
                                putString("imageUrl", profile.profileImageUrl)
                                putString("fromTab", "follower")
                            }
                        }
                    } else {
                        FriendProfileFollowFragment().apply {
                            arguments = Bundle().apply {
                                putInt("userId", profile.userId)
                                putString("fromTab", "follower")
                            }
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            },
            onSendClick = { user ->
                viewModel.getFriendProfile(user.userId) { profile ->
                    if (profile.following) {
                        val f = FriendRoommateDateFragment().apply {
                            arguments = Bundle().apply {
                                putInt("targetUserId", profile.userId)
                                putString("targetNickname", profile.name)
                                putString("targetProfileUrl", profile.profileImageUrl)
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, f)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        val frag = FriendProfileFollowFragment().apply {
                            arguments = Bundle().apply {
                                putInt("userId", profile.userId)
                                putString("fromTab", "follower")
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, frag)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        )

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

        // 무한스크롤 (following)
        val flm = binding.followingRecyclerView.layoutManager as LinearLayoutManager
        binding.followingRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val last = flm.findLastVisibleItemPosition()
                if (last >= flm.itemCount - 3) viewModel.loadNextFollowings()
            }
        })

        // 무한스크롤 (follower)
        val flm2 = binding.followerRecyclerView.layoutManager as LinearLayoutManager
        binding.followerRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val last = flm2.findLastVisibleItemPosition()
                if (last >= flm2.itemCount - 3) viewModel.loadNextFollowers()
            }
        })

        // 탭 클릭 리스너
        binding.tabFollowing.setOnClickListener {
            binding.tabFollowing.setTextColor(requireContext().getColor(R.color.text_primary))
            binding.tabFollower.setTextColor(requireContext().getColor(R.color.teumteum_deactive))
            binding.followingRecyclerView.visibility = View.VISIBLE
            binding.followerRecyclerView.visibility = View.GONE

            // 팔로잉 목록 조회
            viewModel.resetFollowingPaging()
            viewModel.loadNextFollowings()
        }

        binding.tabFollower.setOnClickListener {
            binding.tabFollowing.setTextColor(requireContext().getColor(R.color.teumteum_deactive))
            binding.tabFollower.setTextColor(requireContext().getColor(R.color.text_primary))
            binding.followingRecyclerView.visibility = View.GONE
            binding.followerRecyclerView.visibility = View.VISIBLE

            // 팔로워 목록 조회
            viewModel.resetFollowerPaging()
            viewModel.loadNextFollowers()
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
            val myNickname = viewModel.myNickname.value ?: ""
            val myProfileUrl = viewModel.myProfileUrl.value ?: ""

            val frag = FriendPromiseFragment().apply {
                arguments = Bundle().apply {
                    putString("nickname", myNickname)
                    putString("profileImageUrl", myProfileUrl)
                }
            }

            // 전달한 인스턴스(frag)로 교체
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, frag)
                .addToBackStack(null)
                .commit()
        }

        viewModel.followingUsers.observe(viewLifecycleOwner) { list ->
            followingAdapter.updateData(list)
        }

        viewModel.favoriteMap.observe(viewLifecycleOwner) { favMap ->
            followingAdapter.setFavoriteMap(favMap)
        }

        viewModel.followerUsers.observe(viewLifecycleOwner) { list ->
            followerAdapter.updateData(list)
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.d("FriendFragment", msg)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.d("FriendFragment", msg)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

