package com.example.teumteum.SignUp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingNicknameBinding
import com.example.teumteum.databinding.FragmentOnBoardingProfileBinding
import kotlin.plus

class OnBoardingProfileFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnBoardingProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(25)

        val nickname = arguments?.getString("nickname")
        setNickname(nickname.toString())
    }

    private fun setNickname(nickname: String){
        binding.titleTv.text = nickname + " 님"
        binding.nicknameTv.text = nickname
    }
}