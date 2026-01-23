package com.umc.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentFriendRejectSendBinding

class FriendRejectSendFragment : Fragment() {

    private var _binding: FragmentFriendRejectSendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRejectSendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "전송" 글자만 색상 변경
        val text = "친구에게 전송했어요!"
        val spannable = SpannableString(text)
        val start = text.indexOf("전송")
        val end = start + 2
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#7770FE")),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textTitle.text = spannable

        binding.btnGoHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment()) // 메인에 FriendFragment 로드
                .addToBackStack(null) // 뒤로 가기 가능하게 할지 여부
                .commit()
        }

        // 뒤로가기 버튼 동작
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
