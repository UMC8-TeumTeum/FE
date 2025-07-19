package com.example.teumteum.ui.filling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.entities.AiRecommend
import com.example.teumteum.data.entities.WishRecommend
import com.example.teumteum.databinding.FragmentFillingActivity02Binding
import com.example.teumteum.ui.friend.FriendFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class FillingActivity02Fragment : Fragment() {

    private lateinit var binding: FragmentFillingActivity02Binding

    private lateinit var aiAdapter: AiRecommendRVAdapter
    private lateinit var wishAdapter: WishRecommendRVAdapter

    private var aiRecommendDummyList = mutableListOf(
        AiRecommend(1, "공모전 탐색", "10m", "자기계발"),
        AiRecommend(2, "영단어 10개 외우기", "20m", "자기계발"),
        AiRecommend(3, "독서하기", "30m", "자기계발")
    )

    private var wishRecommendDummyList = mutableListOf(
        WishRecommend(1, "매거진 3장 읽기", "10m", "자기계발"),
        WishRecommend(2, "GTQ 문제 연습", "20m", "자기계발"),
        WishRecommend(3, "일본어 공부", "30m", "자기계발")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFillingActivity02Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        aiAdapter = AiRecommendRVAdapter(aiRecommendDummyList, parentFragmentManager)
        binding.aiRecommendRv.adapter = aiAdapter

        wishAdapter = WishRecommendRVAdapter(wishRecommendDummyList, parentFragmentManager)
        binding.wishRecommendRv.adapter = wishAdapter

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.fillingActivityFriendSearchCv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }


    }
}