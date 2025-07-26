package com.example.teumteum.ui.wish

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.data.entities.WishlistItem
import com.example.teumteum.data.remote.WishService
import com.example.teumteum.databinding.FragmentWishlistBinding
import com.example.teumteum.utils.applyBlurShadow
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class WishlistFragment() : Fragment(), WishlistView {

    private lateinit var binding: FragmentWishlistBinding
    private lateinit var adapter: WishlistRVAdapter

    private var wishlist: List<WishlistItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)

//        binding.editTv.setOnClickListener {
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.main_frm, WishlistEditFragment(wishlist))
//                .addToBackStack(null)
//                .commit()
//        }

        binding.fabAddIv.setOnClickListener {
            val bottomSheet = WishRegisterFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isFromWish", true)  // 위시에서 열렸음을 전달
                }
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = WishlistRVAdapter(wishlist, parentFragmentManager)
        binding.wishlistRv.adapter = adapter

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.fabAddIv.post {
            applyBlurShadow(
                sourceView = binding.fabAddIv,
                targetImageView = binding.fabShadowIv
            )
        }

        setupTimeFilterButtons()

        // 위시 등록 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("wish_register", viewLifecycleOwner) { _, _ ->
            refreshWishlist()
        }

        get(duration = "all", page = 1)

    }

    private fun setupTimeFilterButtons() {
        val allButton = binding.btnWishlistTime01
        val button10m = binding.btnWishlistTime02
        val button20m = binding.btnWishlistTime03
        val button30m = binding.btnWishlistTime04
        val button1h = binding.btnWishlistTime05

        allButton.setOnClickListener {
            adapter.updateList(wishlist) // 전체 표시
            updateTimeButtonUI(allButton)
        }

        button10m.setOnClickListener {
            val filtered = wishlist.filter { it.estimatedDuration == "10m" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button10m)
        }

        button20m.setOnClickListener {
            val filtered = wishlist.filter { it.estimatedDuration == "20m" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button20m)
        }

        button30m.setOnClickListener {
            val filtered = wishlist.filter { it.estimatedDuration == "30m" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button30m)
        }

        button1h.setOnClickListener {
            val filtered = wishlist.filter { it.estimatedDuration == "1h" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button1h)
        }
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
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.text_primary, null))
                button.setTextColor(resources.getColor(R.color.white, null))
            } else {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.teumteum_line, null))
                button.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
    }

    private fun get(duration: String, page: Int) {

        val wishService = WishService()
        wishService.setWishlistGetView(this)
        wishService.getWishlist(duration, page)
    }

    fun refreshWishlist() {
        get(duration = "all", page = 1)
    }

    override fun onGetWishListSuccess(wishlist: List<WishlistItem>) {
        this.wishlist = wishlist
        adapter.updateList(wishlist)
    }

    override fun onGetWishListFailure(code: String, message: String?) {
        val errorMessage = when {
            code == "HOME4002" -> "잘못된 조회 기간입니다. (all, 10m, 20m, 30m, 1h만 입력 가능)"
            code == "COMMON500" && message?.contains("Page index must not be less than zero") == true ->
                "페이지 번호는 0 이상이어야 합니다."
            code == "COMMON500" -> "서버 오류입니다. 관리자에게 문의해주세요."
            code == "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            code == "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> "위시리스트 조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }


}