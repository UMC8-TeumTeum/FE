package com.example.teumteum.ui.friend

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.FriendProfileResult
import com.example.teumteum.data.remote.friend.model.PossibleTimeRequest
import com.example.teumteum.databinding.FragmentFriendRoommateTimeBinding
import com.example.teumteum.databinding.ItemClockMiniPageBinding
import com.example.teumteum.ui.clock.ChartUtils
import com.example.teumteum.ui.clock.ClockHalf
import com.example.teumteum.ui.clock.ClockVPAdapter
import com.example.teumteum.ui.clock.IconPieChartRenderer
import com.example.teumteum.ui.friend.adapter.FriendProfileAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.main.data.TimeBlock
import com.example.teumteum.ui.main.data.TimeType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.orEmpty
import kotlin.getValue

@AndroidEntryPoint
class FriendRoommateTimeFragment : Fragment() {

    private var _binding: FragmentFriendRoommateTimeBinding? = null
    private val binding get() = _binding!!

    private var isAM = true

    private val viewModel: FriendViewModel by activityViewModels()

    //차트에 들어갈 시간 데이터
    private var currentFullDayBlocks: List<TimeBlock> = emptyList()

    private lateinit var clockAdapter: ClockVPAdapter<ItemClockMiniPageBinding>

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

