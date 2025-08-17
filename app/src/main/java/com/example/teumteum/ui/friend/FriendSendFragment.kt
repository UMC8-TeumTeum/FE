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
import androidx.fragment.app.FragmentManager
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.enums.ScheduleType
import com.example.teumteum.databinding.FragmentFriendSendBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendSendFragment : Fragment() {

    private var _binding: FragmentFriendSendBinding? = null
    private val binding get() = _binding!!

    private var scheduleType: String? = null

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

        scheduleType = arguments?.getString("schedule_type")

        // 버튼 클릭 시 타입 처리 및 FriendFragment로 이동
        binding.btnGoHome.setOnClickListener {
            val fragment = FriendFragment().apply {
                arguments = Bundle().apply {
                    putString("schedule_type", ScheduleType.TEUM.toString())
                }
            }
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
