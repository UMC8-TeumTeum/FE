package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.teumteum.databinding.BottomSheetFriendTeumDeleteBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FriendTeumDeleteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendTeumDeleteBinding? = null
    private val binding get() = _binding!!

    // Activity 범위 ViewModel 유지
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
            ForegroundColorSpan(Color.parseColor("#7770FE")),
            start,
            start + 2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvTitle.text = spannable

        /** 아니오 */
        binding.btnNo.setOnClickListener {
            dismiss()
        }

        /** 네 → 취소 API */
        binding.btnYes.setOnClickListener {
            binding.btnYes.isEnabled = false
            viewModel.cancelTeumSchedule(scheduleId)
            Log.d("CANCEL_DEBUG", "취소 요청 scheduleId=$scheduleId")
        }

        /** 취소 성공 이벤트로만 닫기 */
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

        /** 실패 처리 */
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

        // 정석 생성 방식
        fun newInstance(scheduleId: Int) =
            FriendTeumDeleteBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SCHEDULE_ID, scheduleId)
                }
            }
    }
}
