package com.umc.teumteum.ui.friend

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.databinding.BottomSheetFriendTeumDeleteBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.graphics.toColorInt

class BottomSheetFriendTeumDeleteFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendTeumDeleteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    // arguments에서 scheduleId 읽기
    private val scheduleId: Int by lazy {
        requireArguments().getInt(ARG_SCHEDULE_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriendTeumDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "취소" 텍스트 색상 강조
        val text = "약속된 틈을 정말 취소할까요?"
        val spannable = SpannableString(text)
        val start = text.indexOf("취소")
        spannable.setSpan(
            ForegroundColorSpan("#7770FE".toColorInt()),
            start,
            start + 2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvTitle.text = spannable

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            binding.btnYes.isEnabled = false
            viewModel.cancelTeumSchedule(scheduleId)
            Log.d("CANCEL_DEBUG", "취소 요청 scheduleId=$scheduleId")
        }

        viewModel.cancelScheduleSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { canceledId ->
                if (canceledId == scheduleId) {
                    dismissAllowingStateLoss()
                    Log.d(
                        "BOTTOM_SHEET",
                        "cancelScheduleSuccess로 닫힘 scheduleId=$canceledId"
                    )
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Log.e("DELETE_CONFIRM", it)
                binding.btnYes.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SCHEDULE_ID = "ARG_SCHEDULE_ID"

        fun newInstance(scheduleId: Int) =
            BottomSheetFriendTeumDeleteFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SCHEDULE_ID, scheduleId)
                }
            }
    }
}
