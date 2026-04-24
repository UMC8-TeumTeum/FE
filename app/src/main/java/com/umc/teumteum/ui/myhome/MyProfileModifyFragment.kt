package com.umc.teumteum.ui.myhome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

    private var _binding: FragmentMyProfileModifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyHomeViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val modifyViewModel: ProfileModifyViewModel by activityViewModels()

    private var nicknameInitialized = false
    private var fieldInitialized = false

    companion object {
        private const val SELECT_PICTURE_BOTTOM_SHEET_TAG = "BottomSheetSelectPictureFragment"
    }

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileModifyBinding.inflate(inflater, container, false)
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

        homeViewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

        modifyViewModel.imageEditState.observe(viewLifecycleOwner) {
            renderEditProfileImagePreview()
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) {
            renderEditProfileImagePreview()
        }

        binding.profileIv.setOnClickListener {
            if(parentFragmentManager.findFragmentByTag(SELECT_PICTURE_BOTTOM_SHEET_TAG) != null) {
                return@setOnClickListener
            }

            val bottomSheet = BottomSheetSelectPictureFragment().apply {
                setOnGalleryClickListener {
                    val pickImageIntent = Intent(Intent.ACTION_PICK).apply {
                        type = "image/*"
                    }
                    galleryLauncher.launch(pickImageIntent)
                }

                setOnDefaultProfileClickListener {
                    modifyViewModel.setDefaultProfileImage()
                }
            }
//            bottomSheet.show(parentFragmentManager, "BottomSheetSelectPictureFragment")
            bottomSheet.show(parentFragmentManager, SELECT_PICTURE_BOTTOM_SHEET_TAG)
        }

        modifyViewModel.saveSuccess.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                modifyViewModel.consumeSaveSuccess()

                parentFragmentManager.setFragmentResult(
                    "profile_modify_result",
                    bundleOf("profile_updated" to true)
                )

                parentFragmentManager.popBackStack()
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
    }

    private fun renderProfileImage(imageUrl: String?) {
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.profileIv)
        } else {
            Glide.with(binding.profileIv).clear(binding.profileIv)
            binding.profileIv.setImageResource(R.drawable.gray_teum)
        }
    }

    private fun renderEditProfileImagePreview() {
        when (val state = modifyViewModel.imageEditState.value) {
            is ProfileModifyViewModel.ImageEditState.New -> {
                Glide.with(binding.profileIv).clear(binding.profileIv)
                binding.profileIv.setImageURI(state.uri)
            }
            is ProfileModifyViewModel.ImageEditState.Default -> {
                Glide.with(binding.profileIv).clear(binding.profileIv)
                binding.profileIv.setImageResource(R.drawable.gray_teum)
            }
            is ProfileModifyViewModel.ImageEditState.Keep, null -> {
                renderProfileImage(viewModel.profileImageUrl.value)
            }
        }
    }

    private fun updateTeumTime() {
        val days = homeViewModel.teumTimeDays.value ?: 0
        val hours = homeViewModel.teumTimeHours.value ?: 0
        val minutes = homeViewModel.teumTimeMinutes.value ?: 0
        binding.profileTimerTv.text = "${days}일 ${hours}시간 ${minutes}분"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}