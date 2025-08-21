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
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.DialogFriendMatchingPreviewBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import kotlin.getValue

class FriendMatchingPreviewDialog : DialogFragment() {

    private var _binding: DialogFriendMatchingPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    // 제안 리스트 (임시 데이터)
//    private val suggestions = listOf(
//        Suggestion("학교 앞 샤브월데이 가자", "샤브월데이 할인한대! 가서 샤브샤브랑 초코 요구르트 과일쌈 해먹고 삼얼음 맥주 마시자"),
//        Suggestion("카페 가서 공부하자", "조용한 카페에서 팀플 정리하고 과제하자 "),
//        Suggestion("공원 산책하자", "날씨 좋으니까 가까운 공원 산책하고 사진 찍자 ")
//    )
    private var currentIndex = 0

    private var imageList = listOf(
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

        // 초기 텍스트 설정
        updateSuggestion()
        updateImage()

        // 다음 버튼
        binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % imageList.size
            updateImage()
        }

        // 이전 버튼
        binding.btnPrev.setOnClickListener {
            currentIndex = if (currentIndex == 0) imageList.size - 1 else currentIndex - 1
            updateImage()
        }

        // 전송 버튼
        binding.btnSend.setOnClickListener {

            val request = viewModel.buildTeumRequest()
            Log.d("SEND_TEUM_REQUEST", request.toString())
            if (request == null) {
                return@setOnClickListener
            }

            // 전송
            viewModel.sendTeumRequest(
                request,
                onSuccess = {
                    viewModel.setTeumRequestReceiverUserIds(emptyList())

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, FriendSendFragment())
                        .addToBackStack(null)
                        .commit()

                    dismiss()
                },
                onError = { msg ->
//                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d("FRIEND_MATCHING_PREVIEW_DIALOG", msg.toString())
                })
        }
    }

    private fun updateSuggestion() {
//        binding.title.text = suggestions[currentIndex].title
//        binding.detailSentence.text = suggestions[currentIndex].detail
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
        binding.tvDate.text = viewModel.teumRequestSelectedDate.value
        binding.tvTime.text = viewModel.teumRequestSelectedTime.value?.startTime.toString() + " ~ " + viewModel.teumRequestSelectedTime.value?.endTime.toString()
    }

    private fun updateImage(){
        binding.imgTeum.setImageResource(imageList[currentIndex])
        viewModel.setTeumRequestGraphicId(currentIndex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Suggestion(val title: String, val detail: String)
}
