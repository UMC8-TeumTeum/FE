package com.example.teumteum.ui.friend

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetFriendReportChoiceBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendReportChoiceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendReportChoiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    //  서버용: reasonId를 저장
    private var selectedReasonId: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            bottomSheet.setBackgroundResource(R.drawable.calendar_background)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriendReportChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyCheckBoxTint()

        observeReportResult()

        // 1~6만 선택 가능
        setupSingleSelection(binding.optAbuseCb, 8)
        setupSingleSelection(binding.optSexualCb, 9)
        setupSingleSelection(binding.optScamCb, 10)
        setupSingleSelection(binding.optIllegalCb, 11)
        setupSingleSelection(binding.optPrivacyCb, 12)
        setupSingleSelection(binding.optSpamCb, 13)

        // 14번은 무조건 텍스트 바텀시트로 이동 (여기서 신고 X)
        binding.optOtherArrow.setOnClickListener {
            val targetType =
                requireArguments().getString(ARG_TARGET_TYPE) ?: return@setOnClickListener
            val targetId = requireArguments().getLong(ARG_TARGET_ID)

            FriendReportTextBottomSheet
                .newInstance(targetType, targetId)
                .show(parentFragmentManager, "FriendReportTextBottomSheet")

            dismiss()
        }

        // 신고 버튼: 8~13만 여기서 신고됨
        binding.reportBtn.setOnClickListener {

            val reasonId = selectedReasonId
            if (reasonId == null) {
                return@setOnClickListener
            }

            val targetType = arguments?.getString(ARG_TARGET_TYPE)
            val targetId = arguments?.getLong(ARG_TARGET_ID, -1L) ?: -1L

            if (targetType.isNullOrBlank() || targetId <= 0L) {
                return@setOnClickListener
            }

            viewModel.createReport(
                targetType = targetType,
                targetId = targetId,
                reasonId = reasonId,
                otherReason = null
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
            }
        }
    }

    private fun setupSingleSelection(checkBox: CheckBox, reasonId: Int) {
        checkBox.setOnClickListener {
            clearAllChecks()
            checkBox.isChecked = true
            selectedReasonId = reasonId
        }
    }

    private fun applyCheckBoxTint() {
        val tint = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // checked
                intArrayOf(-android.R.attr.state_checked) // unchecked
            ),
            intArrayOf(
                Color.parseColor("#0F0F0F"), // 체크됨
                Color.parseColor("#788084")  // 체크 안됨
            )
        )

        listOf(
            binding.optAbuseCb,
            binding.optSexualCb,
            binding.optScamCb,
            binding.optIllegalCb,
            binding.optPrivacyCb,
            binding.optSpamCb
        ).forEach { cb ->
            cb.buttonTintList = tint
        }
    }

    /** 전체 체크 해제 */
    private fun clearAllChecks() {
        binding.optAbuseCb.isChecked = false
        binding.optSexualCb.isChecked = false
        binding.optScamCb.isChecked = false
        binding.optIllegalCb.isChecked = false
        binding.optPrivacyCb.isChecked = false
        binding.optSpamCb.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TARGET_TYPE = "arg_target_type"
        private const val ARG_TARGET_ID = "arg_target_id"

        fun newInstance(targetType: String, targetId: Long) = FriendReportChoiceBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_TARGET_TYPE, targetType) // "USER" | "TEUM_REQUEST"
                putLong(ARG_TARGET_ID, targetId)
            }
        }
    }
}
