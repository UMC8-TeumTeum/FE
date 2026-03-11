package com.umc.teumteum.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.data.remote.onboarding.model.RemindRequest
import com.umc.teumteum.databinding.FragmentOnBoardingRemindBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingRemindFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingRemindBinding
    private val viewModel: OnBoardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingRemindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        val initialMarginBottom =
            (binding.nextBtn.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.nextBtn) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + bottomInset
            }
            insets
        }

        binding.nextBtn.setOnClickListener {
            val reminders = viewModel.remindList.value ?: emptyList()
            val request = RemindRequest(reminders)
            viewModel.postRemind(request)
        }

        binding.remind1mSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleReminder(1, isChecked)
        }
        binding.remind3mSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleReminder(3, isChecked)
        }
        binding.remind5mSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleReminder(5, isChecked)
        }
        binding.remind10mSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleReminder(10, isChecked)
        }
        binding.remind30mSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleReminder(30, isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Success -> {
                    // 온보딩 완료 - SignUpActivity의 메서드를 통해 메인으로 이동
                    (activity as? SignUpActivity)?.completeOnboarding()
                }
                is OnBoardingUiState.Error -> {
                }
                else -> Unit
            }
        }
    }
}