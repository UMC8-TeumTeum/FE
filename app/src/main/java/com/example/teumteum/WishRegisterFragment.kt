package com.example.teumteum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentWishRegisterBinding

class WishRegisterFragment : Fragment() {

    lateinit var binding: FragmentWishRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }
}