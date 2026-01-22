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

class FriendTeumDeleteBottomSheet(
    private val scheduleId: Int
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendTeumDeleteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

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

        // "취소" 글자만 색상 변경
        val text = "약속된 틈을 정말 취소할까요?"
        val spannable = SpannableString(text)
        val start = text.indexOf("취소")
        val end = start + 2
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#7770FE")),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvTitle.text = spannable

        /** 아니오 → 그냥 닫기 */
        binding.btnNo.setOnClickListener {
            dismiss()
        }

        /** 네 → 틈 취소 API 호출 */
        binding.btnYes.setOnClickListener {
            viewModel.cancelTeumSchedule(scheduleId)
            Log.d("CANCEL_DEBUG", "취소 요청할 스케줄 ID: $scheduleId")

            binding.btnYes.isEnabled = false
        }

        viewModel.cancelScheduleSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { canceledId ->
                if (canceledId == scheduleId) {
                    dismissAllowingStateLoss()
                    Log.d("BOTTOM_SHEET", "취소 성공 이벤트로 닫힘 scheduleId=$canceledId")
                }
            }
        }

        /** 성공 시 → 바텀시트 닫기 */
        viewModel.successMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                dismissAllowingStateLoss()

                Log.d("BOTTOM_SHEET", "successMessage observe 됨")
            }
        }

        /** 실패 로그 */
        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Log.e("DELETE_CONFIRM", it.toString())
                binding.btnYes.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
