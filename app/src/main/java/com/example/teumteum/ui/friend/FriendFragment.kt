package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendBinding
import com.example.teumteum.ui.friend.adapter.FollowerAdapter
import com.example.teumteum.ui.friend.RecommendAdapter
import com.example.teumteum.ui.friend.adapter.FollowingAdapter
import com.example.teumteum.ui.friend.data.FollowerData
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendFragment : Fragment() {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    private val possibleNames = listOf("홍길동", "김영희", "박민수", "최서연")
    private val requesters = mutableListOf<String>()

    private val handler = Handler(Looper.getMainLooper())
    private val simulateRunnable = object : Runnable {
        override fun run() {
            simulateNewRequest()
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.postDelayed(simulateRunnable, 5000)

        if (requesters.isNotEmpty()) {
            binding.textName.text = "나>${requesters.first()}"
            binding.textName.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, Friend02ResponseFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        val recommendAdapter = RecommendAdapter {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02RequestFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.viewPromiseBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendPromiseFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnAlarm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendTeumRequestFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.recommendRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendRecyclerView.adapter = recommendAdapter

        binding.btnSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend01SearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // API 연결 후 서버에서 받아온 데이터로 이 부분 수정 필요
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

        binding.followerRecyclerView.adapter = followerAdapter
        binding.followerRecyclerView.layoutManager = LinearLayoutManager(requireContext())

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
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(simulateRunnable)
        _binding = null
    }

    private fun simulateNewRequest() {
        val newName = possibleNames.random()
        requesters.add(newName)
        binding.textName.text = "나>$newName"
        binding.textName.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02ResponseFragment())
                .addToBackStack(null)
                .commit()
        }
        Toast.makeText(requireContext(), "$newName 의 요청이 들어왔습니다", Toast.LENGTH_SHORT).show()
    }
}