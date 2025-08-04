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
    private val viewModel: FriendViewModel by viewModels()
    private lateinit var recommendAdapter: RecommendAdapter

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

        binding.recommendRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendAdapter
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
        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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
