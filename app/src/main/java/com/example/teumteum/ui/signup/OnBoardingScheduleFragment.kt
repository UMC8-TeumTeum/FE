package com.example.teumteum.ui.signup

import BottomSheetScheduleFragment
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentOnBoardingScheduleBinding
import kotlin.collections.toList
import com.example.teumteum.data.Schedule
import com.example.teumteum.data.remote.onboarding.OnBoardingService
import com.example.teumteum.data.remote.onboarding.dto.ScheduleRequest
import com.example.teumteum.data.remote.onboarding.dto.Week
import com.example.teumteum.ui.signup.view.ScheduleView
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import kotlin.collections.forEachIndexed

@AndroidEntryPoint
class OnBoardingScheduleFragment : Fragment(), ScheduleView{

    private lateinit var binding: FragmentOnBoardingScheduleBinding
    private val scheduleAdapter by lazy { ScheduleAdapter() }

    private lateinit var dayTextViews: List<TextView>

    private var selectedDayIndex = 0

    private val scheduleMap = mutableMapOf<Int, MutableList<Schedule>>()

    private var sleepStart: LocalTime? = null
    private var sleepEnd: LocalTime? = null

    @Inject
    lateinit var onBoardingService: OnBoardingService

    override fun onScheduleSuccess(code: String) {
        val msg = "반복 일정 등록 성공 (code: $code)"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.d("SCHEDULE_FRAGMENT", msg)

        parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingRemindFragment())
                .addToBackStack(null)
                .commit()
    }

    override fun onScheduleFailure(code: String, message: String?) {
        val msg = "반복 일정 등록 실패 (code: $code, message: ${message ?: "없음"})"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        Log.e("SCHEDULE_FRAGMENT", msg)

        //온보딩 단계가 아닐 경우 - 이후 테스트를 위해 화면 이동하도록 구현
        if (message?.contains("ONBOARDING4001") == true) {
            Toast.makeText(requireContext(), "온보딩 단계가 아닙니다.", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingRemindFragment())
                .addToBackStack(null)
                .commit()
        }

        //그 외 잘못된 입력
        if (message?.contains("ONBOARDING4005") == true ||
            message?.contains("ONBOARDING4006") == true ||
            message?.contains("ONBOARDING4007") == true) {
            Log.e("SCHEDULE_FRAGMENT", msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val start = it.getString("sleepStart")
            val end = it.getString("sleepEnd")

            sleepStart = start?.let { LocalTime.parse(it) }
            sleepEnd = end?.let { LocalTime.parse(it) }
        }

        if (savedInstanceState == null) {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            selectedDayIndex = when (dayOfWeek) {
                Calendar.SUNDAY -> 0
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 0
            }
        } else {
            selectedDayIndex = savedInstanceState.getInt("selectedDayIndex", 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedDayIndex", selectedDayIndex)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnBoardingScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(80)

        binding.nextBtn.setOnClickListener {
//            startActivity(Intent(requireContext(), MainActivity::class.java))
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, OnBoardingRemindFragment())
//                .addToBackStack(null)
//                .commit()
            if(scheduleMap.isNotEmpty()) {
                val request = getScheduleRequest()
                onBoardingService.setScheduleView(this)
                onBoardingService.postSchedules(request)
            }
            else{
                parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingRemindFragment())
                .addToBackStack(null)
                .commit()
            }
        }

        // RecyclerView 세팅
        binding.scheduleRv.adapter = scheduleAdapter
        binding.scheduleRv.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddIv.setOnClickListener {
            val list = scheduleMap.getOrPut(selectedDayIndex) { mutableListOf() }

            val bottomSheet = BottomSheetScheduleFragment(
                selectedDayIndex,
                list.toList(),
                onScheduleAdded = { schedule ->
                    list.add(schedule)
                    scheduleAdapter.submitList(list.toList())
                },
                sleepStart = sleepStart,
                sleepEnd = sleepEnd
            )

            bottomSheet.show(parentFragmentManager, "BottomSheetScheduleFragment")
        }

        dayTextViews = listOf(
            binding.sunTv, binding.monTv, binding.tueTv,
            binding.wedTv, binding.thuTv, binding.friTv, binding.satTv
        )

        setupDaySelection()
        updateDayHighlight(selectedDayIndex)

    }

    private fun setupDaySelection() {
        dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                updateDayHighlight(index)
            }
        }
    }

    //요일 선택했을 때 하이라이팅, 해당 요일 일정 보여주기
    private fun updateDayHighlight(selectedIndex: Int) {

        dayTextViews[selectedDayIndex].background = null
        dayTextViews[selectedDayIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        dayTextViews[selectedIndex].background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_day_selected)
        dayTextViews[selectedIndex].setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        selectedDayIndex = selectedIndex

        // 해당 요일의 일정 보여주기
        scheduleAdapter.submitList(scheduleMap[selectedDayIndex] ?: emptyList())
    }

    private fun getScheduleRequest(): ScheduleRequest {
        val allSchedules = mutableListOf<com.example.teumteum.data.remote.onboarding.dto.Schedule>()

        val weekMap = mapOf(
            0 to Week.SUNDAY,
            1 to Week.MONDAY,
            2 to Week.TUESDAY,
            3 to Week.WEDNESDAY,
            4 to Week.THURSDAY,
            5 to Week.FRIDAY,
            6 to Week.SATURDAY
        )

        scheduleMap.forEach { (dayIndex, schedules) ->
            val weekdayEnum = weekMap[dayIndex] ?: Week.SUNDAY
            schedules.forEach { s ->
                allSchedules.add(
                    com.example.teumteum.data.remote.onboarding.dto.Schedule(
                        title = s.title,
                        description = s.description,
                        weekday = weekdayEnum,
                        startTime = s.startTime.toString(),
                        endTime = s.endTime.toString()
                    )
                )
            }
        }

        return ScheduleRequest(routine = allSchedules)
    }
}