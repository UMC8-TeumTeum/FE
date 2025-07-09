package com.example.teumteum.Friend

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetFriend02RejectBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class Friend02RejectBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetFriend02RejectBinding? = null
    private val binding get() = _binding!!

    private var selectedOption: SelectedOption = SelectedOption.REJECT

    enum class SelectedOption {
        REJECT, SUGGEST
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriend02RejectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 텍스트 설정
        binding.btnRejectMent.text = getColoredText("이때는 시간이 안돼요", " 멘트 보내기")
        binding.btnSuggestTime.text = getColoredText("가능한 다른 시간대", " 제안하기")

        // 기본 선택
        updateSelection(SelectedOption.REJECT)

        // 클릭 리스너
        binding.btnRejectMent.setOnClickListener {
            updateSelection(SelectedOption.REJECT)
        }

        binding.btnSuggestTime.setOnClickListener {
            updateSelection(SelectedOption.SUGGEST)
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSend.setOnClickListener {
            if (selectedOption == SelectedOption.SUGGEST) {
                // 화면 전환 (가능한 시간 제안)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_friend_container, Friend02PossibleTimeFragment())
                    .addToBackStack(null)
                    .commit()
                dismiss()
            } else {
                // 단순 거절 멘트 전송
                Toast.makeText(requireContext(), "멘트가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun updateSelection(option: SelectedOption) {
        selectedOption = option

        if (option == SelectedOption.REJECT) {
            binding.btnRejectMent.text = getColoredText("이때는 시간이 안돼요", " 멘트 보내기")
            binding.btnRejectMent.strokeColor = ColorStateList.valueOf(Color.parseColor("#7770FE"))
            binding.btnRejectMent.strokeWidth = 2

            binding.btnSuggestTime.text = getGrayText("가능한 다른 시간대 제안하기")
            binding.btnSuggestTime.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            binding.btnSuggestTime.strokeWidth = 0
        } else {
            binding.btnSuggestTime.text = getColoredText("가능한 다른 시간대", " 제안하기")
            binding.btnSuggestTime.strokeColor = ColorStateList.valueOf(Color.parseColor("#7770FE"))
            binding.btnSuggestTime.strokeWidth = 2

            binding.btnRejectMent.text = getGrayText("이때는 시간이 안돼요 멘트 보내기")
            binding.btnRejectMent.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
            binding.btnRejectMent.strokeWidth = 0
        }
    }

    private fun getColoredText(purplePart: String, blackPart: String): SpannableString {
        val fullText = purplePart + blackPart
        return SpannableString(fullText).apply {
            setSpan(
                ForegroundColorSpan(Color.parseColor("#7770FE")),
                0,
                purplePart.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.parseColor("#0F0F0F")),
                purplePart.length,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun getGrayText(fullText: String): SpannableString {
        return SpannableString(fullText).apply {
            setSpan(
                ForegroundColorSpan(Color.parseColor("#D3D3D3")),
                0,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
