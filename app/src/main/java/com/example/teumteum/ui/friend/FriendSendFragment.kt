package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendSendBinding

class FriendSendFragment : Fragment() {

    private var _binding: FragmentFriendSendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendSendBinding.inflate(inflater, container, false)
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

        // 버튼 클릭 시 FriendFragment로 이동
        binding.btnGoHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
