package com.example.teumteum.ui.myhome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMySettingBinding
import com.example.teumteum.ui.main.MainActivity

class MySettingFragment : Fragment() {

    private lateinit var binding: FragmentMySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMySettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyHomeFragment())
                .addToBackStack(null)
                .commit()
        }

    }
}