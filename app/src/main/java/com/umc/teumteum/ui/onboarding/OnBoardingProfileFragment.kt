package com.umc.teumteum.ui.onboarding

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentOnBoardingProfileBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.ui.myhome.BottomSheetSelectPictureFragment
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingUiState
import com.umc.teumteum.ui.onboarding.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingProfileFragment : Fragment() {

    private var _binding: FragmentOnBoardingProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnBoardingViewModel by activityViewModels()

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setProfileImage(uri)
                renderProfileImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nickname = viewModel.nickname.value
        binding.titleTv.text = "$nickname 님"
        binding.nicknameTv.text = nickname

        renderProfileImage(viewModel.profileImageUri.value)

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

        binding.galleryFl.setOnClickListener {
            showSelectPictureBottomSheet()
        }

//        binding.cameraBtn.setOnClickListener {
//            showSelectPictureBottomSheet()
//        }

        binding.nextBtn.setOnClickListener {
            viewModel.uploadProfileImage(requireContext())
        }
    }

    private fun showSelectPictureBottomSheet() {
        val bottomSheet = BottomSheetSelectPictureFragment().apply {
            setOnGalleryClickListener {
                val pickImageIntent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                galleryLauncher.launch(pickImageIntent)
            }

            setOnDefaultProfileClickListener {
                viewModel.clearProfileImage()
                renderProfileImage(null)
            }
        }

        bottomSheet.show(parentFragmentManager, "BottomSheetSelectPictureFragment")
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
                }

                else -> Unit
            }
        }

        viewModel.profileImageUri.observe(viewLifecycleOwner) { uri ->
            renderProfileImage(uri)
        }
    }

    private fun renderProfileImage(uri: Uri?) {
        if (uri != null) {
            binding.profileIv.setImageURI(uri)
//            binding.cameraBtn.visibility = View.GONE
        } else {
            binding.profileIv.setImageResource(R.drawable.gray_teum)
//            binding.cameraBtn.visibility = View.VISIBLE
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