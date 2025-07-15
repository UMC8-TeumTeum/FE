package com.example.teumteum.ui.singup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingNicknameBinding

class OnBoardingNicknameFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingNicknameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnBoardingNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(20)

        binding.nextBtn.setOnClickListener {
            val fragment = OnBoardingProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("nickname", binding.nicknameEt.text.toString())
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.nicknameClearBtn.setOnClickListener {
            binding.nicknameEt.setText("")
        }
        binding.fieldClearBtn.setOnClickListener {
            binding.fieldEt.setText("")
        }
    }
}