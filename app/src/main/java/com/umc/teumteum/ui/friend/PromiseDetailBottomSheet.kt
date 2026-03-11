package com.umc.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumScheduleDetailResult
import com.umc.teumteum.databinding.Friend03PromiseDetailBottomSheetBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

class PromiseDetailBottomSheet(
    private val detail: TeumScheduleDetailResult,
    private val scheduleId: Int,
    private val isPast: Boolean
) : BottomSheetDialogFragment() {

    private var _binding: Friend03PromiseDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    // 바텀 시트 배경
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.let { bottomSheet ->
            bottomSheet.layoutParams.height =
                (420 * resources.displayMetrics.density).toInt()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Friend03PromiseDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("PromiseDetailBottomSheet", "바텀시트 표시됨 - title: ${detail.title}")

        // UI 설정
        binding.tvTitle.text = detail.title
        binding.tvDate1.text = formatDate(detail.date)
        binding.tvTime1.text = formatTime(detail.startTime)
        binding.tvDate2.text = formatDate(detail.date)
        binding.tvTime2.text = formatTime(detail.endTime)

        // 참여자 프로필 동적 추가
        showParticipantProfiles(detail)

        val isPastLocal = run {
            val date = LocalDate.parse(detail.date)
            val endTime = LocalTime.parse(
                if (detail.endTime == "24:00") "00:00" else detail.endTime
            )
            date.atTime(endTime).isBefore(java.time.LocalDateTime.now())
        }

        binding.btnCancelPromise.visibility =
            if (isPastLocal) View.GONE else View.VISIBLE

        // 클릭 시 취소 요청만 호출
        binding.btnCancelPromise.setOnClickListener {
            FriendTeumDeleteBottomSheet
                .newInstance(scheduleId)
                .show(parentFragmentManager, "FriendTeumDeleteBottomSheet")
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Log.d("PROMISE_DETAIL_BOTTOM_SHEET", message)
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { err ->
                Log.e("PROMISE_DETAIL_BOTTOM_SHEET", err)
            }
        }
    }

    private fun showParticipantProfiles(detail: TeumScheduleDetailResult) {
        val container = binding.profileContainer
        container.removeAllViews()

        val sizeDp = 40
        val marginDp = 8
        val sizePx = (sizeDp * resources.displayMetrics.density).toInt()
        val marginPx = (marginDp * resources.displayMetrics.density).toInt()

        Log.d("PromiseDetailBottomSheet", "참가자 수: ${detail.participants.size}")

        detail.participants.forEach { participant ->
            val imageView = ImageView(requireContext()).apply {
                layoutParams = MarginLayoutParams(sizePx, sizePx).apply {
                    rightMargin = marginPx
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = ContextCompat.getDrawable(context, R.drawable.freind01_circle_profile)
                clipToOutline = true
            }

            Glide.with(this)
                .load(participant.profileImageUrl)
                .placeholder(R.drawable.gray_teum)
                .into(imageView)

            container.addView(imageView)
        }
    }

    private fun formatDate(date: String): String {
        return try {
            val parsed = LocalDate.parse(date)
            val dayOfWeek = parsed.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            "${parsed.monthValue}월 ${parsed.dayOfMonth}일 ($dayOfWeek)"
        } catch (e: Exception) {
            date
        }
    }

    private fun formatTime(time: String): String {
        return try {
            // 24:00 → 00:00으로 변환 후 파싱
            val normalizedTime = if (time == "24:00") "00:00" else time
            val parsed = LocalTime.parse(normalizedTime)

            val hour = if (parsed.hour % 12 == 0) 12 else parsed.hour % 12
            val ampm = if (parsed.hour < 12) "오전" else "오후"
            "$ampm $hour:${parsed.minute.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            time
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
