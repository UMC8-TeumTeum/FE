package com.example.teumteum.ui.myhome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMyhomeBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signup.OnBoardingProfileFragment
import com.example.teumteum.ui.signup.OnBoardingScheduleFragment
import com.example.teumteum.ui.signup.SignUpActivity

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.nicknameTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.fieldTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.arrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
    }
}