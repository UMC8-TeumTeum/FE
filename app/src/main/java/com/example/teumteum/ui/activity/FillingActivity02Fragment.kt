package com.example.teumteum.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.entities.AiRecommend
import com.example.teumteum.data.remote.activity.ActivityService
import com.example.teumteum.data.remote.activity.dto.ActivityWishRequest
import com.example.teumteum.data.remote.activity.dto.ActivityWishResult
import com.example.teumteum.databinding.FragmentFillingActivity02Binding
import com.example.teumteum.ui.activity.view.ActivityWishView
import com.example.teumteum.ui.friend.FriendFragment
import com.example.teumteum.utils.ActivityRequestUtils.getCategoryIdIfExists
import com.example.teumteum.utils.ActivityRequestUtils.getCustomCategoryIfOther
import com.example.teumteum.utils.ActivityRequestUtils.getEstimatedDurationType
import com.example.teumteum.utils.applyBlurShadow
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FillingActivity02Fragment : Fragment(), ActivityWishView {

    private lateinit var binding: FragmentFillingActivity02Binding

    private lateinit var aiAdapter: AiRecommendRVAdapter
    private lateinit var wishAdapter: WishRecommendRVAdapter
    private var wishList: MutableList<ActivityWishResult> = mutableListOf()

    private var aiRecommendDummyList = mutableListOf(
        AiRecommend(1, "공모전 탐색", "10m", "자기계발"),
        AiRecommend(2, "영단어 10개 외우기", "20m", "자기계발"),
        AiRecommend(3, "독서하기", "30m", "자기계발")
    )

    @Inject
    lateinit var service: ActivityService

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

        val selectedTime = arguments?.getString("selectedTime")
        val selectedCategory = arguments?.getString("selectedCategory")
        val customCategory = arguments?.getString("customCategory")

        val request = ActivityWishRequest(
            estimatedDuration = getEstimatedDurationType(selectedTime!!),
            categoryId = getCategoryIdIfExists(selectedCategory),
            customCategory = getCustomCategoryIfOther(selectedCategory, customCategory)
        )

        service.setActivityWishView(this)
        service.activityWish(request)  // 서버에 요청 보내기


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

    }

    override fun onGetActivityWishSuccess(code: String, wishes: List<ActivityWishResult>) {
        Toast.makeText(requireContext(), "채움활동 위시 조회 성공", Toast.LENGTH_SHORT).show()

        if (wishes.isEmpty()) {
            // 위시 없음 → 안내 컴포넌트 표시
            binding.fillingActivityWishNotExistsCv.visibility = View.VISIBLE
            binding.wishRecommendRv.visibility = View.GONE
        } else {
            // 위시 있음 → 리스트 표시
            binding.fillingActivityWishNotExistsCv.visibility = View.GONE
            binding.wishRecommendRv.visibility = View.VISIBLE
        }

        wishList.clear()
        wishList.addAll(wishes)
        wishAdapter.notifyDataSetChanged()
    }

    override fun onGetActivityWishFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "COMMON500" -> "서버 오류입니다. 관리자에게 문의해주세요."
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> "채움활동 위시 조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }
}