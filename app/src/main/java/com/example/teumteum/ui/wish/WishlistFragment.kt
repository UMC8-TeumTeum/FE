package com.example.teumteum.ui.wish

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.FragmentWishlistBinding
import com.example.teumteum.ui.wish.adapter.WishlistRVAdapter
import com.example.teumteum.ui.wish.viewModel.WishViewModel
import com.example.teumteum.utils.applyBlurShadow
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WishlistRVAdapter

    // 로컬 필터링에 의존하지 않으므로 내부 보관만 유지
    private var wishlistItems: List<WishlistItem> = emptyList()

    private val viewModel: WishViewModel by activityViewModels()

    // 스크롤에서 중복 호출 방지용
    private var loadingScrollGuard = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)

        binding.editTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistEditFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.fabAddIv.setOnClickListener {
            val bottomSheet = WishRegisterFragment().apply {
                arguments = Bundle().apply { putBoolean("isFromWish", true) }
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WishlistRVAdapter(wishlistItems, parentFragmentManager)
        binding.wishlistRv.adapter = adapter
        val lm = LinearLayoutManager(requireContext())
        binding.wishlistRv.layoutManager = lm

        // 바텀 내비게이션 숨기기
        activity?.findViewById<BottomNavigationView>(R.id.main_bnv)?.visibility = View.GONE

        binding.backArrowIv.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.fabAddIv.post {
            applyBlurShadow(sourceView = binding.fabAddIv, targetImageView = binding.fabShadowIv)
        }

        setupTimeFilterButtons()
        setupObservers()

        // 페이징: 리스트 끝 근처에서 다음 페이지 로드
        binding.wishlistRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val last = lm.findLastVisibleItemPosition()
                val total = adapter.itemCount
                if (!loadingScrollGuard && last >= total - 3) {
                    loadingScrollGuard = true
                    viewModel.loadNextPage()
                }
            }
        })

        // 최초 로드: 서버에서 all 기준 1페이지
        viewModel.refreshWishlist(duration = "all")

    }

    private fun setupTimeFilterButtons() {
        val allButton = binding.btnWishlistTime01
        val button10m = binding.btnWishlistTime02
        val button20m = binding.btnWishlistTime03
        val button30m = binding.btnWishlistTime04
        val button1h = binding.btnWishlistTime05

        allButton.setOnClickListener { onDurationSelected("all", allButton) }
        button10m.setOnClickListener { onDurationSelected("10m", button10m) }
        button20m.setOnClickListener { onDurationSelected("20m", button20m) }
        button30m.setOnClickListener { onDurationSelected("30m", button30m) }
        button1h.setOnClickListener { onDurationSelected("1h", button1h) }
    }

    private fun onDurationSelected(duration: String, button: MaterialButton) {
        // 로컬 필터링 제거 → 서버 재조회
        viewModel.refreshWishlist(duration)
        updateTimeButtonUI(button)
        // 새 필터 적용 직후 스크롤 가드 초기화
        loadingScrollGuard = false
    }

    private fun updateTimeButtonUI(selectedButton: MaterialButton) {
        val buttons = listOf(
            binding.btnWishlistTime01,
            binding.btnWishlistTime02,
            binding.btnWishlistTime03,
            binding.btnWishlistTime04,
            binding.btnWishlistTime05
        )

        for (button in buttons) {
            if (button == selectedButton) {
                button.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.text_primary, null))
                button.setTextColor(resources.getColor(R.color.white, null))
            } else {
                button.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.teumteum_line, null))
                button.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
    }

    private fun setupObservers() {
        viewModel.wishlistItems.observe(viewLifecycleOwner) { itemList ->
            wishlistItems = itemList
            loadingScrollGuard = false // 새 데이터가 들어오면 스크롤 가드를 풀어 다음 페이지를 받을 수 있게 함

            if (itemList.isEmpty()) {
                binding.wishlistRv.visibility = View.GONE
                binding.wishNotExistsCv.visibility = View.VISIBLE
            } else {
                binding.wishlistRv.visibility = View.VISIBLE
                binding.wishNotExistsCv.visibility = View.GONE
                adapter.updateList(itemList) // ViewModel이 append한 전체 리스트를 그대로 교체 반영
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Log.e("WISH_LIST_FRAGMENT", "위시리스트 조회 실패: $error")
        }
    }
}
