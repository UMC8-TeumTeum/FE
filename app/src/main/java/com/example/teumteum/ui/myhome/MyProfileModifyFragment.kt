package com.example.teumteum.ui.myhome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMyProfileBinding
import com.example.teumteum.databinding.FragmentMyProfileModifyBinding
import com.example.teumteum.ui.main.MainActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyProfileModifyFragment : Fragment() {

    private lateinit var binding: FragmentMyProfileModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyProfileModifyBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.cancelTv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

}