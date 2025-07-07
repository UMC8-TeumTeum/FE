package com.example.teumteum.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.example.teumteum.R

class CustomTimePickerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_custom_time_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ampmPicker = view.findViewById<NumberPicker>(R.id.ampm_picker)
        val hourPicker = view.findViewById<NumberPicker>(R.id.hour_picker)
        val minutePicker = view.findViewById<NumberPicker>(R.id.minute_picker)

        ampmPicker.minValue = 0
        ampmPicker.maxValue = 1
        ampmPicker.displayedValues = arrayOf("오전", "오후")

        hourPicker.minValue = 1
        hourPicker.maxValue = 12

        minutePicker.minValue = 0
        minutePicker.maxValue = 5
        minutePicker.displayedValues = arrayOf("00", "10", "20", "30", "40", "50")
    }
}