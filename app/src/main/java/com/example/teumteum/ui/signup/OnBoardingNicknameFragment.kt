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
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingNicknameBinding
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.ui.signup.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingNicknameFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingNicknameBinding

    private val viewModel: OnBoardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(20)

        observeViewModel()
        setupUI()
    }

    private fun setupUI() {
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
            if (enabled) requireContext().getColor(R.color.black)
            else Color.parseColor("#F6F6F6")
        )
        binding.nextBtn.setTextColor(
            if (enabled) requireContext().getColor(R.color.white)
            else requireContext().getColor(R.color.black)
        )
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> binding.nextBtn.isEnabled = false
                is OnBoardingUiState.Success -> navigateToNext()
                is OnBoardingUiState.Error -> {
                    binding.nextBtn.isEnabled = true
                    val message = state.message
                    val code = state.code
                    if (code.contains("ONBOARDING4091")) {
                        binding.nicknameErrorTv.visibility = View.VISIBLE
                    } else if (code.contains("ONBOARDING4001")) {
                        Log.d("NicknameFragment", "ONBOARDING4001 - 강제 이동")
                        navigateToNext()
                    } else {
//                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    }
                }
                else -> Unit
            }
        }
    }

    private fun navigateToNext() {
        val fragment = OnBoardingProfileFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        viewModel.resetState()
    }
}