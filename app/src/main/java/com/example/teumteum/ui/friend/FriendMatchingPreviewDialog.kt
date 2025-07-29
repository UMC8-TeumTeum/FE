package com.example.teumteum.ui.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.teumteum.R
import com.example.teumteum.databinding.DialogFriendMatchingPreviewBinding

class FriendMatchingPreviewDialog : DialogFragment() {

    private var _binding: DialogFriendMatchingPreviewBinding? = null
    private val binding get() = _binding!!

    // 제안 리스트 (임시 데이터)
    private val suggestions = listOf(
        Suggestion("학교 앞 샤브월데이 가자", "샤브월데이 할인한대! 가서 샤브샤브랑 초코 요구르트 과일쌈 해먹고 삼얼음 맥주 마시자"),
        Suggestion("카페 가서 공부하자", "조용한 카페에서 팀플 정리하고 과제하자 "),
        Suggestion("공원 산책하자", "날씨 좋으니까 가까운 공원 산책하고 사진 찍자 ")
    )
    private var currentIndex = 0

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

        // 다음 버튼
        binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % suggestions.size
            updateSuggestion()
        }

        // 이전 버튼
        binding.btnPrev.setOnClickListener {
            currentIndex = if (currentIndex == 0) suggestions.size - 1 else currentIndex - 1
            updateSuggestion()
        }

        // 전송 버튼
        binding.btnSend.setOnClickListener {
            dismiss()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendSendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateSuggestion() {
        binding.title.text = suggestions[currentIndex].title
        binding.detailSentence.text = suggestions[currentIndex].detail
        // 이미지도 바꾸려면 여기에 추가
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Suggestion(val title: String, val detail: String)
}
