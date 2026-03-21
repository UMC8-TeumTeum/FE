package com.umc.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumConflictItem
import com.umc.teumteum.databinding.BottomSheetFriendSendRequestBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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

        // Fragment에서 전달받은 실제 충돌 리스트
        val conflictList =
            arguments?.getParcelableArrayList<TeumConflictItem>(ARG_CONFLICT_LIST)
                ?: emptyList()

        // ViewPager 어댑터 세팅
        binding.viewPagerConflict.adapter =
            ConflictPagerAdapter(conflictList)

        // 인디케이터 초기화
        if (conflictList.isNotEmpty()) {
            setupIndicator(conflictList.size)
            updateIndicator(0)
        }

        // 페이지 변경 감지
        binding.viewPagerConflict.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicator(position)
                }
            }
        )

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnAccept.setOnClickListener {
            val selectedConflict = conflictList.getOrNull(0) ?: return@setOnClickListener

            val dialog = FriendMatchingPreviewDialog().apply {
                arguments = Bundle().apply {
                    putString("title", selectedConflict.title)
                    putString("description", selectedConflict.description)
                    putString("startTime", selectedConflict.startTime)
                    putString("endTime", selectedConflict.endTime)
                }
                Log.d("BOTTOM_SHEET_CLICK", selectedConflict.toString())

            }
            dialog.show(parentFragmentManager, FriendMatchingPreviewDialog.TAG)
            dismiss()
        }
    }

    // 인디케이터 설정
    private fun setupIndicator(count: Int) {
        binding.clockIndicatorLl.removeAllViews()

        repeat(count) {
            val indicator = View(requireContext())

            val params = LinearLayout.LayoutParams(
                dpToPx(4),
                dpToPx(4)
            ).apply {
                marginEnd = dpToPx(6)
            }

            indicator.layoutParams = params
            indicator.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            binding.clockIndicatorLl.addView(indicator)
        }
    }

    private fun updateIndicator(position: Int) {
        for (i in 0 until binding.clockIndicatorLl.childCount) {
            val view = binding.clockIndicatorLl.getChildAt(i)
            val params = view.layoutParams as LinearLayout.LayoutParams

            if (i == position) {
                // 선택된 카드 → 보라색 막대
                params.width = dpToPx(28)
                params.height = dpToPx(4)
                view.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.clock_indicator_bar_gray
                    )
            } else {
                // 나머지 → 회색 점
                params.width = dpToPx(4)
                params.height = dpToPx(4)
                view.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.clock_indicator_dot
                    )
            }

            view.layoutParams = params
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONFLICT_LIST = "arg_conflict_list"
        const val TAG = "FriendSendRequestBottomSheet"

        // 실제 충돌 리스트를 받아 생성
        fun newInstance(
            list: List<TeumConflictItem>
        ): FriendSendRequestBottomSheet {
            return FriendSendRequestBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(
                        ARG_CONFLICT_LIST,
                        ArrayList(list)
                    )
                }
            }
        }
    }
}
