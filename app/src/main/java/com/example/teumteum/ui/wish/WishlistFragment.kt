package com.example.teumteum.ui.wish

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.FragmentWishlistBinding
import com.example.teumteum.ui.wish.adapter.WishlistRVAdapter
import com.example.teumteum.ui.wish.viewModel.WishlistGetViewModel
import com.example.teumteum.utils.applyBlurShadow
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WishlistFragment() : Fragment() {

    private lateinit var binding: FragmentWishlistBinding
    private lateinit var adapter: WishlistRVAdapter

    private var wishlistItems: List<WishlistItem> = emptyList()

    private val wishlistGetViewModel: WishlistGetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)

        binding.editTv.setOnClickListener {
            val currentList = wishlistGetViewModel.wishlistItems.value ?: emptyList()
            wishlistGetViewModel.wishlistItems.value = currentList.toMutableList()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistEditFragment())
                .addToBackStack(null)
                .commit()
        }

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
        super.onViewCreated(view, savedInstanceState)

        adapter = WishlistRVAdapter(wishlistItems, parentFragmentManager)
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
        setupObservers()

        // 위시 등록 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("wish_register", viewLifecycleOwner) { _, _ ->
            refreshWishlist()
        }

        // 위시 수정 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("wish_edit", viewLifecycleOwner) { _, _ ->
            refreshWishlist()
        }

        // 위시 삭제 성공 이벤트 수신
        parentFragmentManager.setFragmentResultListener("wish_delete", viewLifecycleOwner) { _, _ ->
            refreshWishlist()
        }

        wishlistGetViewModel.getWishlist(duration = "all", page = 1)
    }

    private fun setupTimeFilterButtons() {
        val allButton = binding.btnWishlistTime01
        val button10m = binding.btnWishlistTime02
        val button20m = binding.btnWishlistTime03
        val button30m = binding.btnWishlistTime04
        val button1h = binding.btnWishlistTime05

        allButton.setOnClickListener {
            filterAndUpdate(duration = "all", button = allButton)
        }

        button10m.setOnClickListener {
            filterAndUpdate(duration = "10m", button = button10m)
        }

        button20m.setOnClickListener {
            filterAndUpdate(duration = "20m", button = button20m)
        }

        button30m.setOnClickListener {
            filterAndUpdate(duration = "30m", button = button30m)
        }

        button1h.setOnClickListener {
            filterAndUpdate(duration = "1h", button = button1h)
        }
    }

    private fun filterAndUpdate(duration: String, button: MaterialButton) {
        val filteredList = when (duration) {
            "all" -> wishlistItems
            else -> wishlistItems.filter { it.estimatedDuration == duration }
        }

        if (filteredList.isEmpty()) {
            binding.wishlistRv.visibility = View.GONE
            binding.wishNotExistsCv.visibility = View.VISIBLE
        } else {
            binding.wishlistRv.visibility = View.VISIBLE
            binding.wishNotExistsCv.visibility = View.GONE
            adapter.updateList(filteredList)
        }

        updateTimeButtonUI(button)
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

    private fun setupObservers() {
        wishlistGetViewModel.wishlistItems.observe(viewLifecycleOwner) { itemList ->
            wishlistItems = itemList

            if (itemList.isEmpty()) {
                binding.wishlistRv.visibility = View.GONE
                binding.wishNotExistsCv.visibility = View.VISIBLE
            } else {
                binding.wishlistRv.visibility = View.VISIBLE
                binding.wishNotExistsCv.visibility = View.GONE
                adapter.updateList(itemList)
            }
        }

        wishlistGetViewModel.getError.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), "위시리스트 조회 실패: $error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshWishlist() {
        wishlistGetViewModel.getWishlist(duration = "all", page = 1)
    }
}