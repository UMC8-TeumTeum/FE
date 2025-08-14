package com.example.teumteum.ui.friend

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateDateBinding
import com.example.teumteum.ui.calendar.FriendMonthlyCalendarFragment
import com.example.teumteum.ui.calendar.IDateClickListener
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.getSavedDateOrToday
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
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

    private var selectedDate: LocalDate? = null
    private val today: LocalDate = LocalDate.now()
    private val baseDate: LocalDate by lazy { getSavedDateOrToday(requireContext()) }
    private var currentMonthOffset = 0

    private val onClickListener = object : IDateClickListener {
        override fun onClickDate(date: LocalDate) {
            selectedDate = date
            updateCalendarFragment()

            // 선택된 날짜가 오늘 이후일 때 버튼 활성화
            if (date.isAfter(today)) {
                binding.nextBtn.isEnabled = true
                binding.nextBtn.setBackgroundColor(Color.parseColor("#000000"))  // 검정색
                binding.nextBtn.setTextColor(Color.parseColor("#FFFFFF"))        // 흰 글씨
            } else {
                binding.nextBtn.isEnabled = false
                binding.nextBtn.setBackgroundColor(Color.parseColor("#F6F6F6"))  // 비활성 색
                binding.nextBtn.setTextColor(Color.parseColor("#0F0F0F"))        // 회색 글씨
            }
        }
    }

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

        setupNavigationButtons()
        updateCalendarFragment()

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

        // 뒤로가기 버튼
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

            val fragment = FriendRoommateFriendFragment().apply {
                arguments = bundle
            }

            setViewModelData()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }



    }

    private fun setupNavigationButtons() {
        binding.homeCalendarPreviousDateIv.setOnClickListener {
            currentMonthOffset--
            updateCalendarFragment()
        }

        binding.homeCalendarNextDateIv.setOnClickListener {
            currentMonthOffset++
            updateCalendarFragment()
        }
    }

    private fun updateCalendarFragment() {
        val displayDate = baseDate.plusMonths(currentMonthOffset.toLong())
        binding.homeSelectedDateTv.text = "${displayDate.year}년 ${displayDate.monthValue}월"

        val calendarFragment = FriendMonthlyCalendarFragment.newInstance(
            position = Int.MAX_VALUE / 2 + currentMonthOffset,
            onClickListener = onClickListener,
            showDot = true
        ).apply {
            arguments = Bundle().apply {
                putSerializable("displayDate", displayDate)
                putSerializable("selectedDate", selectedDate)
                putSerializable("today", today)
            }
        }

        childFragmentManager.beginTransaction()
            .replace(binding.calendarContainer.id, calendarFragment)
            .commit()
    }

    private fun setViewModelData(){
        viewModel.setTeumRequestMainTargetUserId(targetUserId)
        viewModel.setTeumRequestMainTargetProfileImage(targetProfileUrl!!)
        viewModel.setTeumRequestSelectedDate(selectedDate.toString())
        viewModel.setTeumRequestMainTargetUserName(targetNickname!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}