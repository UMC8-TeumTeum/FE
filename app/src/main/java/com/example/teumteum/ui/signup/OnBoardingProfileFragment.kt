package com.example.teumteum.ui.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingProfileBinding
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.ui.signup.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingProfileFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingProfileBinding
    private val viewModel: OnBoardingViewModel by activityViewModels()

    private var lastSelectedImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                lastSelectedImageUri = uri
                binding.profileIv.setImageURI(uri)
                binding.cameraBtn.visibility = View.GONE
                viewModel.setProfileImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? SignUpActivity)?.setProgressBar(25)

        val nickname = viewModel.nickname.value
        binding.titleTv.text = "$nickname 님"
        binding.nicknameTv.text = nickname
        binding.profileIv.setImageURI(viewModel.profileImageUri.value)
        if(viewModel.profileImageUri.value != null){
            binding.cameraBtn.visibility = View.GONE
        }

        observeViewModel()

        val pickImageIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        binding.galleryFl.setOnClickListener { galleryLauncher.launch(pickImageIntent) }
        binding.cameraBtn.setOnClickListener { galleryLauncher.launch(pickImageIntent) }

        binding.nextBtn.setOnClickListener {
            viewModel.uploadProfileImage(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> {
                    binding.nextBtn.isEnabled = false
                }

                is OnBoardingUiState.Success -> {
                    binding.nextBtn.isEnabled = true
                    navigateToNext()
                }

                is OnBoardingUiState.Error -> {
                    binding.nextBtn.isEnabled = true

                    if (state.code.contains("ONBOARDING4001")) {
                        Log.d("ProfileFragment", "ONBOARDING4001 - 강제 이동")
                        navigateToNext()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun navigateToNext() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
            .addToBackStack(null)
            .commit()

        viewModel.resetState()
    }
}