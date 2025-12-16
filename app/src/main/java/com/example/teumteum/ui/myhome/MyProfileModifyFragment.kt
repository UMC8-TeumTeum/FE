package com.example.teumteum.ui.myhome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMyProfileBinding
import com.example.teumteum.databinding.FragmentMyProfileModifyBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MyProfileModifyFragment : Fragment() {

    private lateinit var binding: FragmentMyProfileModifyBinding
    private val viewModel: MyHomeViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyProfileModifyBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.cancelTv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.profileNicknameTv.text = nickname ?: "닉네임"
        }

        viewModel.field.observe(viewLifecycleOwner) { field ->
            binding.profileFieldTv.text = if (!field.isNullOrBlank()) "$field" else "직업 없음"
        }

        homeViewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

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
    }

    private fun updateTeumTime() {
        val days = homeViewModel.teumTimeDays.value ?: 0
        val hours = homeViewModel.teumTimeHours.value ?: 0
        val minutes = homeViewModel.teumTimeMinutes.value ?: 0
        binding.profileTimerTv.text = "${days}일 ${hours}시간 ${minutes}분"
    }

}