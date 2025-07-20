package com.example.teumteum.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentWeeklyCalendarBinding
import com.example.teumteum.util.*
import java.time.LocalDate

class WeeklyCalendarFragment : Fragment() {

    private lateinit var binding: FragmentWeeklyCalendarBinding
    private lateinit var textViewList: List<TextView>
    private lateinit var dates: List<LocalDate>

    private var position: Int = 0
    private lateinit var onClickListener: IDateClickListener
    private val todayPosition = Int.MAX_VALUE / 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeeklyCalendarBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newDate = calculateNewDate()
        calculateDatesOfWeek(newDate)
        setOneWeekDateIntoTextView()
        selectTodayIfInWeek()
    }

    override fun onResume() {
        super.onResume()
        setPrevSelectedDate()
    }

    override fun onPause() {
        super.onPause()
        resetUi()
    }

    private fun initViews() {
        with(binding) {
            textViewList = listOf(tv1, tv2, tv3, tv4, tv5, tv6, tv7)
        }
    }

    private fun calculateNewDate(): LocalDate {
        val curDate = LocalDate.now()
        return when {
            position < todayPosition -> curDate.minusDays(((todayPosition - position) * 7).toLong())
            position > todayPosition -> curDate.plusDays(((position - todayPosition) * 7).toLong())
            else -> curDate
        }
    }

    private fun calculateDatesOfWeek(today: LocalDate) {
        val dayOfWeek = today.dayOfWeek.value % 7
        val startOfWeek = today.minusDays(dayOfWeek.toLong())
        dates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    private fun setOneWeekDateIntoTextView() {
        for (i in textViewList.indices) {
            val date = dates[i]
            val textView = textViewList[i]
            textView.text = date.dayOfMonth.toString()
            textView.setOnClickListener {
                resetUi()
                setSelectedDate(requireContext(), textView)
                saveSelectedDate(requireContext(), date)
                onClickListener.onClickDate(date)
            }
        }
    }

    private fun setPrevSelectedDate() {
        val selected = getSavedDateOrToday(requireContext())
        for (i in textViewList.indices) {
            if (dates[i] == selected) {
                setSelectedDate(requireContext(), textViewList[i])
            }
        }
    }

    private fun selectTodayIfInWeek() {
        val today = LocalDate.now()
        for (i in textViewList.indices) {
            if (dates[i] == today) {
                setSelectedDate(requireContext(), textViewList[i])
                saveSelectedDate(requireContext(), today)
                break
            }
        }
    }

    private fun resetUi() {
        val today = LocalDate.now()
        for (i in textViewList.indices) {
            val date = dates[i]
            val textView = textViewList[i]
            if (date == today) {
                setTodayStyle(requireContext(), textView)
            } else {
                resetDateStyle(requireContext(), textView)
            }
        }
    }

    companion object {
        fun newInstance(position: Int, onClickListener: IDateClickListener): WeeklyCalendarFragment {
            val fragment = WeeklyCalendarFragment()
            fragment.position = position
            fragment.onClickListener = onClickListener
            return fragment
        }
    }
}