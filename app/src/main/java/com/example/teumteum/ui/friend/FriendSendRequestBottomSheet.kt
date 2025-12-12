package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.teumteum.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.teumteum.databinding.BottomSheetFriendSendRequestBinding
import com.example.teumteum.ui.friend.data.ConflictItem
import com.google.android.material.bottomsheet.BottomSheetDialog


class FriendSendRequestBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendSendRequestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }
        return dialog
    }


    // '수락' 버튼 클릭 리스너 인터페이스
    interface OnRequestSendListener {
        fun onAcceptClicked()
    }
    private var listener: OnRequestSendListener? = null
    fun setOnRequestSendListener(listener: OnRequestSendListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriendSendRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: 실제 데이터로 교체해야 하는 예시 데이터
        val conflictList = listOf(
            ConflictItem(
                "종강 기념 한강 피크닉",
                "종강 기념 한강 피크닉 가자!...",
                "문혜원",
                "15:20 ~ 16:10"
            ),
            ConflictItem(
                "다른 약속",
                "다른 약속 상세 내용",
                "김철수",
                "15:30 ~ 16:00"
            )
        )

        // 1. (분리된) 어댑터 생성 및 설정
        val pagerAdapter = ConflictPagerAdapter(conflictList)
        binding.viewPagerConflict.adapter = pagerAdapter

        // 2. DotsIndicator와 ViewPager2 연결
        binding.dotsIndicator.setViewPager2(binding.viewPagerConflict)

        // 3. 버튼 클릭 리스너
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnAccept.setOnClickListener {
            listener?.onAcceptClicked()
            Toast.makeText(requireContext(), "수락되었습니다.", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FriendSendRequestBottomSheet"
        fun newInstance(): FriendSendRequestBottomSheet {
            // TODO: newInstance를 통해 데이터를 받아야 한다면 인자 추가
            return FriendSendRequestBottomSheet()
        }
    }
}