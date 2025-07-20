package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.teumteum.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.teumteum.databinding.Friend03PromiseDetailBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class PromiseDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: Friend03PromiseDetailBottomSheetBinding? = null
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

    companion object {
        const val TAG       = "PromiseDetailBottomSheet"
        private const val ARG_YEAR  = "year"
        private const val ARG_MONTH = "month"
        private const val ARG_DAY   = "day"

        fun newInstance(year: Int, month: Int, day: Int): PromiseDetailBottomSheet {
            val bs = PromiseDetailBottomSheet()
            bs.arguments = Bundle().apply {
                putInt(ARG_YEAR,  year)
                putInt(ARG_MONTH, month)
                putInt(ARG_DAY,   day)
            }
            return bs
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = Friend03PromiseDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버튼 클릭 처리
        binding.btnCancelPromise.setOnClickListener {
            Toast.makeText(requireContext(), "약속이 취소되었습니다", Toast.LENGTH_SHORT).show()
            dismiss()  // 다이얼로그 닫기
        }

        // 전달된 인자 꺼내기
        val year  = requireArguments().getInt(ARG_YEAR)
        val month = requireArguments().getInt(ARG_MONTH)
        val day   = requireArguments().getInt(ARG_DAY)

        // 요일 계산
        val cal = Calendar.getInstance().apply { set(year, month, day) }
        val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY    -> "일"
            Calendar.MONDAY    -> "월"
            Calendar.TUESDAY   -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY  -> "목"
            Calendar.FRIDAY    -> "금"
            Calendar.SATURDAY  -> "토"
            else               -> ""
        }

        // 날짜 텍스트 생성
        val dateText = "${month + 1}월 ${day}일 ($dow)"
        binding.tvDate1.text = dateText
        binding.tvDate2.text = dateText

        // ← 여기서 “과거인지” 체크
        val today = Calendar.getInstance().apply {
            // 0시로 초기화해서 “오늘” 기준만 비교하고 싶다면
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val selectedDate = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (selectedDate.before(today)) {
            // 과거 날짜면 버튼 숨기기
            binding.btnCancelPromise.visibility = View.INVISIBLE
        } else {
            // 오늘 이후(오늘 포함)면 버튼 보이기
            binding.btnCancelPromise.visibility = View.VISIBLE
        }


        // TODO: 필요하다면 시작/종료 시간, 설명도 Bundle 로 받아와 세팅
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
