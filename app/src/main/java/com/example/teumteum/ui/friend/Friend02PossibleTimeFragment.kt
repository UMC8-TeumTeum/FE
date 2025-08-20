package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02PossibleTimeBinding
import com.example.teumteum.ui.friend.adapter.FriendRequestCardAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class Friend02PossibleTimeFragment : Fragment() {

    private var _binding: FragmentFriend02PossibleTimeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    private var teumList: List<TeumReceivedItem> = emptyList()
    private var responseId: Int = -1

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02PossibleTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  전달받은 데이터 받기
//        teumList = arguments?.getParcelableArrayList("teumList") ?: emptyList()
        val selected = viewModel.selectedTeum.value
        if (selected == null) {
            // 방어: 선택값이 없으면 종료
            parentFragmentManager.popBackStack()
            return
        }

        teumList = listOf(selected)
        responseId = arguments?.getInt("responseId") ?: -1

        //  어댑터 연결
        adapter = FriendRequestCardAdapter(teumList)
        binding.requestViewPager.adapter = adapter

        //  바텀 네비게이션 숨기기
        (activity as? MainActivity)?.hideBottomBar()

        //  뒤로가기
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02RequestFragment())
                .addToBackStack(null)
                .commit()
        }

        //  "찾기" 버튼 클릭 시 → Suggest로 넘어갈 때도 teumList, responseId 넘기기
        binding.btnFind.setOnClickListener {
            val fragment = Friend02SuggestFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", ArrayList(teumList))
                    putInt("responseId", responseId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()

//            Toast.makeText(requireContext(), "함께할래요 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(teumList: ArrayList<TeumReceivedItem>, responseId: Int): Friend02PossibleTimeFragment {
            return Friend02PossibleTimeFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", teumList)
                    putInt("responseId", responseId)
                }
            }
        }
    }
}
