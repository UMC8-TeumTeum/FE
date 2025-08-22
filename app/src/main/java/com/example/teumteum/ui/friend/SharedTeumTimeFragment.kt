package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.data.AppUserManager
import com.example.teumteum.databinding.FragmentSharedTeumTimeBinding
import com.example.teumteum.ui.friend.adapter.SharedTeumAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel

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

        //  로그인 유저 ID 주입해서 좌/우 결정
        adapter = SharedTeumAdapter(currentUserId = AppUserManager.userId)
        binding.sharedTeumRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.sharedTeumRecyclerView.adapter = adapter

        // API 호출 (총 함께한 시간 + 목록)
        if (targetUserId != -1) {
            viewModel.loadSharedTeumTime(targetUserId)   // TEUM2012
            viewModel.loadSharedTeumList(targetUserId)  // TEUM2013
        }

        // 총 함께한 시간
        viewModel.sharedTeumTimeText.observe(viewLifecycleOwner) { total ->
            binding.sharedTotalTimeTv.text = total
        }

        // 함께한 틈 목록
        viewModel.sharedTeumList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        // 에러
        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Log.e("SHARED_TEUM_TIME_FRAGMENT", msg.toString())
            }
        }

        // 뒤로가기
        binding.backButton.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