        // 네비게이션 바
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, bottomInset)
            insets
        }


        val receivedDate = arguments?.getString("selected_date") ?: ""
        val myNickname = arguments?.getString("myNickname") ?: "나"
        val myProfileUrl = arguments?.getString("myProfileUrl") ?: ""
        val targetNickname = arguments?.getString("targetNickname") ?: "상대"
        val targetProfileUrl = arguments?.getString("targetProfileUrl") ?: ""
        //기존 타겟 유저 아이디
        val targetUserId = arguments?.getInt("targetUserId") ?: -1

        // 날짜 표시
        binding.date.text = receivedDate

        // 화면 진입 시 매번 상태 초기화 (토글/선택 모두 리셋)
        viewModel.clearExclusions()
        viewModel.clearSelectedFriends()

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

        // [ADDED] ViewModel의 "선택 친구" 목록에 대상들 등록 (내 자신 -1은 제외)
        if (targetUserId > 0) {
            viewModel.addSelectedFriend(
                FriendProfileResult(
                    userId = targetUserId,
                    name = targetNickname,
                    profileImageUrl = targetProfileUrl,
                    field = "",
                    following = false,
                    favorite = false
                )
            )
        }
        addedFriends.forEach { friend ->
            if (friend.userId > 0) viewModel.addSelectedFriend(friend)
        }

        // 어댑터 콜백에서 제외 토글을 호출하도록 변경
        val adapter = FriendProfileAdapter(profileList) { userId ->
            viewModel.toggleExclude(userId)
        }
        binding.friendProfileRv.layoutManager = LinearLayoutManager(requireContext())
        binding.friendProfileRv.adapter = adapter

        // 제외 집합 변경 시 아이콘 싱크
        viewModel.excludedUserIds.observe(viewLifecycleOwner) { set ->
            adapter.setExcludedIds(set ?: emptySet())
        }

        // 화면 진입 시 고정 날짜 세팅
        viewModel.setFixedDate(convertDateFormat(receivedDate))

        // PieChart 설정
        setupClockPager()
        updateIndicator(isAM)

        observeViewModel()

        // AM/PM 토글
        binding.amPmTv.setOnClickListener {
            val amPos = clockAdapter.positionOf(ClockHalf.AM)
            val pmPos = clockAdapter.positionOf(ClockHalf.PM)
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 다음 버튼
        binding.nextBtn.setOnClickListener {
            val receivedDate = arguments?.getString("selected_date") ?: ""
            val detail = FriendRoommateMatchingDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("selected_date", receivedDate)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, detail)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupClockPager() {
        clockAdapter = ClockVPAdapter(
            inflate = ItemClockMiniPageBinding::inflate,
            chartOf = { it.clockChart },
            onBindPage = { chart, half ->
                ChartUtils.setupPieChart(chart)
                val sleepBitmap = ChartUtils.getBitmapFromVector(requireContext(), R.drawable.ic_sleep_sv)
                chart.renderer = IconPieChartRenderer(chart, chart.animator, chart.viewPortHandler, sleepBitmap)

                // AM/PM 데이터 바인딩
                val blocks = currentFullDayBlocks
                val halfBlocks = ChartUtils.splitAndFillTimeBlocks(blocks, half == ClockHalf.AM)
                ChartUtils.setTimePieChartData(requireContext(), chart, halfBlocks)

            }
        )

        binding.clockPager.adapter = clockAdapter
        binding.clockPager.offscreenPageLimit = 1

        val amPos = clockAdapter.positionOf(ClockHalf.AM) // 0
        val pmPos = clockAdapter.positionOf(ClockHalf.PM) // 1

        binding.clockPager.setCurrentItem(amPos, false)

        binding.clockPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicator(position == amPos)
            }
        })

        binding.amPmTv.setOnClickListener {
            val next = if (binding.clockPager.currentItem == amPos) pmPos else amPos
            binding.clockPager.setCurrentItem(next, true)
        }

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

    private fun updateIndicator(isAM: Boolean) {
        val leftView = binding.leftView
        val rightView = binding.rightView

        if (isAM) {
            //왼쪽이 막대, 오른쪽이 점
            leftView.layoutParams.width = dpToPx(28)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar_purple)

            rightView.layoutParams.width = dpToPx(4)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            binding.amPmTv.text="AM"
        } else {
            //왼쪽이 점, 오른쪽이 막대
            leftView.layoutParams.width = dpToPx(4)
            leftView.layoutParams.height = dpToPx(4)
            leftView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            rightView.layoutParams.width = dpToPx(28)
            rightView.layoutParams.height = dpToPx(4)
            rightView.background = ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_bar_purple)

            binding.amPmTv.text="PM"
        }

        leftView.requestLayout()
        rightView.requestLayout()
    }


    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun observeViewModel() {
        viewModel.possibleTimeList.observe(viewLifecycleOwner) { list ->
            Log.d("DEBUG", "observeViewModel triggered: ${list.size}개")

            val cards = list.filterNotNull()
            Log.d("DEBUG", "after filterNotNull: ${cards.size}개")

            // 현재 날짜와 선택된 날짜 비교
            val selectedDate = convertDateFormat(arguments?.getString("selected_date") ?: "")
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val filteredCards = if (selectedDate == today) {
                // 오늘인 경우 현재 시각 이후의 시간만 필터링
                val currentMinutes = LocalDateTime.now().let { now ->
                    now.hour * 60 + now.minute
                }

                cards.mapNotNull { card ->
                    val startMinutes = timeToMinutes(card.startTime)
                    val endMinutes = timeToMinutes(card.endTime)

                    when {
                        // 전체 시간이 현재 시각 이전인 경우 제외
                        endMinutes <= currentMinutes -> null
                        // 시작 시간이 현재 시각 이전인 경우 현재 시각부터 시작하도록 조정
                        startMinutes < currentMinutes -> {
                            val adjustedStartTime = minutesToTime(currentMinutes)
                            card.copy(startTime = adjustedStartTime)
                        }
                        // 전체 시간이 현재 시각 이후인 경우 그대로 유지
                        else -> card
                    }
                }
            } else {
                // 오늘이 아닌 경우 모든 시간 표시
                cards
            }

            // 필터링 결과에 따른 텍스트 및 UI 업데이트
            if (filteredCards.isEmpty()) {
                // 가용 시간이 없는 경우의 텍스트 분기
                binding.possibleTime.text = if (selectedDate == today) {
                    if (cards.isEmpty()) {
                        "이때는 가능한 빈틈이 없어요"  // 원래 서버에서 빈 시간이 없는 경우
                    } else {
                        "현재 시각 이후 가능한 빈틈이 없어요"  // 현재 시각 필터링으로 인해 없어진 경우
                    }
                } else {
                    "이때는 가능한 빈틈이 없어요"  // 다른 날짜
                }
                currentFullDayBlocks = listOf(TimeBlock(0, 1440, TimeType.TODO))
                updateNextButton(false)
            } else {
                // 가용 시간이 있는 경우의 텍스트 분기
                binding.possibleTime.text = if (selectedDate == today && cards.size > filteredCards.size) {
                    "현재 시각 이후 가능한 빈틈이 있어요"  // 일부 시간이 필터링된 경우
                } else {
                    "가능한 빈틈이 있어요"  // 일반적인 경우
                }
                currentFullDayBlocks = ChartUtils.buildBlocksFromTimeCardItems(filteredCards)
                updateNextButton(true)
            }
            clockAdapter.refreshAll()
        }
    }

    // 시간 문자열을 분(minutes)으로 변환하는 헬퍼 함수
    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    // 분(minutes)을 시간 문자열로 변환하는 헬퍼 함수
    private fun minutesToTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
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
