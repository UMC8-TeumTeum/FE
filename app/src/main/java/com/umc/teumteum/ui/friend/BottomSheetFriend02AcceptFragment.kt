package com.umc.teumteum.ui.friend

import android.app.Dialog
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
import com.umc.teumteum.databinding.BottomSheetFriend02AcceptBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetFriend02AcceptFragment : BottomSheetDialogFragment() {

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

        val item = viewModel.selectedTeum.value
        if (item == null) {
            dismiss()
            return
        }

        val fullText = "함께 할래요 멘트를 보낼까요?"
        val spannable = SpannableString(fullText).apply {
            setSpan(ForegroundColorSpan(requireContext().getColor(R.color.main_1)), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(requireContext().getColor(R.color.text_primary)), 6, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.mentText.text = spannable

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSend.setOnClickListener {
            val responseId = arguments?.getInt("responseId") ?: return@setOnClickListener
            val status = "accepted"

            Log.d("ACCEPT_BOTTOM_SHEET", "responseId: $responseId, status: $status")

            binding.btnSend.isEnabled = false

            // 응답 처리
            viewModel.respondToTeum(responseId, status)

            dismiss()

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
        fun newInstance(responseId: Int): BottomSheetFriend02AcceptFragment {
            return BottomSheetFriend02AcceptFragment().apply {
                arguments = Bundle().apply {
                    putInt("responseId", responseId)
                }
            }
        }
    }
}
