package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    // ViewModel 추가 (activity 범위 공유)
    private val viewModel: FriendViewModel by activityViewModels()

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
        _binding = BottomSheetFriendReportTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // target 정보 꺼내기 (없으면 그냥 종료)
        val targetType = arguments?.getString(ARG_TARGET_TYPE) ?: return
        val targetId = arguments?.getLong(ARG_TARGET_ID) ?: return

        // 실시간 글자수 표시
        binding.etReportDetail.addTextChangedListener { editable ->
            val length = editable?.length ?: 0
            binding.tvCharCount.text = "$length / 100"
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            FriendReportChoiceBottomSheet
                .newInstance(targetType, targetId)
                .show(parentFragmentManager, "FriendReportChoiceBottomSheet")
            dismiss()
        }

        // 신고 버튼: 14번(기타) 신고는 여기서만
        binding.btnReportSubmit.setOnClickListener {
            val raw = binding.etReportDetail.text?.toString()
            val trimmed = raw?.trim()

            Log.e("REPORT_TEXT", "raw=[$raw], trimmed=[$trimmed], len=${trimmed?.length}")

            if (trimmed.isNullOrBlank()) {
                Toast.makeText(requireContext(), "사유를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 클릭 방지
            binding.btnReportSubmit.isEnabled = false

            viewModel.createReport(
                targetType = targetType,
                targetId = targetId,
                reasonId = 14,
                otherReason = trimmed
            )

            Toast.makeText(requireContext(), "신고가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show()

            dismiss()
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
