package com.example.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentMyhomeBinding

class MyhomeFragment : Fragment() {

    lateinit var binding: FragmentMyhomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyhomeBinding.inflate(inflater, container, false)

        return binding.root
    }
}