package com.umc.teumteum.ui.friend

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentFriendRoommateDateBinding
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.umc.teumteum.utils.weekdayShortKorean
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class FriendRoommateDateFragment : Fragment() {

    private var _binding: FragmentFriendRoommateDateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    // 전달받은 타겟 유저
    private var targetUserId: Int = -1
    private var targetNickname: String? = null
    private var targetProfileUrl: String? = null

    private var selectedDate: LocalDate = LocalDate.now()
    private val today: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.now()

    private val headerFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")

    private lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            targetUserId = it.getInt("targetUserId")
            targetNickname = it.getString("targetNickname")
            targetProfileUrl = it.getString("targetProfileUrl")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRoommateDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomBar()

        calendarView = binding.calendarView
        calendarView.isVisible = true

        setupCalendar()
        setupHeader()
        setupWeekdayLabels()
        setupNavigationButtons()

        // 네비게이션 바
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, bottomInset)
            insets
        }

        // 2-1) 좌측 = 상대(타겟) 표시
        binding.profileNicknameTv1.text = targetNickname ?: "상대"
        Glide.with(binding.profileIv1)
            .load(targetProfileUrl)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.profileIv1)

        // 2-2) 우측 = 나 표시 (ViewModel에서 내 프로필 관찰)
        viewModel.fetchMyInfo() // 최초 1회 로딩
        viewModel.myNickname.observe(viewLifecycleOwner) { myNick ->
            binding.profileNicknameTv2.text = myNick ?: "나"
        }
        viewModel.myProfileUrl.observe(viewLifecycleOwner) { myUrl ->
            Glide.with(this)
                .load(myUrl)
                .placeholder(R.drawable.gray_teum)
                .error(R.drawable.gray_teum)
                .circleCrop()
                .into(binding.profileIv2)
        }

        // 초기 버튼 상태 비활성화
        binding.nextBtn.isEnabled = false
        binding.nextBtn.setBackgroundColor(Color.parseColor("#F6F6F6"))
        binding.nextBtn.setTextColor(Color.parseColor("#0F0F0F"))

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.nextBtn.setOnClickListener {
            val formattedDate = selectedDate?.let {
                val formatter = DateTimeFormatter.ofPattern("yy.MM.dd(E)", Locale.KOREAN)
                it.format(formatter)
            } ?: ""

            val bundle = Bundle().apply {
                putString("selected_date", formattedDate)

                // 상대방 정보
                putInt("targetUserId", targetUserId)
                putString("targetNickname", targetNickname)
                putString("targetProfileUrl", targetProfileUrl)

                //  내 정보
                putString("myNickname", viewModel.myNickname.value)
                putString("myProfileUrl", viewModel.myProfileUrl.value)
            }

            // 다음 버튼 클릭 시
            val fragment = FriendRoommateTimeFragment().apply {
                arguments = bundle
            }

            setViewModelData()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun setupHeader() {
        binding.selectedDateTv.text = visibleMonth.format(headerFormatter)
    }

    private fun setupCalendar() {
        // 해당 라이브러리는 캘린더 범위를 무제한으로 설정할 수 없어 일단은 +-50년으로 설정...
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(50) // 50년 전
        val endMonth = currentMonth.plusYears(50)  // 50년 후
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        calendarView.monthScrollListener = { month ->
            visibleMonth = month.yearMonth
            setupHeader()
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val tv = container.textView
                tv.text = day.date.dayOfMonth.toString()

                // 기본 스타일 초기화
                tv.typeface = Typeface.DEFAULT
                tv.background = null

                // 이번 달 셀만 활성화, out-date는 비활성화/회색
                val isThisMonth = day.position == DayPosition.MonthDate
                val isBeforeToday = day.date.isBefore(today)

                // 회색 텍스트 적용
                tv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        when {
                            !isThisMonth -> R.color.teumteum_deactive // 다른 달은 비활성화 색상
                            isBeforeToday -> R.color.teumteum_deactive // 지난 날짜도 비활성화
                            else -> R.color.text_primary
                        }
                    )
                )

                // 오늘 표시
                if (day.date == today) {
                    tv.background = circleFill(
                        fillColor = ContextCompat.getColor(requireContext(), R.color.teumteum_gray)
                    )
                }

                // 날짜 선택
                if (day.date == selectedDate && !isBeforeToday && isThisMonth) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    tv.background = circleFill(ContextCompat.getColor(requireContext(), R.color.main_1))
                }

                // 클릭으로 선택 처리
                container.view.setOnClickListener {
                    if (!isThisMonth || isBeforeToday) return@setOnClickListener  // 전환/선택 방지

                    val old = selectedDate
                    selectedDate = day.date

                    // 월 갱신
                    calendarView.notifyDateChanged(old)
                    calendarView.notifyDateChanged(selectedDate)

                    updateHeader()
                    updateNextButtonState(selectedDate)
                }
            }
        }
    }

    private fun updateHeader() {
        val ym = YearMonth.from(selectedDate)
        binding.selectedDateTv.text = ym.format(headerFormatter)
    }

    // 요일 텍스트 설정 (일~토)
    private fun setupWeekdayLabels() {
        val container = binding.calendarWeekdaysRow
        container.removeAllViews()

        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val days = (0..6).map { firstDayOfWeek.plus(it.toLong()) }
        days.forEach { dow ->
            val tv = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                text = weekdayShortKorean(dow)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }
            container.addView(tv)
        }
    }

    private fun setupNavigationButtons() {
        binding.calendarPreviousDateIv.setOnClickListener {
            calendarView.smoothScrollToMonth(visibleMonth.minusMonths(1))
        }

        binding.calendarNextDateIv.setOnClickListener {
            calendarView.smoothScrollToMonth(visibleMonth.plusMonths(1))
        }
    }

    private fun updateNextButtonState(date: LocalDate?) {
        if (date != null && !date.isBefore(today)) {
            binding.nextBtn.isEnabled = true
            binding.nextBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            binding.nextBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            binding.nextBtn.isEnabled = false
            binding.nextBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teumteum_bg))
            binding.nextBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        }
    }

    private fun setViewModelData() {
        viewModel.setTeumRequestMainTargetUserId(targetUserId)
        viewModel.setTeumRequestMainTargetProfileImage(targetProfileUrl!!)
        viewModel.setTeumRequestSelectedDate(selectedDate.toString())
        viewModel.setTeumRequestMainTargetUserName(targetNickname!!)
    }

    // 채운 동그라미 배경
    private fun circleFill(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
        }
    }

    // DayView의 뷰 홀더
    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendar_day_tv)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}