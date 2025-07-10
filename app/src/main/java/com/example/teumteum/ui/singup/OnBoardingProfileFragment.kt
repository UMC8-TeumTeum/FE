package com.example.teumteum.ui.singup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.teumteum.databinding.FragmentOnBoardingProfileBinding
import com.example.teumteum.ui.main.MainActivity

class OnBoardingProfileFragment : Fragment() {

    private lateinit var binding: FragmentOnBoardingProfileBinding

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            Log.d("Gallery", "선택된 이미지: $imageUri")
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
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

    }

    private fun setNickname(nickname: String){
        binding.titleTv.text = nickname + " 님"
        binding.nicknameTv.text = nickname
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 1001
    }
}