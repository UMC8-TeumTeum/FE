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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendReportChoiceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendReportChoiceBinding? = null
    private val binding get() = _binding!!

    // 선택된 신고 사유 저장용 변수
    private var selectedReason: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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

        // 신고 사유 목록 클릭 시 체크박스 하나만 선택되도록 설정
        setupSingleSelection(binding.clReportHate, binding.checkboxHate, "욕설 등 혐오 발언")
        setupSingleSelection(binding.clReportSexual, binding.checkboxSexual, "성희롱 또는 음란 발언")
        setupSingleSelection(binding.clReportFalse, binding.checkboxFalse, "사기 또는 거짓")
        setupSingleSelection(binding.clReportIllegal, binding.checkboxIllegal, "불법 또는 유해 콘텐츠 공유")
        setupSingleSelection(binding.clReportInformation, binding.checkboxInformation, "개인정보 노출 요구")
        setupSingleSelection(binding.clReportMessage, binding.checkboxMessage, "스팸")

        // "해당 리스트에 관련 사유 없음" 클릭 시 → 다음 BottomSheet (직접 입력)
//        binding.clReportOther.setOnClickListener {
//            val bottomSheet = FriendReportCustomBottomSheet()
//            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
//            dismiss()
//        }

        // 신고 버튼 클릭
        binding.btnReportSubmit.setOnClickListener {
            if (selectedReason == null) {
                // 선택 안 하면 무시하거나 Toast 띄워도 됨
                return@setOnClickListener
            }

            // TODO: viewModel을 통해 신고 API 연동
            dismiss()
        }
    }

    /**
     * ConstraintLayout 클릭 시 해당 CheckBox 선택, 나머지는 해제
     */
    private fun setupSingleSelection(layout: ConstraintLayout, checkBox: CheckBox, reason: String) {
        layout.setOnClickListener {
            clearAllCheckBoxes()
            checkBox.isChecked = true
            selectedReason = reason
        }

        checkBox.setOnClickListener {
            clearAllCheckBoxes()
            checkBox.isChecked = true
            selectedReason = reason
        }
    }

    /** 모든 체크박스 해제 */
    private fun clearAllCheckBoxes() {
        binding.checkboxHate.isChecked = false
        binding.checkboxSexual.isChecked = false
        binding.checkboxFalse.isChecked = false
        binding.checkboxIllegal.isChecked = false
        binding.checkboxInformation.isChecked = false
        binding.checkboxMessage.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
