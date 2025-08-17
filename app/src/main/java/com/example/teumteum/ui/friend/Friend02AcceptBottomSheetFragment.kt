package com.example.teumteum.ui.friend

import android.app.Dialog
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
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.example.teumteum.databinding.BottomSheetFriend02AcceptBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Friend02AcceptBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriend02AcceptBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendViewModel by activityViewModels()

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

        val fullText = "함께 할래요 멘트를 보낼까요?"
        val spannable = SpannableString(fullText).apply {
            setSpan(ForegroundColorSpan(Color.parseColor("#7770FE")), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(Color.parseColor("#0F0F0F")), 6, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.mentText.text = spannable

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSend.setOnClickListener {
            val responseId = arguments?.getInt("responseId") ?: return@setOnClickListener
            val status = "accepted"

            //  로그 & 토스트
            Log.d("ACCEPT_BOTTOM_SHEET", "responseId: $responseId, status: $status")

            //  응답 처리
            viewModel.respondToTeum(responseId, status)

            // 바텀시트 닫기
            dismiss()

            // 화면 전환
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendSendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(responseId: Int): Friend02AcceptBottomSheetFragment {
            return Friend02AcceptBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("responseId", responseId)
                }
            }
        }
    }
}
