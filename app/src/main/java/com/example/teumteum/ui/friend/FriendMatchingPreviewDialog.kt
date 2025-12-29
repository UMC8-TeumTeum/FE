package com.example.teumteum.ui.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.DialogFriendMatchingPreviewBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import kotlin.getValue

class FriendMatchingPreviewDialog : DialogFragment() {

    private var _binding: DialogFriendMatchingPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private var currentIndex = 0

    private var imageList = listOf(
        R.drawable.friend_teum_logo,
        R.drawable.teumi_teuma_eat,
        R.drawable.teumi_teuma_ball,
        R.drawable.teumi_teuma_down,
        R.drawable.teumi_teuma_juice
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFriendMatchingPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 1. 무조건 ViewModel 기반 전체 세팅
        updateSuggestion()

        // ✅ 2. 겹침 조회에서 넘어온 값이 있으면 덮어쓰기
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

        updateImage()

    binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % imageList.size
            updateImage()
        }

        binding.btnPrev.setOnClickListener {
            currentIndex = if (currentIndex == 0) imageList.size - 1 else currentIndex - 1
            updateImage()
        }

        binding.btnSend.setOnClickListener {
            val request = viewModel.buildTeumRequest()
            setLoading(true)
            if (request == null) return@setOnClickListener

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
        viewModel.teumRequestReceiverUserIds.value?.size?.let {
            if(it > 0){
                binding.tvName.text = viewModel.teumRequestMainTargetUserName.value + " 외 " + viewModel.teumRequestReceiverUserIds.value?.size.toString() + "명"
            } else{
                binding.tvName.text = viewModel.teumRequestMainTargetUserName.value
            }
        }
        Glide.with(binding.imgProfile)
            .load(viewModel.teumRequestMainTargetProfileImage.value)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.imgProfile)

        //  날짜 포맷 적용
        val rawDate = viewModel.teumRequestSelectedDate.value
        binding.tvDate.text = "${formatDate(rawDate)}     |"

        // 시간
        binding.tvTime.text =
            "${viewModel.teumRequestSelectedTime.value?.startTime} ~ ${viewModel.teumRequestSelectedTime.value?.endTime}"
    }

    private fun updateImage(){
        binding.imgTeum.setImageResource(imageList[currentIndex])
        viewModel.setTeumRequestGraphicId(currentIndex)
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val parsed = java.time.LocalDate.parse(date) // "2025-08-20"
            parsed.format(java.time.format.DateTimeFormatter.ofPattern("yy.MM.dd")) // "25.08.20"
        } catch (e: Exception) {
            date // 파싱 실패 시 원본 그대로
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLoading(loading: Boolean) {

        // 버튼 막기
        binding.btnSend.isEnabled = !loading
        binding.btnPrev.isEnabled = !loading
        binding.btnNext.isEnabled = !loading

        // 다이얼로그 취소/바깥터치 방지
        isCancelable = !loading
        dialog?.setCanceledOnTouchOutside(!loading)
    }

    companion object {
        const val TAG = "FriendMatchingPreviewDialog"  // public by default
    }
}
