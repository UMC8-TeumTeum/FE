package com.example.teumteum.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentLoadingPageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingPageFragment : Fragment() {

    private lateinit var binding: FragmentLoadingPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingPageBinding.inflate(inflater, container, false)
        return binding.root
    }
}