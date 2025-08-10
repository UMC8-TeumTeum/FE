package com.example.teumteum.ui.friend

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.databinding.FragmentFriendRoommateTimeBinding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.friend.adapter.FriendProfileAdapter
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.main.data.TimeBlock
import com.example.teumteum.ui.main.data.TimeType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRoommateTimeFragment : Fragment() {

    private var _binding: FragmentFriendRoommateTimeBinding? = null
    private val binding get() = _binding!!

    private var isAM = true
    private lateinit var sleepIconBitmap: Bitmap

    // 예시 시간 데이터
    private val fullSchedule = listOf(
        TimeBlock(0, 360, TimeType.SLEEP),
        TimeBlock(360, 600, TimeType.TODO),
        TimeBlock(660, 720, TimeType.TODO),
        TimeBlock(780, 840, TimeType.TODO),
        TimeBlock(900, 1080, TimeType.EMPTY),
        TimeBlock(1140, 1320, TimeType.TODO),
        TimeBlock(1320, 1440, TimeType.SLEEP)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRoommateTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        val receivedDate = arguments?.getString("selected_date") ?: ""
        val myNickname = arguments?.getString("myNickname") ?: "나"
        val myProfileUrl = arguments?.getString("myProfileUrl") ?: ""
        val targetNickname = arguments?.getString("targetNickname") ?: "상대"
        val targetProfileUrl = arguments?.getString("targetProfileUrl") ?: ""

        // 날짜 표시
        binding.date.text = receivedDate

        // RecyclerView에 들어갈 리스트
        val profileList = mutableListOf<FriendProfileResult>()

        val addedFriends = arguments?.getParcelableArrayList<FriendProfileResult>("addedFriends") ?: emptyList()

        // 1. 나
        profileList.add(
            FriendProfileResult(
                userId = -1,
                name = myNickname,
                profileImageUrl = myProfileUrl,
                field = "",
                following = false,
                favorite = false
            )
        )

        // 2. 상대
        profileList.add(
            FriendProfileResult(
                userId = -2,
                name = targetNickname,
                profileImageUrl = targetProfileUrl,
                field = "",
                following = false,
                favorite = false
            )
        )

        // 선택한 친구들 추가
        profileList.addAll(addedFriends)

        // 어댑터 연결
        val adapter = FriendProfileAdapter(profileList) { profile ->
            // sendButton 클릭 시 동작
        }
        binding.friendProfileRv.layoutManager = LinearLayoutManager(requireContext())
        binding.friendProfileRv.adapter = adapter

        // PieChart 설정
        ChartUtils.setupPieChart(binding.clockChart)
        updateTimeChart()
        updateAMPMIndicator()

        // AM/PM 토글
        binding.amPmTv.setOnClickListener {
            isAM = !isAM
            updateTimeChart()
            updateAMPMIndicator()
        }

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 다음 버튼
        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateMatchingDetailFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateTimeChart() {
        val halfBlocks = ChartUtils.splitAndFillTimeBlocks(fullSchedule, isAM)
        val unifiedPurple = Color.parseColor("#847EFF")
        ChartUtils.setTimePieChartData(requireContext(), binding.clockChart, halfBlocks, unifiedPurple)
        val hasEmptyTime = halfBlocks.any { it.type == TimeType.EMPTY }
        updateNextButton(hasEmptyTime)
    }

    private fun updateNextButton(hasEmpty: Boolean) {
        if (!hasEmpty) {
            binding.nextBtn.isEnabled = false
            binding.nextBtn.setBackgroundColor(Color.parseColor("#F6F6F6"))
            binding.nextBtn.setTextColor(Color.parseColor("#0F0F0F"))
        } else {
            binding.nextBtn.isEnabled = true
            binding.nextBtn.setBackgroundColor(Color.parseColor("#0F0F0F"))
            binding.nextBtn.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun updateAMPMIndicator() {
        val leftView = binding.leftView
        val rightView = binding.rightView

        if (isAM) {
            updateIndicatorView(leftView, 28, 4, R.drawable.clock_indicator_bar_purple)
            updateIndicatorView(rightView, 4, 4, R.drawable.clock_indicator_dot)
            binding.amPmTv.text = "AM"
        } else {
            updateIndicatorView(leftView, 4, 4, R.drawable.clock_indicator_dot)
            updateIndicatorView(rightView, 28, 4, R.drawable.clock_indicator_bar_purple)
            binding.amPmTv.text = "PM"
        }

        leftView.requestLayout()
        rightView.requestLayout()
    }

    private fun updateIndicatorView(view: View, widthDp: Int, heightDp: Int, drawableRes: Int) {
        val layoutParams = view.layoutParams
        layoutParams.width = dpToPx(widthDp)
        layoutParams.height = dpToPx(heightDp)
        view.layoutParams = layoutParams
        view.background = ContextCompat.getDrawable(requireContext(), drawableRes)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
