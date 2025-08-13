package com.example.teumteum.ui.friend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.databinding.FragmentFriendRoommateFriendAddBinding
import com.example.teumteum.data.remote.friend.model.MutualFriendItem
import com.example.teumteum.ui.friend.adapter.FriendAddAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRoommateFriendAddFragment : Fragment() {

    private var _binding: FragmentFriendRoommateFriendAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendAddAdapter
    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRoommateFriendAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val TAG = "MUTUAL_FRAGMENT"

        // 1) 리사이클러뷰/어댑터
        adapter = FriendAddAdapter(emptyList<MutualFriendItem>())
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendRecyclerView.adapter = adapter

        // 2) 제외할 사용자 ID (매칭 대상)
        val excludeUserIdArg = arguments?.getInt("excludeUserId", -1) ?: -1
        val excludeForApi = excludeUserIdArg.takeIf { it != -1 }
        Log.d(TAG, "onViewCreated | excludeUserId(from args)=$excludeUserIdArg -> forApi=$excludeForApi")

        // 3) 서버 호출: 서버에서 직접 제외 적용 (프론트에서 재필터링하지 않음)
        viewModel.getMutualFriends(excludeForApi)

        // 4) 단일 옵저버: 중복 등록 제거
        viewModel.mutualFriends.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "받은 맞팔 목록 수=${list.size}")
            if (list.isNotEmpty()) {
                Log.d(TAG, list.take(5).joinToString(prefix="sample<=5: ") { "(${it.userId}, ${it.nickname})" })
            }
            adapter.updateList(list) // FriendAddAdapter.updateList 내부에서 notify 호출 필요
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "error: $msg")
            }
        }

        // 5) 뒤로가기
        binding.backButton.setOnClickListener {
            val selectedFriends = adapter.getSelectedUserIdsWithInfo() // FriendProfileResult 리스트
            //뷰모델 저장
            viewModel.setTeumRequestReceiverUserIds(selectedFriends.map {
                it.userId
            })

            val result = Bundle().apply {
                putParcelableArrayList("friends", ArrayList(selectedFriends))
            }
            parentFragmentManager.setFragmentResult("selectedFriends", result)
            parentFragmentManager.popBackStack()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
