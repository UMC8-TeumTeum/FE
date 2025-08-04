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
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import com.example.teumteum.databinding.FragmentOnBoardingProfileBinding
import com.example.teumteum.ui.signup.viewModel.OnBoardingUiState
import com.example.teumteum.ui.signup.viewModel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

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

        val nickname = arguments?.getString("nickname").orEmpty()
        binding.titleTv.text = "$nickname 님"
        binding.nicknameTv.text = nickname

        observeViewModel()

        val pickImageIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        binding.galleryFl.setOnClickListener { galleryLauncher.launch(pickImageIntent) }
        binding.cameraBtn.setOnClickListener { galleryLauncher.launch(pickImageIntent) }

        binding.nextBtn.setOnClickListener {
            val uri = lastSelectedImageUri
            if (uri != null) {
                val contentType = getMimeType(uri)
                viewModel.requestPresignedUrl(PresignedRequest(contentType))
            } else {
                viewModel.resetState()
                navigateToNext()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OnBoardingUiState.Loading -> {
                    binding.nextBtn.isEnabled = false
                }

                is OnBoardingUiState.PresignedSuccess -> {
                    lastSelectedImageUri?.let { uri ->
                        uploadImageToS3(state.presignedUrl, uri, state.contentType) {
                            viewModel.postProfileImage(ProfileImageRequest(state.fileName))
                        }
                    }
                }

                is OnBoardingUiState.Success -> {
                    binding.nextBtn.isEnabled = true
                    navigateToNext()
                }

                is OnBoardingUiState.Error -> {
                    binding.nextBtn.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                    if (state.code.contains("ONBOARDING4001")) {
                        navigateToNext()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun uploadImageToS3(url: String, imageUri: Uri, contentType: String, onSuccess: () -> Unit) {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val bytes = inputStream?.readBytes() ?: return
        val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())

        val request = Request.Builder().url(url).put(requestBody).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Upload", "S3 업로드 실패: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    binding.nextBtn.isEnabled = true
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    Log.d("Upload", "S3 업로드 성공")
                    requireActivity().runOnUiThread { onSuccess() }
                } else {
                    Log.e("Upload", "S3 업로드 실패: ${response.code}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                        binding.nextBtn.isEnabled = true
                    }
                }
            }
        })
    }

    private fun getMimeType(uri: Uri): String {
        return requireContext().contentResolver.getType(uri) ?: "image/jpeg"
    }

    private fun navigateToNext() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
            .addToBackStack(null)
            .commit()

        viewModel.resetState()
    }
}