package com.example.teumteum.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.activity.model.ActivityAiRequest
import com.example.teumteum.data.remote.activity.model.ActivityWishRequest
import com.example.teumteum.databinding.FragmentFillingActivity01Binding
import com.example.teumteum.ui.activity.viewModel.ActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FillingActivity01Fragment : Fragment() {

    private lateinit var binding: FragmentFillingActivity01Binding

    private var selectedTimeTag: String? = null
    private var selectedTimeButton: View? = null

    private var selectedLocation: String? = null
    private var selectedLocationButton: View? = null

    private var selectedCategoryText: String? = null
    private var selectedCategoryButton: View? = null

    private val activityViewModel: ActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFillingActivity01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selectedStroke = ContextCompat.getColor(requireContext(), R.color.main_1)
        val defaultStroke = ContextCompat.getColor(requireContext(), R.color.teumteum_bg)

        val selectedBg = ContextCompat.getColor(requireContext(), R.color.main_1)
        val selectedText = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.main_2)
        val defaultText = ContextCompat.getColor(requireContext(), R.color.text_primary)

        activity?.findViewById<BottomNavigationView>(R.id.main_bnv)?.visibility = View.GONE

        binding.fillingActivityLocationClearBtn.setOnClickListener {
            binding.fillingActivityLocationEt.setText("")
        }

        binding.fillingActivityCategoryClearBtn.setOnClickListener {
            binding.fillingActivityCategoryEt.setText("")
        }

        // 시간 선택
        val timeCards = listOf(
            binding.fillingActivityTime01Cv.apply { tag = "10m" },
            binding.fillingActivityTime02Cv.apply { tag = "20m" },
            binding.fillingActivityTime03Cv.apply { tag = "30m" },
            binding.fillingActivityTime04Cv.apply { tag = "1h" }
        )

        timeCards.forEach { timeBtn ->
            timeBtn.setOnClickListener {
                timeCards.forEach {
                    it.strokeColor = defaultStroke
                    it.strokeWidth = 0
                }

                timeBtn.strokeColor = selectedStroke
                timeBtn.strokeWidth = 4
                selectedTimeTag = timeBtn.tag as String
                selectedTimeButton = timeBtn
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
                // 이미 선택된 같은 버튼이면 해제
                if (selectedLocationButton === locationBtn) {
                    locationBtn.setBackgroundColor(defaultBg)
                    locationBtn.setTextColor(defaultText)
                    selectedLocation = null
                    selectedLocationButton = null
                    updateNextButtonState()
                    return@setOnClickListener
                }

                // 기존 선택 초기화 + 새 선택
                locationButtons.forEach {
                    it.setBackgroundColor(defaultBg)
                    it.setTextColor(defaultText)
                }
                locationBtn.setBackgroundColor(selectedBg)
                locationBtn.setTextColor(selectedText)
                selectedLocation = locationBtn.text.toString()
                selectedLocationButton = locationBtn
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

        val categoryIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        categoryButtons.forEachIndexed { index, categoryBtn ->
            categoryBtn.tag = categoryIds[index]
            categoryBtn.setOnClickListener {
                // 이미 선택된 같은 버튼이면 해제
                if (selectedCategoryButton === categoryBtn) {
                    categoryBtn.setBackgroundColor(defaultBg)
                    categoryBtn.setTextColor(defaultText)
                    selectedCategoryButton = null
                    selectedCategoryText = null
                    updateNextButtonState()
                    return@setOnClickListener
                }

                // 기존 선택 초기화 + 새 선택
                categoryButtons.forEach {
                    it.setBackgroundColor(defaultBg)
                    it.setTextColor(defaultText)
                }
                categoryBtn.setBackgroundColor(selectedBg)
                categoryBtn.setTextColor(selectedText)
                selectedCategoryButton = categoryBtn
                selectedCategoryText = categoryBtn.text.toString()
                updateNextButtonState()
            }
        }

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.searchBtn.setOnClickListener {

            // 카테고리 선택 + 직접 입력 시 예외 처리
            if (binding.fillingActivityCategoryEt.text.toString().isNotBlank()) {
                Toast.makeText(requireContext(), "카테고리와 직접 입력은 둘 중 하나만 선택해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val wishRequest = ActivityWishRequest(
                estimatedDuration = selectedTimeTag ?: "",
                categoryId = selectedCategoryButton?.tag as? Long,
                customCategory = binding.fillingActivityCategoryEt.text.toString()
            )

            activityViewModel.activityWish(wishRequest)

            val aiRequest = ActivityAiRequest(
                estimatedDuration = selectedTimeTag ?: "",
                location = binding.fillingActivityLocationEt.text.toString(),
                categoryId = selectedCategoryButton?.tag as? Long,
                customCategory = binding.fillingActivityCategoryEt.text.toString()
            )

            activityViewModel.activityAi(aiRequest)

            val bundle = Bundle().apply {
                putString("selectedTime", selectedTimeTag)
                putString("location", binding.fillingActivityLocationEt.text.toString())
                putString("selectedCategory", selectedCategoryText)
                putString("customCategory", binding.fillingActivityCategoryEt.text.toString())
            }

            val fragment = FillingActivity02Fragment().apply {
                arguments = bundle
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

        setupObservers()
    }

    private fun updateNextButtonState() {
        val isAllSelected = selectedTimeTag != null && selectedLocation != null && selectedCategoryText != null

        binding.searchBtn.isEnabled = isAllSelected
        binding.searchBtn.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (isAllSelected) R.color.text_primary else R.color.teumteum_bg
            )
        )
        binding.searchBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isAllSelected) R.color.white else R.color.text_primary
            )
        )
    }

    private fun setupObservers() {

        activityViewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("FillingActivity01Fragment", "에러 발생: $it")
            }
        }
    }
}