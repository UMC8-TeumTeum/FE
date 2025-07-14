package com.example.teumteum.ui.friend

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetFriend02AcceptBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class Friend02AcceptBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriend02AcceptBinding? = null
    private val binding get() = _binding!!

    /** ↓ BottomSheetDialog 배경 커스텀 */
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
        _binding = BottomSheetFriend02AcceptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "함께 할래요"만 보라색, 나머지는 검정
        val fullText = "함께 할래요 멘트를 보낼까요?"
        val spannable = SpannableString(fullText).apply {
            setSpan(
                ForegroundColorSpan(Color.parseColor("#7770FE")),
                0, 6,                              // "함께 할래요"
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.parseColor("#0F0F0F")),
                6, fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.mentText.text = spannable

        // 취소
        binding.btnCancel.setOnClickListener { dismiss() }

        // 전송 후 FriendSendFragment로 이동
        binding.btnSend.setOnClickListener {
            dismiss()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendSendFragment())   // main_frm 확인 필요
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
