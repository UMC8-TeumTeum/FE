package com.example.teumteum.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFillingActivity01Binding
import com.example.teumteum.ui.activity.viewModel.ActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FillingActivity01Fragment : Fragment() {

    private var _binding: FragmentFillingActivity01Binding? = null
    private val binding get() = _binding!!

    private var selectedTimeTag: String? = null
    private var selectedTimeButton: View? = null

    private var selectedLocationText: String? = null
    private var selectedLocationButton: View? = null

    private var selectedCategoryText: String? = null
    private var selectedCategoryButton: View? = null

    private val viewModel: ActivityViewModel by activityViewModels()

    private var isRestoring = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFillingActivity01Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selectedStroke = ContextCompat.getColor(requireContext(), R.color.main_1)
        val defaultStroke = ContextCompat.getColor(requireContext(), R.color.teumteum_bg)

        val selectedBg = ContextCompat.getColor(requireContext(), R.color.main_1)
        val selectedText = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.main_2)
        val defaultText = ContextCompat.getColor(requireContext(), R.color.text_primary)

        // 자동 상태 복원 방지
        binding.fillingActivityLocationEt.isSaveEnabled = false
        binding.fillingActivityCategoryEt.isSaveEnabled = false

        // 이전 View 참조 제거
        selectedTimeButton = null
        selectedLocationButton = null
        selectedCategoryButton = null
        selectedTimeTag = null
        selectedLocationText = null
        selectedCategoryText = null

        activity?.findViewById<BottomNavigationView>(R.id.main_bnv)?.visibility = View.GONE

        binding.fillingActivityLocationClearBtn.setOnClickListener {
            binding.fillingActivityLocationEt.setText("")
        }

        binding.fillingActivityCategoryClearBtn.setOnClickListener {
            binding.fillingActivityCategoryEt.setText("")
        }

        binding.fillingLl.bringToFront()

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

                viewModel.updateFillingFormState { it.copy(selectedTime = selectedTimeTag) }
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

        val locationIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        locationButtons.forEachIndexed { index, locationBtn ->
            locationBtn.tag = locationIds[index]
            locationBtn.setOnClickListener {
                // 위치 버튼 선택 시 직접 입력 선택 해제
                if (!binding.fillingActivityLocationEt.text.isNullOrBlank()) {
                    binding.fillingActivityLocationEt.setText("")
                    selectedLocationText = null
                }

                // 이미 선택된 같은 버튼이면 해제
                if (selectedLocationButton === locationBtn) {
                    locationBtn.setBackgroundColor(defaultBg)
                    locationBtn.setTextColor(defaultText)
                    selectedLocationButton = null
                    viewModel.updateFillingFormState { it.copy(locationId = null, customLocation = null) }

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
                selectedLocationButton = locationBtn

                val selectedLocationId = locationBtn.tag as? Long
                viewModel.updateFillingFormState { it.copy(locationId = selectedLocationId, customLocation = null) }

                updateNextButtonState()
            }
        }

        binding.fillingActivityLocationEt.doOnTextChanged { text, _, _, _ ->
            if (isRestoring) return@doOnTextChanged

            val locationText = text?.toString()?.trim()

            // 직접 입력이 있으면 selectedLocationText에 반영
            selectedLocationText = if (!locationText.isNullOrEmpty()) locationText else null

            // 직접 입력 선택 시 위치 버튼 해제
            if (!locationText.isNullOrEmpty() && selectedLocationButton != null) {
                (selectedLocationButton as? TextView)?.apply {
                    setBackgroundColor(defaultBg)
                    setTextColor(defaultText)
                }
                selectedLocationButton = null
            }

            viewModel.updateFillingFormState {
                it.copy(customLocation = selectedLocationText, locationId = null)
            }

            // 버튼 상태 갱신
            updateNextButtonState()
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
                // 카테고리 버튼 선택 시 직접 입력 선택 해제
                if (!binding.fillingActivityCategoryEt.text.isNullOrBlank()) {
                    binding.fillingActivityCategoryEt.setText("")
                    selectedCategoryText = null
                }

                // 이미 선택된 같은 버튼이면 해제
                if (selectedCategoryButton === categoryBtn) {
                    categoryBtn.setBackgroundColor(defaultBg)
                    categoryBtn.setTextColor(defaultText)
                    selectedCategoryButton = null

                    viewModel.updateFillingFormState { it.copy(categoryId = null, customCategory = null) }

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

                val selectedCategoryId = categoryBtn.tag as? Long
                viewModel.updateFillingFormState { it.copy(categoryId = selectedCategoryId, customCategory = null) }

                updateNextButtonState()
            }
        }

        binding.fillingActivityCategoryEt.doOnTextChanged { text, _, _, _ ->
            if (isRestoring) return@doOnTextChanged

            val categoryText = text?.toString()?.trim()

            // 직접 입력이 있으면 selectedCategoryText에 반영
            selectedCategoryText = if (!categoryText.isNullOrEmpty()) categoryText else null

            // 직접 입력 선택 시 카테고리 버튼 해제
            if (!categoryText.isNullOrEmpty() && selectedCategoryButton != null) {
                (selectedCategoryButton as? TextView)?.apply {
                    setBackgroundColor(defaultBg)
                    setTextColor(defaultText)
                }
                selectedCategoryButton = null
            }

            viewModel.updateFillingFormState {
                it.copy(customCategory = selectedCategoryText, categoryId = null)
            }

            // 버튼 상태 갱신
            updateNextButtonState()
        }

        binding.backArrowIv.setOnClickListener {
            viewModel.clearFillingFormState()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.searchBtn.setOnClickListener {

            val selectedLocationId = selectedLocationButton?.tag as? Long
            val selectedCategoryId = selectedCategoryButton?.tag as? Long

            val customLocation = binding.fillingActivityLocationEt.text.toString().trim()
                .takeIf { it.isNotEmpty() }
            val customCategory = binding.fillingActivityCategoryEt.text.toString().trim()
                .takeIf { it.isNotEmpty() }

            // 위치 선택 + 직접 입력 시 예외 처리
            if (selectedLocationButton != null && selectedLocationText != null) {
                Toast.makeText(requireContext(), "위치와 직접 입력은 둘 중 하나만 선택해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 카테고리 선택 + 직접 입력 시 예외 처리
            if (selectedCategoryButton != null && selectedCategoryText != null) {
                Toast.makeText(requireContext(), "카테고리와 직접 입력은 둘 중 하나만 선택해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("selectedTime", selectedTimeTag ?: "")

                selectedLocationId?.let { putLong("locationId", it) }
                    ?: putString("customLocation", customLocation)

                selectedCategoryId?.let { putLong("categoryId", it) }
                    ?: putString("customCategory", customCategory)
            }

            val fragment = FillingActivity02Fragment().apply {
                arguments = bundle
            }

            viewModel.clearActivityResults()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

        setupObservers()

        restoreFormFromViewModel() // viewModel 값으로 ui 복원
        updateNextButtonState()
    }

    private fun restoreFormFromViewModel() {
        val state = viewModel.fillingFormState.value ?: return

        val selectedStroke = ContextCompat.getColor(requireContext(), R.color.main_1)
        val defaultStroke = ContextCompat.getColor(requireContext(), R.color.teumteum_bg)

        val selectedBg = ContextCompat.getColor(requireContext(), R.color.main_1)
        val selectedText = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.main_2)
        val defaultText = ContextCompat.getColor(requireContext(), R.color.text_primary)

        isRestoring = true
        try {
            // 시간 복원
            val timeCards = listOf(
                binding.fillingActivityTime01Cv.apply { tag = "10m" },
                binding.fillingActivityTime02Cv.apply { tag = "20m" },
                binding.fillingActivityTime03Cv.apply { tag = "30m" },
                binding.fillingActivityTime04Cv.apply { tag = "1h" }
            )

            timeCards.forEach {
                it.strokeColor = defaultStroke
                it.strokeWidth = 0
            }

            selectedTimeTag = state.selectedTime
            val timeView = timeCards.firstOrNull { it.tag == state.selectedTime }
            timeView?.let {
                it.strokeColor = selectedStroke
                it.strokeWidth = 4
                selectedTimeButton = it
            }

            // 위치 복원
            val locationButtons = listOf(
                binding.btnFillingActivityLocation01,
                binding.btnFillingActivityLocation02,
                binding.btnFillingActivityLocation03,
                binding.btnFillingActivityLocation04,
                binding.btnFillingActivityLocation05,
                binding.btnFillingActivityLocation06
            )

            locationButtons.forEach {
                it.setBackgroundColor(defaultBg)
                it.setTextColor(defaultText)
            }
            selectedLocationButton = null
            selectedLocationText = null
            binding.fillingActivityLocationEt.setText("")

            when {
                state.locationId != null -> {
                    val btn = locationButtons.firstOrNull { (it.tag as? Long) == state.locationId }
                    btn?.let {
                        it.setBackgroundColor(selectedBg)
                        it.setTextColor(selectedText)
                        selectedLocationButton = it
                    }
                }
                !state.customLocation.isNullOrBlank() -> {
                    binding.fillingActivityLocationEt.setText(state.customLocation)
                    selectedLocationText = state.customLocation
                }
            }

            // 카테고리 복원
            val categoryButtons = listOf(
                binding.btnFillingActivityCategory01,
                binding.btnFillingActivityCategory02,
                binding.btnFillingActivityCategory03,
                binding.btnFillingActivityCategory04,
                binding.btnFillingActivityCategory05,
                binding.btnFillingActivityCategory06
            )

            categoryButtons.forEach {
                it.setBackgroundColor(defaultBg)
                it.setTextColor(defaultText)
            }
            selectedCategoryButton = null
            selectedCategoryText = null
            binding.fillingActivityCategoryEt.setText("")

            when {
                state.categoryId != null -> {
                    val btn = categoryButtons.firstOrNull { (it.tag as? Long) == state.categoryId }
                    btn?.let {
                        it.setBackgroundColor(selectedBg)
                        it.setTextColor(selectedText)
                        selectedCategoryButton = it
                    }
                }
                !state.customCategory.isNullOrBlank() -> {
                    binding.fillingActivityCategoryEt.setText(state.customCategory)
                    selectedCategoryText = state.customCategory
                }
            }

            updateNextButtonState()
        } finally {
            isRestoring = false
        }
    }


    private fun updateNextButtonState() {

        val isAllSelected = selectedTimeTag != null && (selectedLocationButton != null || selectedLocationText != null) && (selectedCategoryButton != null || selectedCategoryText != null)

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

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e("FillingActivity01Fragment", "에러 발생: $it")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        // 폼 초기화
        if (isRemoving || (activity?.isFinishing == true)) {
            viewModel.clearFillingFormState()
        }
        super.onDestroy()
    }
}