package com.example.teumteum.ui.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.teumteum.R
import com.example.teumteum.data.remote.onboarding.OnBoardingService
import com.example.teumteum.data.remote.onboarding.model.PresignedRequest
import com.example.teumteum.databinding.FragmentOnBoardingProfileBinding
import com.example.teumteum.ui.signup.view.ProfileImageView
import com.example.teumteum.data.remote.onboarding.dto.PresignedFileInfo
import com.example.teumteum.data.remote.onboarding.model.ProfileImageRequest
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingProfileFragment : Fragment(), ProfileImageView {

    private lateinit var binding: FragmentOnBoardingProfileBinding

    private var lastSelectedImageUri: Uri? = null

    @Inject
    lateinit var onBoardingService: OnBoardingService

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            Log.d("Gallery", "선택된 이미지: $imageUri")

            lastSelectedImageUri = imageUri

            imageUri?.let { uri ->
                binding.profileIv.setImageURI(uri)  // 선택한 이미지 적용
                binding.cameraBtn.visibility = View.GONE  // 카메라 버튼 숨김
            }
        }
    }

    override fun onPresignedSuccess(code: String, result: PresignedFileInfo) {
        val msg = "프리사인드 URL 요청 성공 (code: $code)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("PROFILE_FRAGMENT", msg)

        Log.d("Presigned", "URL 발급 성공: $code, url=${result.presignedUrl}")

        val uri = lastSelectedImageUri ?: return
        val contentType = getMimeType(uri)

        //presigned URL로 이미지 업로드
        uploadImageToS3(result.presignedUrl, uri, contentType) {
            //업로드 성공 후 등록 API
            val request = ProfileImageRequest(result.fileName)
            onBoardingService.setProfileImageView(this)
            onBoardingService.postProfileImage(request)
        }

    }

    override fun onPresignedFailure(code: String, message: String?) {
        val msg = "프리사인드 URL 요청 실패 (code: $code, message: ${message ?: "없음"})"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.e("PROFILE_FRAGMENT", msg)

        //닉네임 중복
        if (message?.contains("ONBOARDING4004") == true) {
            Toast.makeText(requireContext(), "지원하지 않는 이미지 형식", Toast.LENGTH_SHORT).show()
        }

        //온보딩 단계가 아닐 경우 - 이후 테스트를 위해 화면 이동하도록 구현
        if (message?.contains("ONBOARDING4001") == true) {
            Toast.makeText(requireContext(), "온보딩 단계가 아닙니다.", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onProfileImageSuccess(code: String) {
        val msg = "프로필 이미지 등록 성공 (code: $code)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("PROFILE_FRAGMENT", msg)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
            .addToBackStack(null)
            .commit()

    }

    override fun onProfileImageFailure(code: String, message: String?) {
        val msg = "프로필 이미지 등록 실패 (code: $code, message: ${message ?: "없음"})"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.e("PROFILE_FRAGMENT", msg)

        //닉네임 중복
        if (message?.contains("ONBOARDING4005") == true) {
            Toast.makeText(requireContext(), "잘못된 파일명", Toast.LENGTH_SHORT).show()
        }

        //온보딩 단계가 아닐 경우 - 이후 테스트를 위해 화면 이동하도록 구현
        if (message?.contains("ONBOARDING4001") == true) {
            Toast.makeText(requireContext(), "온보딩 단계가 아닙니다.", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
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
        binding = FragmentOnBoardingProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(25)

        val nickname = arguments?.getString("nickname")
        setNickname(nickname.toString())

        binding.galleryFl.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            galleryLauncher.launch(intent)
        }

        binding.cameraBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            galleryLauncher.launch(intent)
        }


        binding.nextBtn.setOnClickListener {
//            startActivity(Intent(requireContext(), MainActivity::class.java))
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
//                .addToBackStack(null)
//                .commit()
            lastSelectedImageUri?.let { uri ->
                val contentType = getMimeType(uri)
                val request = PresignedRequest(contentType)
                onBoardingService.setProfileImageView(this)
                onBoardingService.requestPresignedUrl(request)
            } ?: run {
                // 이미지 선택 안 한 경우 처리
                Log.w("Profile", "이미지 선택 안 됨")
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, OnBoardingSleepPatternFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

    }

    private fun setNickname(nickname: String){
        binding.titleTv.text = nickname + " 님"
        binding.nicknameTv.text = nickname
    }

    private fun getMimeType(uri: Uri): String {
        val contentResolver = requireContext().contentResolver
        return contentResolver.getType(uri) ?: "image/jpeg"
    }

    private fun uploadImageToS3(url: String, imageUri: Uri, contentType: String, onSuccess: () -> Unit) {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val bytes = inputStream?.readBytes() ?: return

        val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Upload", "실패: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    Log.d("Upload", "성공")
                    onSuccess()
                } else {
                    Log.e("Upload", "실패: ${response.code}")
                }
            }
        })
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 1001
    }
}