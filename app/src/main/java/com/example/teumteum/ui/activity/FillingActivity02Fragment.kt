package com.example.teumteum.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.entities.AiRecommend
import com.example.teumteum.data.remote.activity.model.ActivityWishResult
import com.example.teumteum.databinding.FragmentFillingActivity02Binding
import com.example.teumteum.ui.activity.viewModel.ActivityViewModel
import com.example.teumteum.ui.friend.FriendFragment
import com.example.teumteum.utils.applyBlurShadow
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FillingActivity02Fragment : Fragment() {

    private lateinit var binding: FragmentFillingActivity02Binding

    private lateinit var aiAdapter: AiRecommendRVAdapter
    private lateinit var wishAdapter: WishRecommendRVAdapter
    private var wishList: MutableList<ActivityWishResult> = mutableListOf()

    private var aiRecommendDummyList = mutableListOf(
        AiRecommend(1, "공모전 탐색", "10m", "자기계발"),
        AiRecommend(2, "영단어 10개 외우기", "20m", "자기계발"),
        AiRecommend(3, "독서하기", "30m", "자기계발")
    )

    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFillingActivity02Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        wishAdapter = WishRecommendRVAdapter(wishList, parentFragmentManager)
        binding.wishRecommendRv.adapter = wishAdapter

        aiAdapter = AiRecommendRVAdapter(aiRecommendDummyList, parentFragmentManager)
        binding.aiRecommendRv.adapter = aiAdapter

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

        binding.fabRefreshIv.setOnClickListener {

            // 새로고침 되는지 테스트
            aiRecommendDummyList = mutableListOf(
                AiRecommend(5, "캠퍼스 풍경 사진 찍기", "20m", "취미"),
                AiRecommend(6, "음악 감상하면서 산책하기", "30m", "취미"),
                AiRecommend(6, "계단 오르기 운동 해보기", "20m", "취미")
            )

            aiAdapter.updateList(aiRecommendDummyList)
        }

        binding.fabRefreshIv.post {
            applyBlurShadow(
                sourceView = binding.fabRefreshIv,
                targetImageView = binding.fabShadowIv
            )
        }

        setupObservers()

    }

    private fun setupObservers() {

        activityViewModel.activityWishes.observe(viewLifecycleOwner) { wishes ->
            val filtered = wishes.filter { it.content.isNotBlank() }
            Log.d("위시확인", "받은 위시 개수: ${filtered.size}")
            filtered.forEach {
                Log.d("위시", "id=${it.id}, content='${it.content}'")
            }

            wishList.clear()
            wishList.addAll(wishes)

            if (wishes.isEmpty()) {
                binding.fillingActivityWishNotExistsCv.visibility = View.VISIBLE
                binding.wishRecommendRv.visibility = View.GONE
            } else {
                binding.fillingActivityWishNotExistsCv.visibility = View.GONE
                binding.wishRecommendRv.visibility = View.VISIBLE
            }

            wishAdapter.notifyDataSetChanged()
        }

        activityViewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("FillingActivity02Fragment", "에러 발생: $it")
            }
        }
    }

}