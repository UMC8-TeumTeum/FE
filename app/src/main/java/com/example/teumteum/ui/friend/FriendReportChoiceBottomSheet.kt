package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetFriendReportChoiceBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendReportChoiceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendReportChoiceBinding? = null
    private val binding get() = _binding!!

    private var selectedReason: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
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

        setupSingleSelection(binding.optAbuseCb, "욕설 등 혐오 발언")
        setupSingleSelection(binding.optSexualCb, "성희롱 또는 음란 발언")
        setupSingleSelection(binding.optScamCb, "사기 또는 거짓")
        setupSingleSelection(binding.optIllegalCb, "불법 또는 유해 콘텐츠 공유")
        setupSingleSelection(binding.optPrivacyCb, "개인정보 노출 요구")
        setupSingleSelection(binding.optSpamCb, "스팸")

        // "해당 리스트에 관련 사유 없음" → 다음 바텀시트 이동
        binding.optOtherArrow.setOnClickListener {
            val sheet = FriendReportTextBottomSheet()
            sheet.show(parentFragmentManager, "FriendReportTextBottomSheet")
            dismiss() // 이전 바텀시트 닫기 (선택)
        }

        //  신고 버튼 클릭
        binding.reportBtn.setOnClickListener {
            if (selectedReason == null) {
                // 필요 시 토스트:
                // Toast.makeText(requireContext(), "신고 사유를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: API 연결
            dismiss()
        }
    }

    /** 체크박스 단일 선택 처리 */
    private fun setupSingleSelection(checkBox: CheckBox, reason: String) {
        checkBox.setOnClickListener {
            clearAllChecks()
            checkBox.isChecked = true
            selectedReason = reason
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
}
