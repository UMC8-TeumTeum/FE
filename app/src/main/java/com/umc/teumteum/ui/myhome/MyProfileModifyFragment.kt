package com.umc.teumteum.ui.myhome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentMyProfileModifyBinding
import com.umc.teumteum.ui.main.MainActivity
import com.umc.teumteum.ui.main.viewModel.HomeViewModel
import com.umc.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.umc.teumteum.ui.myhome.viewModel.ProfileModifyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max

@AndroidEntryPoint
class MyProfileModifyFragment : Fragment() {

    private lateinit var binding: FragmentMyProfileModifyBinding

    private val viewModel: MyHomeViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    // 수정 전용 ViewModel
    private val modifyViewModel: ProfileModifyViewModel by activityViewModels()

    private var nicknameInitialized = false
    private var fieldInitialized = false

    // 갤러리에서 이미지 선택
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                modifyViewModel.setTempImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileModifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        ViewCompat.setOnApplyWindowInsetsListener(binding.profileScroll) { v, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val bottomPadding = if (imeBottom > 0) max(imeBottom, sysBottom) else 0

            v.updatePadding(bottom = bottomPadding)
            insets
        }
        ViewCompat.requestApplyInsets(binding.profileScroll)

        modifyViewModel.startEdit(
            nickname = viewModel.nickname.value,
            field = viewModel.field.value,
            profileUrl = viewModel.profileImageUrl.value
        )

        binding.cancelTv.setOnClickListener {
            modifyViewModel.cancelEdit()
            parentFragmentManager.popBackStack()
        }

        binding.completeTv.setOnClickListener {
            modifyViewModel.setTempNickname(binding.profileNicknameTv.text?.toString()?.trim())
            modifyViewModel.setTempField(binding.profileFieldTv.text?.toString()?.trim())
            modifyViewModel.commit(requireContext())
        }

        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            if (!nicknameInitialized) {
                binding.profileNicknameTv.setText(nickname.orEmpty())
                binding.profileNicknameTv.setSelection(binding.profileNicknameTv.text.length)
                nicknameInitialized = true
            }
        }

        viewModel.field.observe(viewLifecycleOwner) { field ->
            if (!fieldInitialized) {
                binding.profileFieldTv.setText(field.orEmpty())
                binding.profileFieldTv.setSelection(binding.profileFieldTv.text.length)
                fieldInitialized = true
            }
        }

        // 타이머
        homeViewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

        modifyViewModel.tempImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                binding.profileIv.setImageURI(uri)
            }
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (modifyViewModel.tempImageUri.value != null) return@observe

            if (!imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_teum)
                    .error(R.drawable.gray_teum)
                    .into(binding.profileIv)
            } else {
                binding.profileIv.setImageResource(R.drawable.gray_teum)
            }
        }

        val pickImageIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        binding.profileIv.setOnClickListener {
            galleryLauncher.launch(pickImageIntent)
        }

        modifyViewModel.saveSuccess.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                // 성공 후 내 프로필 재조회
                viewModel.getMyInfo()

                parentFragmentManager.popBackStack()
                modifyViewModel.consumeSaveSuccess()
            }
        }

        modifyViewModel.saveError.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                modifyViewModel.clearError()
            }
        }

        fun renderTimePublicUi(isPublic: Boolean) {
            binding.timerLockIv.setImageResource(
                if (isPublic) R.drawable.ic_unlock_sv else R.drawable.ic_lock_sv
            )
        }

        modifyViewModel.timePublic.observe(viewLifecycleOwner) { isPublic ->
            renderTimePublicUi(isPublic == true)
        }

        binding.profileTimerTv.setOnClickListener {
            val next = !(modifyViewModel.timePublic.value ?: true)
            modifyViewModel.setTimePublic(next)
        }
        binding.timerLockIv.setOnClickListener {
            val next = !(modifyViewModel.timePublic.value ?: true)
            modifyViewModel.setTimePublic(next)
        }

        modifyViewModel.saveSuccess.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                // 재조회
                viewModel.getMyInfo()

                viewModel.profileImageUrl.observe(viewLifecycleOwner) {
                    parentFragmentManager.popBackStack()
                }

                modifyViewModel.consumeSaveSuccess()
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