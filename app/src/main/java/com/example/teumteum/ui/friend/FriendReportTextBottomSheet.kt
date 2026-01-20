package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetFriendReportTextBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendReportTextBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendReportTextBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return@setOnShowListener

            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { _, insets -> insets }

            bottomSheet.setPadding(0, 0, 0, 0)
            bottomSheet.setBackgroundResource(R.drawable.calendar_background)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriendReportTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 네비게이션 바
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, bottomInset)
            insets
        }

        val targetType = arguments?.getString(ARG_TARGET_TYPE) ?: return
        val targetId = arguments?.getLong(ARG_TARGET_ID) ?: return

        // ViewModel 결과 관찰 (성공/실패에 따라 토스트/버튼/닫기 처리)
        observeReportResult()

        binding.etReportDetail.addTextChangedListener { editable ->
            val length = editable?.length ?: 0
            binding.tvCharCount.text = "$length / 100"
        }

        binding.btnBack.setOnClickListener {
            FriendReportChoiceBottomSheet
                .newInstance(targetType, targetId)
                .show(parentFragmentManager, "FriendReportChoiceBottomSheet")
            dismiss()
        }

        binding.btnReportSubmit.setOnClickListener {
            val trimmed = binding.etReportDetail.text?.toString()?.trim()

            if (trimmed.isNullOrBlank()) {
                Toast.makeText(requireContext(), "사유를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지 (응답 오기 전까지 비활성화)
            binding.btnReportSubmit.isEnabled = false

            viewModel.createReport(
                targetType = targetType,
                targetId = targetId,
                reasonId = 14,
                otherReason = trimmed
            )
        }
    }

    private fun observeReportResult() {
        viewModel.successMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                // 실패 시 재시도 가능하도록 버튼 다시 활성화
                binding.btnReportSubmit.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TARGET_TYPE = "arg_target_type"
        private const val ARG_TARGET_ID = "arg_target_id"

        fun newInstance(targetType: String, targetId: Long): FriendReportTextBottomSheet {
            return FriendReportTextBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TARGET_TYPE, targetType)
                    putLong(ARG_TARGET_ID, targetId)
                }
            }
        }
    }
}
