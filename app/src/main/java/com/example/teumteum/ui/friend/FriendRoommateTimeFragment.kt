package com.example.teumteum.ui.friend

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.data.remote.friend.model.PossibleTimeRequest
import com.example.teumteum.databinding.FragmentFriendRoommateTimeBinding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.friend.adapter.FriendProfileAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.main.data.TimeBlock
import com.example.teumteum.ui.main.data.TimeType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class FriendRoommateTimeFragment : Fragment() {

    private var _binding: FragmentFriendRoommateTimeBinding? = null
    private val binding get() = _binding!!

    private var isAM = true
    private lateinit var sleepIconBitmap: Bitmap

    private val viewModel: FriendViewModel by activityViewModels()

    //차트에 들어갈 시간 데이터
    private var currentFullDayBlocks: List<TimeBlock> = emptyList()

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
        //기존 타겟 유저 아이디
        val targetUserId = arguments?.getInt("targetUserId") ?: -1

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
                userId = targetUserId,
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


        //시간표 조회를 위한 요청 생성
        val userIds: List<Int> = buildList {
            if (targetUserId > 0) add(targetUserId)
            addedFriends.asSequence()
                .map { it.userId }
                .filter { it > 0 }
                .forEach { add(it) }
        }.distinct()

        // 요청 객체 생성
        val request = PossibleTimeRequest(
            userIds = userIds,
            date = convertDateFormat(receivedDate)
        )

        Log.d("TIME_REQUEST", request.toString())
        // 호출
        viewModel.getPossibleTimeWithFriend(request)


        // PieChart 설정
        ChartUtils.setupPieChart(binding.clockChart)
        observeViewModel()
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
        // 데이터가 오기 전엔 예시(fullSchedule), 온 뒤엔 currentFullDayBlocks 사용
        val baseBlocks = currentFullDayBlocks
        val halfBlocks = ChartUtils.splitAndFillTimeBlocks(baseBlocks, isAM)

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

    private fun observeViewModel() {
        viewModel.possibleTimeList.observe(viewLifecycleOwner) { list ->
            Log.d("DEBUG", "observeViewModel triggered: ${list.size}개")

            val cards = list.filterNotNull()
            Log.d("DEBUG", "after filterNotNull: ${cards.size}개")

            if (cards.isEmpty()) {
                currentFullDayBlocks = listOf(TimeBlock(0, 1440, TimeType.TODO))
            } else {
                currentFullDayBlocks = ChartUtils.buildBlocksFromTimeCardItems(cards)
            }

            updateTimeChart()
        }
    }

    private fun convertDateFormat(dateStr: String): String {
        val formatterInput = DateTimeFormatter.ofPattern("yy.MM.dd(E)", Locale.KOREAN)
        val formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN)
        return LocalDate.parse(dateStr, formatterInput).format(formatterOutput)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
