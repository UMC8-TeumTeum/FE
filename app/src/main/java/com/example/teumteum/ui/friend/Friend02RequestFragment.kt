package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.dto.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02RequestBinding
import com.example.teumteum.ui.main.MainActivity
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class Friend02RequestFragment : Fragment() {

    private var _binding: FragmentFriend02RequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    var teumList: List<TeumReceivedItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02RequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        // 1. 전달받은 TeumReceivedItem 리스트
        teumList = arguments?.getParcelableArrayList("teumList") ?: emptyList()

        // 2. 어댑터 연결
        adapter = FriendRequestCardAdapter(teumList)
        binding.requestViewPager.adapter = adapter

        // 3. 페이지 인디케이터 연결
        val dotsIndicator: DotsIndicator = binding.dotsIndicator
        dotsIndicator.setViewPager2(binding.requestViewPager)

        // 4. 버튼 이벤트
        binding.btnReject.setOnClickListener {
            val bottomSheet = Friend02RejectBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            Toast.makeText(requireContext(), "거절 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.btnAccept.setOnClickListener {
            val bottomSheet = Friend02AcceptBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            Toast.makeText(requireContext(), "함께할래요 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show()
        }

        // 5. 뒤로가기
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(teumList: ArrayList<TeumReceivedItem>): Friend02RequestFragment {
            return Friend02RequestFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", teumList)
                }
            }
        }
    }
}
