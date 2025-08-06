package com.example.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMyHomeBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyHomeFragment : Fragment() {

    lateinit var binding: FragmentMyHomeBinding

    private val viewModel: MyHomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 내 정보 요청
        if(!viewModel.isLoaded){
            viewModel.getMyInfo()
        }

        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.nicknameTv.text = nickname ?: "닉네임"
        }

        viewModel.field.observe(viewLifecycleOwner) { field ->
            binding.fieldTv.text = if (!field.isNullOrBlank()) "  •  $field" else "  •  직업 없음"
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_teum) // 기본 이미지 리소스
                    .error(R.drawable.gray_teum)       // 에러 시 이미지
                    .into(binding.profileIv)
            } else {
                binding.profileIv.setImageResource(R.drawable.gray_teum)
            }
        }

        binding.profileIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.nicknameTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.fieldTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.arrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.settingIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MySettingFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
    }

}