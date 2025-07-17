package com.example.teumteum.ui.signup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.databinding.FragmentOnBoardingRemindBinding
import com.example.teumteum.ui.main.MainActivity

class OnBoardingRemindFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingRemindBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnBoardingRemindBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(100)

        binding.nextBtn.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

    }

}