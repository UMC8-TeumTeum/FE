package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import androidx.core.widget.addTextChangedListener
import com.example.teumteum.databinding.BottomSheetFriendReportTextBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendReportTextBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendReportTextBinding? = null
    private val binding get() = _binding!!

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
        _binding = BottomSheetFriendReportTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 실시간 글자수 표시
        binding.etReportDetail.addTextChangedListener { editable ->
            val length = editable?.length ?: 0
            binding.tvCharCount.text = "$length / 100"
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            // 이전 바텀시트 열기
            val previousSheet = FriendReportChoiceBottomSheet()
            previousSheet.show(parentFragmentManager, "FriendReportChoiceBottomSheet")

            // 현재 바텀시트 닫기
            dismiss()
        }


        // 신고 버튼 클릭
        binding.btnReportSubmit.setOnClickListener {
            val detailText = binding.etReportDetail.text.toString().trim()

            if (detailText.isEmpty()) {
                // 필요시 토스트
                // Toast.makeText(requireContext(),"사유를 입력해주세요.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: API로 detailText 전달
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
