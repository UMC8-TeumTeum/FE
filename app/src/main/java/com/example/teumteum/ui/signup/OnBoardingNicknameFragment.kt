package com.example.teumteum.ui.signup

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.OnBoardingService
import com.example.teumteum.data.remote.onboarding.dto.NicknameJobRequest
import com.example.teumteum.databinding.FragmentOnBoardingNicknameBinding
import com.example.teumteum.ui.signup.view.NicknameJobFieldView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingNicknameFragment : Fragment(), NicknameJobFieldView {

    private lateinit var binding: FragmentOnBoardingNicknameBinding

    @Inject
    lateinit var onBoardingService: OnBoardingService

    override fun onNicknameJobSuccess(code: String) {
        val msg = "닉네임, 직종 입력 성공 (code: $code)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("NICKNAME_FRAGMENT", msg)

        binding.nicknameErrorTv.visibility = View.INVISIBLE

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

    override fun onNicknameJobFailure(code: String, message: String?) {
        val msg = "닉네임 직종 입력 실패 (code: $code, message: ${message ?: "없음"})"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.e("NICKNAME_FRAGMENT", msg)

        //닉네임 중복
        if (message?.contains("ONBOARDING4091") == true) {
            binding.nicknameErrorTv.visibility = View.VISIBLE
        }

        //온보딩 단계가 아닐 경우 - 이후 테스트를 위해 화면 이동하도록 구현
        if (message?.contains("ONBOARDING4001") == true) {
            Toast.makeText(requireContext(), "온보딩 단계가 아닙니다.", Toast.LENGTH_SHORT).show()

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
    }

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
//            val fragment = OnBoardingProfileFragment().apply {
//                arguments = Bundle().apply {
//                    putString("nickname", binding.nicknameEt.text.toString())
//                }
//            }

            val request = getNicknameJobRequest()
            onBoardingService.setNicknameJobFieldView(this)
            onBoardingService.postNicknameAndJobField(request)

//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .addToBackStack(null)
//                .commit()
        }

        binding.nicknameClearBtn.setOnClickListener {
            binding.nicknameEt.setText("")
        }
        binding.fieldClearBtn.setOnClickListener {
            binding.fieldEt.setText("")
        }

//        binding.nicknameEt.addTextChangedListener(object: TextWatcher{
//            //텍스트 변경 전 호출
//            override fun beforeTextChanged(
//                s: CharSequence?,
//                start: Int,
//                count: Int,
//                after: Int
//            ) {
//                //Todo: 닉네임 중복 검사
//            }
//
//            override fun onTextChanged(
//                s: CharSequence?,
//                start: Int,
//                before: Int,
//                count: Int
//            ) {
//                //Todo: 닉네임 중복 검사
//                updateNextButtonState()
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//
//                //Todo: 닉네임 중복 검사
//                updateNextButtonState()
//            }
//
//        })

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

    private fun getNicknameJobRequest(): NicknameJobRequest{
        return NicknameJobRequest(
            nickname = binding.nicknameEt.text.toString(),
            jobField = binding.fieldEt.text.toString()
        )
    }
}