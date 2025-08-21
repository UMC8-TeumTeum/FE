package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TeumReceivedItem
import com.example.teumteum.databinding.FragmentFriend02ResponseBinding
import com.example.teumteum.ui.friend.adapter.FriendResponseCardAdapter
import com.example.teumteum.ui.main.MainActivity
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Friend02ResponseFragment  : Fragment(){

    private var _binding: FragmentFriend02ResponseBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendResponseCardAdapter
    private var teumItem: TeumReceivedItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02ResponseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .commit()
        }

        // 넘겨받은 아이템
        teumItem = arguments?.getParcelable("teumItem")

        teumItem?.let {
            adapter = FriendResponseCardAdapter(listOf(it))
            binding.requestViewPager.adapter = adapter
            binding.dotsIndicator.setViewPager2(binding.requestViewPager)
        }

        // 수락 버튼
        binding.btnAccept.setOnClickListener {
            teumItem?.responseId?.let { responseId ->
                val bottomSheet = Friend02AcceptBottomSheetFragment.newInstance(responseId)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }

        // 거절 버튼
        binding.btnReject.setOnClickListener {
            teumItem?.responseId?.let { responseId ->
                val bottomSheet = Friend02RejectBottomSheetFragment.newInstance(responseId)
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}