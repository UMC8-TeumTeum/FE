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
    }

}