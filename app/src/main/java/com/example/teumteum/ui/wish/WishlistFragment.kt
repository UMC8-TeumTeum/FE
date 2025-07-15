package com.example.teumteum.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.databinding.FragmentWishlistBinding
import com.example.teumteum.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class WishlistFragment : Fragment() {

    private lateinit var binding: FragmentWishlistBinding

    private lateinit var adapter: WishRVAdapter

    private var dummyList = mutableListOf(
        WishItem(1, "화분 물 주기", "10m", "일상"),
        WishItem(2, "무신사 아이 쇼핑", "10m", "일상"),
        WishItem(3, "뜨개질하기", "30m", "취미"),
        WishItem(4, "피그마 파일 정리", "20m", "일상"),
        WishItem(5, "인센스 & 명상", "10m", "휴식"),
        WishItem(6, "방 구조 바꾸기", "30m", "일상"),
        WishItem(7, "매거진 3장 읽기", "10m", "일상"),
        WishItem(8, "사진첩 정리", "30m", "일상"),
        WishItem(9, "중랑천 산책", "1h~", "운동")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = WishRVAdapter(dummyList, parentFragmentManager)
        binding.wishlistRv.adapter = adapter

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
    }

}