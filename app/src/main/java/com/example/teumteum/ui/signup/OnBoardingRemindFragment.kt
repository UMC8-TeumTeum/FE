package com.example.teumteum.ui.signup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.teumteum.data.remote.onboarding.model.RemindRequest
import com.example.teumteum.databinding.FragmentOnBoardingRemindBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.ui.signup.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class OnBoardingRemindFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingRemindBinding
    private val viewModel: OnBoardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnBoardingRemindBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(100)

        observeViewModel()

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
                    navigateToMain()
                }
                is OnBoardingUiState.Error -> {
                    if (state.code.contains("ONBOARDING4001")) {
                        navigateToMain()
                    }
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}