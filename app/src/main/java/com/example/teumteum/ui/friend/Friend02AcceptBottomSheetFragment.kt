package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.teumteum.databinding.BottomSheetFriend02AcceptBinding

class Friend02AcceptBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriend02AcceptBinding? = null
    private val binding get() = _binding!!

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

        // "함께 할래요"만 보라색, 나머지 검정
        val fullText = "함께 할래요 멘트를 보낼까요?"
        val spannable = SpannableString(fullText).apply {
            setSpan(
                ForegroundColorSpan(Color.parseColor("#7770FE")),
                0, 6, // "함께 할래요"
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.parseColor("#0F0F0F")),
                6, fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.mentText.text = spannable

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSend.setOnClickListener {
            // 전송 처리
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
