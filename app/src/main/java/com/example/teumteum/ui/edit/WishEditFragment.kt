package com.example.teumteum.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.databinding.FragmentWishEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WishEditFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentWishEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishEditBinding.inflate(inflater, container, false)
        return binding.root
    }
}