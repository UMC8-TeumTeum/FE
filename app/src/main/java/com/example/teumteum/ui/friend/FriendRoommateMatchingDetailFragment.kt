package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateMatchingDetailBinding
import com.example.teumteum.ui.friend.adapter.TimeCardAdapter
import com.example.teumteum.ui.friend.data.SelectedTime
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.auth.SignUpActivity
import com.example.teumteum.ui.friend.adapter.TimeConflictCardAdapter
import com.example.teumteum.utils.enableTapToNext
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class FriendRoommateMatchingDetailFragment : Fragment() {

    private lateinit var binding: FragmentFriendRoommateMatchingDetailBinding
    private lateinit var timeConflictCardAdapter: TimeConflictCardAdapter

    // 선택된 날짜 저장 (이전 Fragment에서 전달)
    private var selectedDate: String = ""

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendRoommateMatchingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBar(75)
        (activity as? MainActivity)?.hideBottomBar()

        // 이전 Fragment에서 선택된 날짜 받기 (예: "25.08.21(목)" 또는 "2025-08-21")
        selectedDate = arguments?.getString("selected_date") ?: ""

        val fullText = "틈 요청 제목을 작성해주세요*"
        val spannable = android.text.SpannableString(fullText)
        val starIndex = fullText.indexOf("*")

        if (starIndex != -1) {
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#7770FE")),
                starIndex,
                starIndex + 1,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.teumRequestTitle.text = spannable
        }

        binding.clearTitleBtn.setOnClickListener {
            binding.editTextTitle.text.clear()
        }

        binding.clearDetailBtn.setOnClickListener {
            binding.editTextDetail.text.clear()
        }

        binding.editTextTitle.addTextChangedListener {
            updateNextButtonState()
        }

        binding.editTextDetail.addTextChangedListener {
            updateNextButtonState()
        }

        // 전송할게요 버튼 클릭 시 dialogFragment 화면 띄우기
        binding.sendBtn.setOnClickListener {
            setViewModelData()

            val dialog = FriendMatchingPreviewDialog()
            dialog.show(parentFragmentManager, "PreviewDialog")
        }

        // 뒤로가기 버튼 처리
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupTimeCardRecyclerView()
        observeViewModel()
        timeConflictCardAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                // notify 이후 다음 프레임에 상태 읽기 (선택 반영 보장)
                binding.possibleTimeRc.post { updateNextButtonState() }
            }
            override fun onChanged() = onItemRangeChanged(0, timeConflictCardAdapter.itemCount)
        })

        // 초기 버튼 상태 설정
        binding.sendBtn.isEnabled = false
        binding.sendBtn.setBackgroundColor(android.graphics.Color.parseColor("#F6F6F6"))
    }

    private fun updateNextButtonState() {
        val isTimeSelected = timeConflictCardAdapter.getSelectedItem() != null
        val isTitleFilled = binding.editTextTitle.text.toString().isNotBlank()
        val isEnabled = isTimeSelected && isTitleFilled

        binding.sendBtn.isEnabled = isEnabled
        binding.sendBtn.setBackgroundColor(
            if (isEnabled) 0xFF0F0F0F.toInt() else 0xFFF6F6F6.toInt()
        )
        binding.sendBtn.setTextColor(
            if (isEnabled) 0xFFFFFFFF.toInt() else 0xFF0F0F0F.toInt()
        )
    }

    private fun setupTimeCardRecyclerView() {
        timeConflictCardAdapter = TimeConflictCardAdapter(
            onTimeClick = { position, isStart, _, _, current ->
                showCustomTimePicker(initial = current) { picked ->
                    // 범위 제한 제거
                    timeConflictCardAdapter.updateTime(position, isStart, picked)
                }
            },
            onTimeCompleted = { start, end ->
                viewModel.checkTeumConflict(
                    date = convertDateFormat(selectedDate),
                    startTime = start,
                    endTime = end
                )
            }
        )

        binding.possibleTimeRc.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timeConflictCardAdapter
        }
    }

    // 정해진 시간 범위의 시간으로 선택했는지 확인
    private fun isWithinRange(picked: String, min: String, max: String): Boolean {
        val normMax = if (max == "24:00") "23:59" else max
        val t = LocalTime.parse(picked)
        val tMin = LocalTime.parse(min)
        val tMax = LocalTime.parse(normMax)
        return !t.isBefore(tMin) && !t.isAfter(tMax)
    }

    private fun showCustomTimePicker(
        initial: String,
        onPicked: (String) -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null, false)
        val am = dialogView.findViewById<NumberPicker>(R.id.ampmPicker01Np)
        val h = dialogView.findViewById<NumberPicker>(R.id.hourPicker01Np)
        val m = dialogView.findViewById<NumberPicker>(R.id.minutePicker01Np)
        val mins = arrayOf("00","10","20","30","40","50")

        am.minValue = 0
        am.maxValue = 1
        am.displayedValues = arrayOf("AM","PM")
        am.wrapSelectorWheel = true

        h.minValue = 1
        h.maxValue = 12
        h.wrapSelectorWheel = true

        m.minValue = 0
        m.maxValue = mins.size-1
        m.displayedValues = mins
        m.wrapSelectorWheel = true

        am.enableTapToNext(wrap = true)
        h.enableTapToNext(wrap = true)
        m.enableTapToNext(wrap = true)

        // 초기값 세팅
        runCatching {
            val (ih, im) = initial.split(":").map { it.toInt() }
            val isAm = ih < 12
            am.value = if (isAm) 0 else 1
            val th = if (ih % 12 == 0) 12 else ih % 12
            h.value = th
            m.value = mins.indexOf(String.format("%02d", im)).coerceAtLeast(0)
        }

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val isAm = am.value == 0
            var hour24 = h.value % 12
            if (!isAm) hour24 += 12
            val mm = mins[m.value]
            val picked = String.format("%02d:%s", hour24, mm)
            onPicked(picked)
            dialog.dismiss()
        }
        updateNextButtonState()
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.possibleTimeList.observe(viewLifecycleOwner) { list ->
            val nonNullList = list.filterNotNull()

            // 오늘인 경우, 현재 시각 기준으로 카드들 필터링/조정
            val dateFormatted = convertDateFormat(selectedDate)
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val filteredList = if (dateFormatted == today) {
                val now = LocalDateTime.now()
                val currentMinutes = now.hour * 60 + now.minute
                // 현재 시각을 10분 단위로 올림
                val adjustedCurrentMinutes = ((currentMinutes + 9) / 10) * 10

                nonNullList.mapNotNull { card ->
                    val startMinutes = timeToMinutes(card.startTime)
                    val endMinutes = timeToMinutes(card.endTime)

                    when {
                        // endTime이 현재 시각 이후가 아니면 (현재 시각 이하면) 제외
                        endMinutes <= currentMinutes -> {
                            null
                        }
                        // startTime과 endTime 사이에 현재 시각이 있으면 조정된 현재 시각을 startTime으로 설정
                        startMinutes <= currentMinutes && endMinutes > currentMinutes -> {
                            val adjustedStartTime = minutesToTime(adjustedCurrentMinutes)
                            // 조정된 시작 시간이 종료 시간보다 크거나 같으면 제외
                            if (adjustedCurrentMinutes >= endMinutes) {
                                null
                            } else {
                                card.copy(startTime = adjustedStartTime)
                            }
                        }
                        // startTime이 현재 시각 이후면 그대로 사용
                        else -> {
                            card
                        }
                    }
                }
            } else {
                nonNullList
            }

            timeConflictCardAdapter.setData(filteredList)
            updateNextButtonState()
        }

        viewModel.teumConflict.observe(viewLifecycleOwner) { response ->
            response ?: return@observe

            if (response.hasConflict && response.conflictingRequests.isNotEmpty()) {
                FriendSendRequestBottomSheet
                    .newInstance(response.conflictingRequests)
                    .show(parentFragmentManager, FriendSendRequestBottomSheet.TAG)

                viewModel.clearTeumConflict()
            }
        }

    }

    // 선택 시간이 (선택 날짜가 오늘인 경우) 현재 시각 이후인지 확인
    private fun isAfterCurrentTime(selectedTime: String): Boolean {
        val dateFormatted = convertDateFormat(selectedDate)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (dateFormatted != today) return true // 오늘이 아니면 통과

        val now = LocalDateTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val selectedMinutes = timeToMinutes(selectedTime)
        return selectedMinutes > currentMinutes
    }

    // 날짜 형식 변환 ("yy.MM.dd(E)" -> "yyyy-MM-dd"). 이미 yyyy-MM-dd면 그대로 반환
    private fun convertDateFormat(dateStr: String): String {
        if (dateStr.isBlank()) return dateStr
        return try {
            val formatterInput = DateTimeFormatter.ofPattern("yy.MM.dd(E)", Locale.KOREAN)
            val formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN)
            LocalDate.parse(dateStr, formatterInput).format(formatterOutput)
        } catch (e: Exception) {
            // 파싱 실패 시, 원본을 반환 (이미 yyyy-MM-dd 형태일 수 있음)
            dateStr
        }
    }

    // 시간 문자열(HH:mm)을 분으로 변환
    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    // 분을 HH:mm 문자열로 변환
    private fun minutesToTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }

    private fun setViewModelData() {
        val selectedTime = timeConflictCardAdapter.getSelectedItem()
        viewModel.setTeumRequestSelectedTime(
            SelectedTime(
                startTime = convert24To00(selectedTime!!.startTime),
                endTime = convert24To00(selectedTime.endTime)
            )
        )
        viewModel.setTeumRequestTitle(binding.editTextTitle.text.toString())
        if (binding.editTextDetail.text.isEmpty()) {
            // 기본 멘트
            viewModel.setTeumRequestDescription("같이 빈틈을 채워봐요.")
        } else {
            viewModel.setTeumRequestDescription(binding.editTextDetail.text.toString())
        }
    }

    // 24:00 -> 00:00 변환
    private fun convert24To00(timeStr: String): String {
        return if (timeStr == "24:00") "00:00" else timeStr
    }
}
