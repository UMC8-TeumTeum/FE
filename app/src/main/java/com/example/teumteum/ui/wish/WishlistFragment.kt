package com.example.teumteum.ui.wish

import android.content.res.ColorStateList
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
import com.google.android.material.button.MaterialButton

class WishlistFragment(private val wishlist: MutableList<WishItem>) : Fragment() {

    private lateinit var binding: FragmentWishlistBinding

    private lateinit var adapter: WishlistRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)

        binding.editTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistEditFragment(wishlist))
                .addToBackStack(null)
                .commit()
        }

        binding.fabAddIv.setOnClickListener {
            val bottomSheet = WishOnlyBottomSheetFragment()
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
        setupTimeFilterButtons()
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
            val filtered = wishlist.filter { it.time == "10m" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button10m)
        }

        button20m.setOnClickListener {
            val filtered = wishlist.filter { it.time == "20m" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button20m)
        }

        button30m.setOnClickListener {
            val filtered = wishlist.filter { it.time == "30m" }
            adapter.updateList(filtered)
            updateTimeButtonUI(button30m)
        }

        button1h.setOnClickListener {
            val filtered = wishlist.filter { it.time == "1h~" }
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


}