package com.example.teumteum.ui.filling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFillingActivity01Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

class FillingActivity01Fragment : Fragment() {

    private lateinit var binding: FragmentFillingActivity01Binding

    private var selectedTime: String? = null
    private var selectedLocation: String? = null
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFillingActivity01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        binding.fillingActivityLocationClearBtn.setOnClickListener {
            binding.fillingActivityLocationEt.setText("")
        }
        binding.fillingActivityCategoryClearBtn.setOnClickListener {
            binding.fillingActivityCategoryEt.setText("")
        }

        val selectedStroke = ContextCompat.getColor(requireContext(), R.color.main_1)
        val defaultStroke = ContextCompat.getColor(requireContext(), R.color.teumteum_bg)

        val selectedBg = ContextCompat.getColor(requireContext(), R.color.main_1)
        val selectedText = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.main_2)
        val defaultText = ContextCompat.getColor(requireContext(), R.color.text_primary)

        // 시간 선택
        val timeCards = listOf(
            binding.fillingActivityTime01Cv,
            binding.fillingActivityTime02Cv,
            binding.fillingActivityTime03Cv,
            binding.fillingActivityTime04Cv
        )
        timeCards.forEach { timeBtn ->
            timeBtn.setOnClickListener {
                timeCards.forEach {
                    it.strokeColor = defaultStroke
                    it.strokeWidth = 0
                }

                timeBtn.strokeColor = selectedStroke
                timeBtn.strokeWidth = 4
                selectedTime = timeBtn.toString()
                updateNextButtonState()
            }
        }

        // 위치 선택
        val locationButtons = listOf(
            binding.btnFillingActivityLocation01,
            binding.btnFillingActivityLocation02,
            binding.btnFillingActivityLocation03,
            binding.btnFillingActivityLocation04,
            binding.btnFillingActivityLocation05,
            binding.btnFillingActivityLocation06
        )

        locationButtons.forEach { locationBtn ->
            locationBtn.setOnClickListener {
                locationButtons.forEach {
                    it.setBackgroundColor(defaultBg)
                    it.setTextColor(defaultText)
                }

                locationBtn.setBackgroundColor(selectedBg)
                locationBtn.setTextColor(selectedText)
                selectedLocation = locationBtn.text.toString()

                updateNextButtonState()
            }
        }

        // 카테고리 선택
        val categoryButtons = listOf(
            binding.btnFillingActivityCategory01,
            binding.btnFillingActivityCategory02,
            binding.btnFillingActivityCategory03,
            binding.btnFillingActivityCategory04,
            binding.btnFillingActivityCategory05,
            binding.btnFillingActivityCategory06
        )

        categoryButtons.forEach { categoryBtn ->
            categoryBtn.setOnClickListener {
                categoryButtons.forEach {
                    it.setBackgroundColor(defaultBg)
                    it.setTextColor(defaultText)
                }

                categoryBtn.setBackgroundColor(selectedBg)
                categoryBtn.setTextColor(selectedText)
                selectedCategory = categoryBtn.text.toString()

                updateNextButtonState()
            }
        }

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.searchBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FillingActivity02Fragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateNextButtonState() {
        val isAllSelected = selectedTime != null && selectedLocation != null && selectedCategory != null

        binding.searchBtn.isEnabled = isAllSelected

        binding.searchBtn.setBackgroundColor(
            if (isAllSelected)
                ContextCompat.getColor(requireContext(), R.color.text_primary)
            else
                ContextCompat.getColor(requireContext(), R.color.teumteum_bg)
        )

        binding.searchBtn.setTextColor(
            if (isAllSelected)
                ContextCompat.getColor(requireContext(), R.color.white)
            else
                ContextCompat.getColor(requireContext(), R.color.text_primary)
        )
    }
}