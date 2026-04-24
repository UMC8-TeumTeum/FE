package com.umc.teumteum.ui.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentOnBoardingNicknameBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingNicknameFragment : Fragment() {

    private var _binding: FragmentOnBoardingNicknameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnBoardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialMarginBottom =
            (binding.nextBtn.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.nextBtn) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + bottomInset
            }
            insets
        }

        observeViewModel()
        setupUI()
    }

    private fun setupUI() {
        // 프로그래스바 설정
        (activity as? SignUpActivity)?.setProgressBar(20)

        // 텍스트 입력 ViewModel 업데이트
        binding.nicknameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setNickname(s.toString())
                updateNextButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.fieldEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setField(s.toString())
                updateNextButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.nextBtn.setOnClickListener {
            viewModel.postNicknameAndJob()
        }

        binding.nicknameClearBtn.setOnClickListener {
            binding.nicknameEt.setText("")
        }
        binding.fieldClearBtn.setOnClickListener { binding.fieldEt.setText("") }

        binding.nicknameEt.addTextChangedListener(textWatcher)
        binding.fieldEt.addTextChangedListener(textWatcher)

        // 기존에 입력된 데이터가 있으면 복원
        binding.nicknameEt.setText(viewModel.nickname.value)
        binding.fieldEt.setText(viewModel.field.value)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateNextButtonState()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updateNextButtonState() {
        val enabled = viewModel.nickname.value?.isNotEmpty() == true &&
                viewModel.field.value?.isNotEmpty() == true

        binding.nextBtn.isEnabled = enabled
        binding.nextBtn.setBackgroundColor(
            if (enabled) requireContext().getColor(R.color.text_primary)
            else requireContext().getColor(R.color.teumteum_bg)
        )
        binding.nextBtn.setTextColor(
            if (enabled) requireContext().getColor(R.color.white)
            else requireContext().getColor(R.color.text_primary)
        )
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> binding.nextBtn.isEnabled = false
                is OnBoardingUiState.Success -> navigateToNext()
                is OnBoardingUiState.Error -> {
                    binding.nextBtn.isEnabled = true

                    binding.nicknameErrorTv.visibility = View.GONE

                    val code = state.code
                    val msg = state.message

                    // 1) 닉네임 중복 검증 로직
                    if (code.contains("ONBOARDING4091")) {
                        binding.nicknameErrorTv.visibility = View.VISIBLE
                        binding.nicknameErrorTv.text = "중복된 닉네임입니다."
                        return@observe
                    }

                    // 2) 닉네임 형식 오류
                    if (code.contains("COMMON400") || msg.contains("닉네임은")) {
                        binding.nicknameErrorTv.visibility = View.VISIBLE
                        binding.nicknameErrorTv.text =
                            if (msg.contains("닉네임은")) msg else "닉네임은 영어와 한글만 가능합니다."
                        return@observe
                    }
                }
                else -> Unit
            }
        }
    }

    private fun navigateToNext() {
        (activity as? SignUpActivity)?.proceedToNextOnboardingStep(this)
        viewModel.resetState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}