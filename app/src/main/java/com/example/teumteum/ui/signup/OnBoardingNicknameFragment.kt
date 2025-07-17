package com.example.teumteum.ui.signup

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        binding.nicknameEt.addTextChangedListener(object: TextWatcher{
            //텍스트 변경 전 호출
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //Todo: 닉네임 중복 검사
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                //Todo: 닉네임 중복 검사
                updateNextButtonState()
            }

            override fun afterTextChanged(s: Editable?) {

                //Todo: 닉네임 중복 검사
                updateNextButtonState()
            }

        })

        binding.fieldEt.addTextChangedListener(object: TextWatcher{
            //텍스트 변경 전 호출
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //Todo: 닉네임 중복 검사
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                //Todo: 닉네임 중복 검사
                updateNextButtonState()
            }

            override fun afterTextChanged(s: Editable?) {

                //Todo: 닉네임 중복 검사
                updateNextButtonState()
            }

        })
    }

    private fun updateNextButtonState() {
        val allChecked = binding.nicknameEt.text.isNotEmpty() && binding.fieldEt.text.isNotEmpty()
        binding.nextBtn.isEnabled = allChecked

        // 배경색 변경
        binding.nextBtn.setBackgroundColor(
            if (allChecked)
                requireContext().getColor(R.color.black)
            else
                Color.parseColor("#F6F6F6")
        )

        // 글자색 변경
        binding.nextBtn.setTextColor(
            if (allChecked)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.black)
        )
    }
}