package com.umc.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.teumteum.databinding.FragmentSharedTeumTimeBinding
import com.umc.teumteum.ui.friend.adapter.SharedTeumAdapter
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel

class SharedTeumTimeFragment : Fragment() {

    private var _binding: FragmentSharedTeumTimeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()
    private lateinit var adapter: SharedTeumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharedTeumTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetUserId = arguments?.getInt("targetUserId") ?: -1

        // 로그인 유저 ID 주입하여 좌/우 결정
        adapter = SharedTeumAdapter()
        binding.sharedTeumRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.sharedTeumRecyclerView.adapter = adapter

        // (총 함께한 시간 + 목록) 데이터 호출
        if (targetUserId != -1) {
            viewModel.loadSharedTeumTime(targetUserId) // TEUM2012
            viewModel.loadSharedTeumList(targetUserId) // TEUM2013
        }

        // 총 함께한 시간
        viewModel.sharedTeumTimeText.observe(viewLifecycleOwner) { total ->
            binding.sharedTotalTimeTv.text = total
        }

        // 함께한 틈 목록
        viewModel.sharedTeumList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.emptyLayout.visibility = View.VISIBLE
                binding.sharedTeumRecyclerView.visibility = View.GONE
                binding.sharedTotalTimeTv.text = "0시간 0분"
            } else {
                binding.emptyLayout.visibility = View.GONE
                binding.sharedTeumRecyclerView.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.e("SHARED_TEUM_TIME_FRAGMENT", msg)
            }
        }
        binding.backButton.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
