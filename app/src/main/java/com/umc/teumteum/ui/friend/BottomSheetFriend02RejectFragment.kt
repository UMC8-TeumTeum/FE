package com.umc.teumteum.ui.friend

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.R
import com.umc.teumteum.databinding.BottomSheetFriend02RejectBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt

@AndroidEntryPoint
class Friend02RejectBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriend02RejectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendViewModel by activityViewModels()

    private var selectedOption: SelectedOption = SelectedOption.REJECT

    enum class SelectedOption {
        REJECT, SUGGEST
    }

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriend02RejectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = viewModel.selectedTeum.value
        if (item == null) {
            dismiss()
            return
        }

        binding.btnRejectMent.text = getColoredText("이때는 시간이 안돼요", " 멘트 보내기")
        binding.btnSuggestTime.text = getColoredText("가능한 다른 시간대", " 제안하기")
        updateSelection(SelectedOption.REJECT)

        binding.btnRejectMent.setOnClickListener {
            updateSelection(SelectedOption.REJECT)
        }

        binding.btnSuggestTime.setOnClickListener {
            updateSelection(SelectedOption.SUGGEST)
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSend.setOnClickListener {
            val responseId = arguments?.getInt("responseId") ?: return@setOnClickListener

            if (selectedOption == SelectedOption.SUGGEST) {
                // Friend02RequestFragment에서 받아온 teumList 전달
                val parentFragment = parentFragmentManager.fragments.firstOrNull { it is Friend02RequestFragment } as? Friend02RequestFragment
                val teumList = parentFragment?.teumList ?: emptyList()

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.main_frm,
                        Friend02PossibleTimeFragment.newInstance(ArrayList(teumList), responseId)
                    )
                    .addToBackStack(null)
                    .commit()
                dismiss()
            } else {
                val status = "rejected"

                Log.d("REJECT_BOTTOM_SHEET", "responseId: $responseId, status: $status")
                Log.d("REJECT_BOTTOM_SHEET", "틈 요청을 거절하였습니다.")

                // 응답 처리
                viewModel.respondToTeum(responseId, status)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, FriendRejectSendFragment())
                    .addToBackStack(null)
                    .commit()

                dismiss()
            }
        }
    }

    private fun updateSelection(option: SelectedOption) {
        selectedOption = option

        if (option == SelectedOption.REJECT) {
            binding.btnRejectMent.text = getColoredText("이때는 시간이 안돼요", " 멘트 보내기")
            binding.btnRejectMent.strokeColor = ColorStateList.valueOf("#7770FE".toColorInt())
            binding.btnRejectMent.strokeWidth = 2

            binding.btnSuggestTime.text = getGrayText("가능한 다른 시간대 제안하기")
            binding.btnSuggestTime.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            binding.btnSuggestTime.strokeWidth = 0
        } else {
            binding.btnSuggestTime.text = getColoredText("가능한 다른 시간대", " 제안하기")
            binding.btnSuggestTime.strokeColor = ColorStateList.valueOf("#7770FE".toColorInt())
            binding.btnSuggestTime.strokeWidth = 2

            binding.btnRejectMent.text = getGrayText("이때는 시간이 안돼요 멘트 보내기")
            binding.btnRejectMent.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            binding.btnRejectMent.strokeWidth = 0
        }
    }

    private fun getColoredText(purplePart: String, blackPart: String): SpannableString {
        val fullText = purplePart + blackPart
        return SpannableString(fullText).apply {
            setSpan(ForegroundColorSpan("#7770FE".toColorInt()), 0, purplePart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan("#0F0F0F".toColorInt()), purplePart.length, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun getGrayText(fullText: String): SpannableString {
        return SpannableString(fullText).apply {
            setSpan(ForegroundColorSpan("#D3D3D3".toColorInt()), 0, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(responseId: Int): Friend02RejectBottomSheetFragment {
            return Friend02RejectBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("responseId", responseId)
                }
            }
        }
    }
}
