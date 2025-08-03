package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.TeumReceivedItem
import com.example.teumteum.data.remote.friend.dto.TeumRequest
import com.example.teumteum.databinding.FragmentFriendBinding
import com.example.teumteum.ui.friend.adapter.FollowerAdapter
import com.example.teumteum.ui.friend.adapter.FollowingAdapter
import com.example.teumteum.ui.friend.view.TeumRequestView
import com.example.teumteum.ui.friend.view.TeumReceivedView
import com.example.teumteum.data.remote.friend.dto.TeumRequestService
import com.example.teumteum.data.remote.friend.dto.TeumReceivedService
import com.example.teumteum.ui.friend.adapter.RecommendAdapter
import com.example.teumteum.ui.main.MainActivity

class FriendFragment : Fragment(), TeumRequestView, TeumReceivedView {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!
    private lateinit var recommendAdapter: RecommendAdapter

    private var latestTeumList: List<TeumReceivedItem> = emptyList() // 추가


    private val teumRequestService = TeumRequestService()
    private val teumReceivedService = TeumReceivedService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recommendAdapter = RecommendAdapter(
            onCardClick = { item, position ->

                teumRequestService.setTeumRequestView(this)

                // 카드 상세 프래그먼트로 이동만 수행
                val fragment = Friend02RequestFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList("teumList", ArrayList(latestTeumList))  // teumList는 TeumReceivedItem 리스트
                        putInt("selectedPosition", position)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()

            }
        )

        binding.recommendRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendAdapter
        }

        teumRequestService.setTeumRequestView(this)
        teumReceivedService.setTeumReceivedView(this)
        teumReceivedService.getReceivedTeumRequests()

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

        val followingAdapter = FollowingAdapter(
            data = emptyList(),
            onProfileClick = { user ->
                val fragment = FriendProfileFollowFragment().apply {
                    arguments = Bundle().apply {
                        putInt("userId", user.userId)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onSendClick = { user ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, FriendRoommateDateFragment())
                    .addToBackStack(null)
                    .commit()
            }
        )

        val followerAdapter = FollowerAdapter(emptyList())

        binding.followerRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = followerAdapter
        }

        binding.followingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = followingAdapter
        }

        binding.tabFollowing.setOnClickListener {
            binding.tabFollowing.setTextColor(Color.parseColor("#0F0F0F"))
            binding.tabFollower.setTextColor(Color.parseColor("#B1B2B3"))
            binding.followingRecyclerView.visibility = View.VISIBLE
            binding.followerRecyclerView.visibility = View.GONE
        }

        binding.tabFollower.setOnClickListener {
            binding.tabFollowing.setTextColor(Color.parseColor("#B1B2B3"))
            binding.tabFollower.setTextColor(Color.parseColor("#0F0F0F"))
            binding.followingRecyclerView.visibility = View.GONE
            binding.followerRecyclerView.visibility = View.VISIBLE
        }

        binding.viewPromiseBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendPromiseFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // 여기서부터
     // 추후 틈 요청하기 api 연동 시 필요
    override fun onTeumRequestSuccess(teumId: Int) {
        val message = "틈 요청이 성공적으로 생성되었습니다. (id: $teumId)"
        Log.d("REQUEST_FRAGMENT", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onTeumRequestFailure(code: String, message: String) {
        val userMessage = when (code) {
            "TEUM4030" -> "요청 또는 응답에 대한 권한이 없습니다."
            "COMMON400" -> "잘못된 요청입니다"
            "TEUM4091" -> "자기 자신에게 틈 요청을 보낼 수 없습니다."
            else -> "[$code] $message"
        }
        Log.e("REQUEST_FRAGMENT", "틈 요청 실패 - [$code] $message")
        Toast.makeText(requireContext(), userMessage, Toast.LENGTH_LONG).show()
    }
    // 여기까지

    override fun onTeumReceivedSuccess(teumList: List<TeumReceivedItem>) {
        val message = "틈 요청 조회 성공입니다. 요청 수: ${teumList.size}"
        Log.d("RECEIVED_FRAGMENT", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

        binding.emptyTeumRequestLayout.visibility = View.GONE
        binding.recommendRecyclerView.visibility = View.VISIBLE
        recommendAdapter.setTeumList(teumList)

        latestTeumList = teumList // 저장

    }

    override fun onTeumReceivedFailure(code: String, message: String) {
        val userMessage = when (code) {
            "TEUM4030" -> "요청 또는 응답에 대한 권한이 없습니다."
            else -> "[$code] $message"
        }
        Log.e("RECEIVED_FRAGMENT", "틈 요청 조회 실패 - [$code] $message")
        Toast.makeText(requireContext(), userMessage, Toast.LENGTH_LONG).show()

        binding.emptyTeumRequestLayout.visibility = View.VISIBLE
        binding.recommendRecyclerView.visibility = View.GONE
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