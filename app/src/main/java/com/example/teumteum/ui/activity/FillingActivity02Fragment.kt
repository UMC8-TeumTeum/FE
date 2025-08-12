package com.example.teumteum.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.activity.model.ActivityAiRequest
import com.example.teumteum.data.remote.activity.model.ActivityAiResult
import com.example.teumteum.data.remote.activity.model.ActivityWishRequest
import com.example.teumteum.data.remote.activity.model.ActivityWishResult
import com.example.teumteum.databinding.FragmentFillingActivity02Binding
import com.example.teumteum.ui.activity.adapter.AiRecommendRVAdapter
import com.example.teumteum.ui.activity.adapter.WishRecommendRVAdapter
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
    private var aiList: MutableList<ActivityAiResult> = mutableListOf()

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

        showLoadingPage()

        wishAdapter = WishRecommendRVAdapter(wishList, parentFragmentManager)
        binding.wishRecommendRv.adapter = wishAdapter

        aiAdapter = AiRecommendRVAdapter(aiList, parentFragmentManager)
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

        getFillingActivity()

        binding.fabRefreshIv.setOnClickListener { getFillingActivity() }

        binding.fabRefreshIv.post {
            applyBlurShadow(
                sourceView = binding.fabRefreshIv,
                targetImageView = binding.fabShadowIv
            )
        }

        setupObservers()
        setupLoadingObserver()
    }

    private fun getFillingActivity() {
        val estimatedDuration = arguments?.getString("selectedTime") ?: ""
        val location = arguments?.getString("location") ?: ""
        val customCategory = arguments?.getString("customCategory") ?: ""
        val selectedCategoryText = arguments?.getString("selectedCategory")

        val categoryNameToId = mapOf(
            "자기계발" to 1L, "운동" to 2L, "취미" to 3L,
            "일상" to 4L, "문화생활" to 5L, "휴식" to 6L
        )
        val categoryId = categoryNameToId[selectedCategoryText]

        activityViewModel.activityWish(
            ActivityWishRequest(
                estimatedDuration = estimatedDuration,
                categoryId = categoryId,
                customCategory = customCategory
            )
        )
        activityViewModel.activityAi(
            ActivityAiRequest(
                estimatedDuration = estimatedDuration,
                location = location,
                categoryId = categoryId,
                customCategory = customCategory
            )
        )
    }

    private fun showLoadingPage() {
        val tag = LoadingPageFragment.TAG
        if (parentFragmentManager.findFragmentByTag(tag) == null) {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.main_frm, LoadingPageFragment.newInstance(), tag)
                .commitAllowingStateLoss()
        }
    }

    private fun setupLoadingObserver() {
        activityViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            val tag = LoadingPageFragment.TAG
            val loading = parentFragmentManager.findFragmentByTag(tag) as? LoadingPageFragment
            if (isLoading) {
                // 90%까지 채워두고 로딩 중 상태
                loading?.animateProgress(90)
            } else {
                // 100% 채우고 닫기
                loading?.completeAndDismiss()
            }
        }
    }

    private fun setupObservers() {

        activityViewModel.activityWishes.observe(viewLifecycleOwner) { wishes ->
            val filtered = wishes.filter { it.title.isNotBlank() }
            Log.d("위시확인", "받은 위시 개수: ${filtered.size}")
            filtered.forEach {
                Log.d("위시", "id=${it.id}, title='${it.title}'")
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

        activityViewModel.activityAiContents.observe(viewLifecycleOwner) { aiContents ->
            val filtered = aiContents.filter { it.title.isNotBlank() }
            Log.d("ai컨텐츠확인", "받은 ai컨텐츠 개수: ${filtered.size}")
            filtered.forEach {
                Log.d("ai컨텐츠", "id=${it.id}, title='${it.title}'")
            }

            aiList.clear()
            aiList.addAll(aiContents)

            aiAdapter.notifyDataSetChanged()
        }

        activityViewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("FillingActivity02Fragment", "에러 발생: $it")
            }
        }
    }

}