package com.example.teumteum.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.data.remote.activity.dto.ActivityAiResult
import com.example.teumteum.data.remote.activity.dto.ActivityWishResult
import com.example.teumteum.databinding.FragmentFillingActivity01Binding
import com.example.teumteum.ui.activity.view.ActivityAiView
import com.example.teumteum.ui.activity.view.ActivityWishView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FillingActivity01Fragment : Fragment(), ActivityWishView, ActivityAiView {

    private lateinit var binding: FragmentFillingActivity01Binding

    private var selectedTimeTag: String? = null
    private var selectedTimeButton: View? = null

    private var selectedLocation: String? = null

    private var selectedCategoryText: String? = null
    private var selectedCategoryButton: View? = null

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

        val categoryIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        categoryButtons.forEachIndexed { index, categoryBtn ->
            categoryBtn.tag = categoryIds[index]
            categoryBtn.setOnClickListener {
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

            val bundle = Bundle().apply {
                putString("selectedTime", selectedTimeTag)
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

    override fun onGetActivityWishSuccess(code: String, wishes: List<ActivityWishResult>) {
        Toast.makeText(requireContext(), "채움활동 위시 탐색 성공", Toast.LENGTH_SHORT).show()
    }

    override fun onGetActivityWishFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "HOME4042" -> "해당 카테고리를 찾을 수 없습니다."
            "HOME4003" -> "카테고리는 필수 항목입니다."
            "HOME4004" -> "카테고리와 직접 입력은 둘 중 하나만 선택해야 합니다."
            "COMMON400" -> "널이어서는 안됩니다."
            "COMMON500" -> "서버 오류입니다. 관리자에게 문의해주세요."
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> "위시 조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onGetActivityAiSuccess(code: String, wishes: List<ActivityAiResult>) {
        Toast.makeText(requireContext(), "채움활동 ai컨텐츠 탐색 성공", Toast.LENGTH_SHORT).show()
    }

    override fun onGetActivityAiFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "COMMON500" -> "서버 오류입니다. 관리자에게 문의해주세요."
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> "ai컨텐츠 조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }
}