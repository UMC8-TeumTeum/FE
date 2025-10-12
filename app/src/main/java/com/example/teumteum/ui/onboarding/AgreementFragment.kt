package com.example.teumteum.ui.onboarding

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.model.AgreementRequest
import com.example.teumteum.databinding.FragmentAgreementBinding
import com.example.teumteum.ui.auth.SignUpActivity
import com.example.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.example.teumteum.ui.onboarding.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgreementFragment : Fragment() {

    private var _binding: FragmentAgreementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnBoardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgreementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? SignUpActivity)?.apply {
            setProgressBarVisible(true)
            setProgressBar(50)
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setupCheckBoxListeners()
        setupSelectAllCheckbox()
        updateNextButtonState()

        binding.nextBtn.setOnClickListener {
            val request = getAgreementRequest()
            viewModel.postAgreements(request)
        }

        listOf(
            Triple(binding.term1Tv, "term1", "서비스 이용 약관 동의 (필수)"),
            Triple(binding.term2Tv, "term2", "개인정보 수집 및 이용 동의 (필수)"),
            Triple(binding.term3Tv, "term3", "개인정보 제3자 제공에 대한 안내 (선택)"),
            Triple(binding.term4Tv, "term4", "마케팅 및 광고성 정보 수신 동의 (선택)")
        ).forEach { (textView, key, title) ->
            textView.setOnClickListener { openTermsDetail(key, title) }
        }

        listOf(
            binding.term1Checkbox,
            binding.term2Checkbox,
            binding.term3Checkbox,
            binding.term4Checkbox,
            binding.allCheckbox
        ).forEach { setCheckBoxTint(it, it.isChecked) }
    }

    private fun setupCheckBoxListeners() {
        val listener = { checkBox: CompoundButton ->
            updateNextButtonState()
            syncSelectAllCheckbox()
            setCheckBoxTint(checkBox, checkBox.isChecked)
        }

        listOf(
            binding.term1Checkbox,
            binding.term2Checkbox,
            binding.term3Checkbox,
            binding.term4Checkbox
        ).forEach {
            it.setOnCheckedChangeListener { _, _ -> listener(it) }
        }
    }

    private fun updateNextButtonState() {
        val enabled = binding.term1Checkbox.isChecked && binding.term2Checkbox.isChecked
        binding.nextBtn.isEnabled = enabled
        binding.nextBtn.setBackgroundColor(
            if (enabled) requireContext().getColor(R.color.black) else Color.parseColor("#F6F6F6")
        )
        binding.nextBtn.setTextColor(
            if (enabled) requireContext().getColor(R.color.white) else requireContext().getColor(R.color.black)
        )
    }

    private fun setupSelectAllCheckbox() {
        binding.allCheckbox.setOnCheckedChangeListener { checkBox, isChecked ->
            setAllAgreementChecked(isChecked)
            setCheckBoxTint(checkBox, isChecked)
        }
    }

    private fun syncSelectAllCheckbox() {
        val allChecked = listOf(
            binding.term1Checkbox,
            binding.term2Checkbox,
            binding.term3Checkbox,
            binding.term4Checkbox
        ).all { it.isChecked }

        if (binding.allCheckbox.isChecked != allChecked) {
            binding.allCheckbox.setOnCheckedChangeListener(null)
            binding.allCheckbox.isChecked = allChecked
            setCheckBoxTint(binding.allCheckbox, allChecked)
            binding.allCheckbox.setOnCheckedChangeListener { _, isChecked ->
                setAllAgreementChecked(isChecked)
                setCheckBoxTint(binding.allCheckbox, isChecked)
            }
        }
    }

    private fun setAllAgreementChecked(isChecked: Boolean) {
        listOf(
            binding.term1Checkbox,
            binding.term2Checkbox,
            binding.term3Checkbox,
            binding.term4Checkbox
        ).forEach { it.isChecked = isChecked }
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val color = ContextCompat.getColor(
            requireContext(), if (isChecked) R.color.black else R.color.gray
        )
        checkBox.buttonTintList = ColorStateList.valueOf(color)
    }

    private fun openTermsDetail(termKey: String, title: String) {
        val fragment = TermsDetailFragment().apply {
            arguments = Bundle().apply {
                putString("term_key", termKey)
                putString("term_title", title)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getAgreementRequest(): AgreementRequest {
        return AgreementRequest(
            tosConsent = binding.term1Checkbox.isChecked,
            privacyConsent = binding.term2Checkbox.isChecked,
            thirdPartyConsent = binding.term3Checkbox.isChecked,
            marketingConsent = binding.term4Checkbox.isChecked
        )
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> binding.nextBtn.isEnabled = false
                is OnBoardingUiState.Success -> navigateToNext()
                is OnBoardingUiState.Error -> {
                    if (state.code == "ONBOARDING4001") {
                        Log.d("AgreementFragment", "ONBOARDING4001 - 강제 이동")
                        navigateToNext()
                    } else {
                        binding.nextBtn.isEnabled = true
                    }
                }
                else -> Unit
            }
        }
    }

    private fun navigateToNext() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CompleteFragment())
            .addToBackStack(null)
            .commit()
        viewModel.resetState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}