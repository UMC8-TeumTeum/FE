package com.example.teumteum.ui.signup

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.model.NicknameJobRequest
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
        binding.nextBtn.setOnClickListener {
            val request = getNicknameJobRequest()
            viewModel.postNicknameAndJob(request)
        }

        binding.nicknameClearBtn.setOnClickListener { binding.nicknameEt.setText("") }
        binding.fieldClearBtn.setOnClickListener { binding.fieldEt.setText("") }

        binding.nicknameEt.addTextChangedListener(textWatcher)
        binding.fieldEt.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateNextButtonState()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updateNextButtonState() {
        val enabled = binding.nicknameEt.text.isNotEmpty() && binding.fieldEt.text.isNotEmpty()
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

    private fun getNicknameJobRequest(): NicknameJobRequest {
        return NicknameJobRequest(
            nickname = binding.nicknameEt.text.toString(),
            jobField = binding.fieldEt.text.toString()
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
                    } else if (message.contains("ONBOARDING4001")) {
                        Toast.makeText(requireContext(), "온보딩 단계가 아닙니다.", Toast.LENGTH_SHORT).show()
                        navigateToNext()
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> Unit
            }
        }
    }

    private fun navigateToNext() {
        val fragment = OnBoardingProfileFragment().apply {
            arguments = Bundle().apply {
                putString("nickname", binding.nicknameEt.text.toString())
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        viewModel.resetState()
    }
}