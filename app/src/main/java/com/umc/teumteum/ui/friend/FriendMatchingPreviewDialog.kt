package com.umc.teumteum.ui.friend

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.databinding.DialogFriendMatchingPreviewBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import kotlin.math.abs
import androidx.core.graphics.drawable.toDrawable

class FriendMatchingPreviewDialog : DialogFragment() {

    private var _binding: DialogFriendMatchingPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private var currentIndex = 0

    private val imageList = listOf(
        R.drawable.friend_teum_logo,
        R.drawable.teumi_teuma_eat,
        R.drawable.teumi_teuma_ball,
        R.drawable.teumi_teuma_down,
        R.drawable.teumi_teuma_juice
    )

    private var downX = 0f
    private val swipeThreshold = 100f // 드래그 인식 최소 거리

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFriendMatchingPreviewBinding.inflate(inflater, container, false)

        binding.imgTeum.setImageResource(imageList[currentIndex])

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val params = window.attributes
            params.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = (resources.displayMetrics.heightPixels * 0.18).toInt()
            params.dimAmount = 0.5f
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) viewModel 기반 텍스트/프로필 세팅
        updateSuggestion()

        // 2) arguments로 넘어온 값 덮어쓰기
        arguments?.let { args ->
            binding.title.text = args.getString("title") ?: binding.title.text
            binding.detailSentence.text =
                args.getString("description") ?: binding.detailSentence.text

            val start = args.getString("startTime")
            val end = args.getString("endTime")
            if (!start.isNullOrBlank() && !end.isNullOrBlank()) {
                binding.tvTime.text = "$start ~ $end"
            }
        }

        // 3) 초기 이미지 반영
        updateImage()

        binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % imageList.size
            updateImage()
        }

        binding.btnPrev.setOnClickListener {
            currentIndex =
                if (currentIndex == 0) imageList.size - 1
                else currentIndex - 1
            updateImage()
        }

        binding.imgTeum.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    v.isPressed = true
                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.isPressed = false

                    val diffX = event.x - downX
                    if (abs(diffX) > swipeThreshold) {
                        currentIndex = if (diffX < 0) {
                            // 왼쪽 드래그 → 다음
                            (currentIndex + 1) % imageList.size
                        } else {
                            // 오른쪽 드래그 → 이전
                            if (currentIndex == 0) imageList.size - 1
                            else currentIndex - 1
                        }
                        updateImage()
                    }
                    v.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    true
                }
                else -> false
            }
        }

        binding.btnSend.setOnClickListener {
            val request = viewModel.buildTeumRequest() ?: return@setOnClickListener
            setLoading(true)

            viewModel.sendTeumRequest(
                request,
                onSuccess = {
                    viewModel.setTeumRequestReceiverUserIds(emptyList())
                    if (isAdded &&
                        this@FriendMatchingPreviewDialog.view != null &&
                        lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                    ) {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, FriendSendFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    dismissAllowingStateLoss()
                },
                onError = { msg ->
                    setLoading(false)
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateSuggestion() {
        binding.title.text = viewModel.teumRequestTitle.value
        binding.detailSentence.text = viewModel.teumRequestDescription.value

        viewModel.teumRequestReceiverUserIds.value?.size?.let { count ->
            binding.tvName.text =
                if (count > 0) {
                    "${viewModel.teumRequestMainTargetUserName.value} 외 ${count}명"
                } else {
                    viewModel.teumRequestMainTargetUserName.value
                }
        }

        Glide.with(binding.imgProfile)
            .load(viewModel.teumRequestMainTargetProfileImage.value)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.imgProfile)

        val rawDate = viewModel.teumRequestSelectedDate.value
        binding.tvDate.text = "${formatDate(rawDate)}     |"

        binding.tvTime.text =
            "${viewModel.teumRequestSelectedTime.value?.startTime} ~ ${viewModel.teumRequestSelectedTime.value?.endTime}"
    }

    private fun updateImage() {
        binding.imgTeum.setImageResource(imageList[currentIndex])
        viewModel.setTeumRequestGraphicId(currentIndex)
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val parsed = java.time.LocalDate.parse(date)
            parsed.format(java.time.format.DateTimeFormatter.ofPattern("yy.MM.dd"))
        } catch (e: Exception) {
            date
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSend.isEnabled = !loading
        binding.btnPrev.isEnabled = !loading
        binding.btnNext.isEnabled = !loading
        isCancelable = !loading
        dialog?.setCanceledOnTouchOutside(!loading)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FriendMatchingPreviewDialog"
    }
}
