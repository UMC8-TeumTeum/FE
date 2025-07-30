package com.example.teumteum.ui.friend

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.TimeBlock
import com.example.teumteum.data.TimeType
import com.example.teumteum.databinding.FragmentFriendRoommateTimeBinding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.main.MainActivity

class FriendRoommateTimeFragment : Fragment() {

    private var _binding: FragmentFriendRoommateTimeBinding? = null
    private val binding get() = _binding!!

    private var isAM = true
    private lateinit var sleepIconBitmap: Bitmap

    // 예시 시간 데이터
    private val fullSchedule = listOf(
        TimeBlock(0, 360, TimeType.SLEEP),     // 00:00 ~ 06:00
        TimeBlock(360, 600, TimeType.TODO),    // 06:00 ~ 10:00
        TimeBlock(660, 720, TimeType.TODO),    // 11:00 ~ 12:00
        TimeBlock(780, 840, TimeType.TODO),    // 13:00 ~ 14:00
        TimeBlock(900, 1080, TimeType.EMPTY),  // 15:00 ~ 18:00
        TimeBlock(1140, 1320, TimeType.TODO),  // 19:00 ~ 22:00
        TimeBlock(1320, 1440, TimeType.SLEEP)  // 22:00 ~ 24:00
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

        // 바텀네비게이션 삭제
        (activity as? MainActivity)?.hideBottomBar()

        // 전달받은 날짜를 텍스트뷰에 반영
        val receivedDate = arguments?.getString("selected_date")
        if (!receivedDate.isNullOrEmpty()) {
            binding.date.text = receivedDate
        }


        // PieChart 기본 설정
        ChartUtils.setupPieChart(binding.clockChart)

        // 중앙 아이콘용 비트맵 로드
//        sleepIconBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)

        // 아이콘 렌더러 설정
//        binding.clockChart.renderer = IconPieChartRenderer(
//            binding.clockChart,
//            binding.clockChart.animator,
//            binding.clockChart.viewPortHandler,
//            sleepIconBitmap
//        )

        // 초기 시간 차트 세팅
        updateTimeChart()
        updateAMPMIndicator()

        // AM/PM 토글
        binding.amPmTv.setOnClickListener {
            isAM = !isAM
            updateTimeChart()
            updateAMPMIndicator()
        }

        // 뒤로가기
        // 버튼 클릭 시 FriendRoommateFriendFragment로 이동
        binding.btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateFriendFragment())
                .addToBackStack(null)
                .commit()
        }

        // 다음 버튼 클릭 시 FriendRoommateMatchingDetailFragment로 이동
        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendRoommateMatchingDetailFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateTimeChart() {
        val halfBlocks = ChartUtils.splitAndFillTimeBlocks(fullSchedule, isAM)
        ChartUtils.setTimePieChartData(requireContext(), binding.clockChart, halfBlocks)

        //  EMPTY 블럭 있는지 확인
        val hasEmptyTime = halfBlocks.any { it.type == TimeType.EMPTY }

        //  버튼 상태 업데이트
        updateNextButton(hasEmptyTime)
    }

    // 버튼 활성화 비활성화
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
            // 왼쪽 막대, 오른쪽 점
            updateIndicatorView(leftView, 28, 4, R.drawable.clock_indicator_bar_purple)
            updateIndicatorView(rightView, 4, 4, R.drawable.clock_indicator_dot)
            binding.amPmTv.text = "AM"
        } else {
            // 왼쪽 점, 오른쪽 막대
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
