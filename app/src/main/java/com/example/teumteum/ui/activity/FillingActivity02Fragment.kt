package com.example.teumteum.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FillingActivity02Fragment : Fragment() {

    private lateinit var binding: FragmentFillingActivity02Binding
    private lateinit var aiAdapter: AiRecommendRVAdapter
    private lateinit var wishAdapter: WishRecommendRVAdapter
    private val wishList = mutableListOf<ActivityWishResult>()
    private val aiList = mutableListOf<ActivityAiResult>()

    private val activityViewModel: ActivityViewModel by activityViewModels()

    private var firstLoad = true
    private var isRefreshing = false

    private var shimmerStartAt = 0L
    private val minShimmerShownMs = 600L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        binding = FragmentFillingActivity02Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wishAdapter = WishRecommendRVAdapter(wishList, parentFragmentManager)
        binding.wishRecommendRv.adapter = wishAdapter

        aiAdapter = AiRecommendRVAdapter(aiList, parentFragmentManager)
        binding.aiRecommendRv.adapter = aiAdapter

        binding.fabShadowIv.isClickable = false
        binding.fabRefreshIv.bringToFront()

        // 옵저버 -> 최초 조회
        setupObservers()
        setupLoadingObserver()

        if (firstLoad) {
            view.post { showLoadingPage() }
        }

        getFillingActivity()

        // 새로고침: 시머 -> 재조회
        binding.fabRefreshIv.setOnClickListener {
            showShimmer()
            getFillingActivity() // API 재호출
        }
    }

    private fun getFillingActivity() {

        val args = requireArguments()

        val estimatedDuration = args.getString("selectedTime").orEmpty()
        val locationId = args.getLong("locationId", -1L).takeIf { it > 0 }
        val customLocation = args.getString("customLocation")?.takeIf { it.isNotBlank() }
        val categoryId = args.getLong("categoryId", -1L).takeIf { it > 0 }
        val customCategory = args.getString("customCategory")?.takeIf { it.isNotBlank() }

        val wishRequest = ActivityWishRequest(
            estimatedDuration = estimatedDuration,
            categoryId = categoryId,
            customCategory = customCategory
        )
        activityViewModel.activityWish(wishRequest)

        val aiRequest = ActivityAiRequest(
            estimatedDuration = estimatedDuration,
            locationId = locationId,
            customLocation = customLocation,
            categoryId = categoryId,
            customCategory = customCategory
        )
        activityViewModel.activityAi(aiRequest)
    }

    private fun setupLoadingObserver() {
        activityViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            val tag = LoadingPageFragment.TAG
            val fm = parentFragmentManager
            val overlay = fm.findFragmentByTag(tag) as? LoadingPageFragment

            if (isLoading) {
                if (!firstLoad && !isRefreshing) showShimmer() // 새로고침 시 시머 보이기
            } else {
                overlay?.completeAndDismiss()
                if (isRefreshing) hideShimmer()
                if (firstLoad) firstLoad = false   // 최초 사이클 끝나면 false
            }
        }
    }

    private fun setupObservers() {
        activityViewModel.activityWishes.observe(viewLifecycleOwner) { wishes ->
            wishList.clear()
            wishList.addAll(wishes)
            if (!isRefreshing) {
                if (wishes.isEmpty()) {
                    binding.fillingActivityWishNotExistsCv.visibility = View.VISIBLE
                    binding.wishRecommendRv.visibility = View.GONE
                } else {
                    binding.fillingActivityWishNotExistsCv.visibility = View.GONE
                    binding.wishRecommendRv.visibility = View.VISIBLE
                }
            }
            wishAdapter.notifyDataSetChanged()
        }
        activityViewModel.activityAiContents.observe(viewLifecycleOwner) { aiContents ->
            aiList.clear()
            aiList.addAll(aiContents)
            aiAdapter.notifyDataSetChanged()
        }
        activityViewModel.errorMessage.observe(viewLifecycleOwner) { it?.let { Log.e("Filling02", it) } }
    }

    private fun showLoadingPage() {
        val tag = LoadingPageFragment.TAG
        val fm = parentFragmentManager

        // 실행 중 트랜잭션과 충돌하지 않도록 다음 프레임으로 미룸
        val addOverlay = Runnable {
            if (!isAdded || fm.isStateSaved) return@Runnable
            if (fm.findFragmentByTag(tag) == null) {
                fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_frm, LoadingPageFragment.newInstance(), tag)
                    .commitAllowingStateLoss()
            }
        }

        // view가 있으면 view.post, 없으면 액티비티의 decorView로 post
        (view ?: activity?.window?.decorView)?.post(addOverlay)
    }


    private fun showShimmer() {
        isRefreshing = true
        shimmerStartAt = android.os.SystemClock.uptimeMillis()

        // 레이아웃 유지하면서 recyclerview 감추기
        binding.aiRecommendRv.alpha = 0f
        binding.wishRecommendRv.alpha = 0f
        binding.fillingActivityWishNotExistsCv.visibility = View.GONE

        // 위 레이어 고정 + 측정 후 시작
        fun on(t: com.facebook.shimmer.ShimmerFrameLayout) {
            t.visibility = View.VISIBLE
            t.bringToFront()
            ViewCompat.setTranslationZ(t, 8f)
            t.setShimmer(
                com.facebook.shimmer.Shimmer.AlphaHighlightBuilder()
                    .setDuration(2000L)
                    .setBaseAlpha(0.55f)
                    .setHighlightAlpha(1f)
                    .setIntensity(0.30f)
                    .setDropoff(0.65f)
                    .setDirection(com.facebook.shimmer.Shimmer.Direction.LEFT_TO_RIGHT)
                    .build()
            )
            t.post { t.startShimmer() }
        }
        on(binding.shimmerAi)
        on(binding.shimmerWish)
    }

    private fun hideShimmer() {
        val elapsed = android.os.SystemClock.uptimeMillis() - shimmerStartAt
        val delay = (minShimmerShownMs - elapsed).coerceAtLeast(0L)
        binding.root.postDelayed({
            binding.shimmerAi.apply { stopShimmer(); visibility = View.GONE }
            binding.shimmerWish.apply { stopShimmer(); visibility = View.GONE }
            binding.aiRecommendRv.alpha = 1f
            binding.wishRecommendRv.alpha = 1f
            isRefreshing = false
        }, delay)
    }
}
