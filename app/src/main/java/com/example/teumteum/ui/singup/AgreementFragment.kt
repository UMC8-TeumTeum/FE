package com.example.teumteum.ui.singup

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentAgreementBinding

class AgreementFragment : Fragment() {

    private lateinit var binding: FragmentAgreementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAgreementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBarVisible(true)
        (activity as? SignUpActivity)?.setProgressBar(50)


        setupCheckBoxListeners()
        updateNextButtonState()
        setupSelectAllCheckbox()

        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CompleteFragment())
                .addToBackStack(null)
                .commit()
        }

        // 약관 텍스트뷰 클릭 시 상세 프래그먼트로 이동
        binding.term1Tv.setOnClickListener { openTermsDetail("term1","서비스 이용 약관 동의 (필수)") }
        binding.term2Tv.setOnClickListener { openTermsDetail("term2","개인정보 수집 및 이용 동의 (필수)") }
        binding.term3Tv.setOnClickListener { openTermsDetail("term3","개인정보 제3자 제공에 대한 안내 (선택)") }
        binding.term4Tv.setOnClickListener { openTermsDetail("term4","마케팅 및 광고성 정보 수신 동의 (선택)") }
    }

    //체크박스 리스너
    private fun setupCheckBoxListeners() {
        val listener = { checkBox: CompoundButton ->
            updateNextButtonState()
            syncSelectAllCheckbox()
            setCheckBoxTint(checkBox, checkBox.isChecked)
        }

        binding.term1Checkbox.setOnCheckedChangeListener { _, _ -> listener(binding.term1Checkbox) }
        binding.term2Checkbox.setOnCheckedChangeListener { _, _ -> listener(binding.term2Checkbox) }
        binding.term3Checkbox.setOnCheckedChangeListener { _, _ -> listener(binding.term3Checkbox) }
        binding.term4Checkbox.setOnCheckedChangeListener { _, _ -> listener(binding.term4Checkbox) }
    }

    //체크박스 상태에 따른 다음으로 버튼 상태 변경
    private fun updateNextButtonState() {
        val allChecked = binding.term1Checkbox.isChecked && binding.term2Checkbox.isChecked
        binding.nextBtn.isEnabled = allChecked

        // 배경색 변경
        binding.nextBtn.setBackgroundColor(
            if (allChecked)
                requireContext().getColor(R.color.black)
            else
                Color.parseColor("#F6F6F6")
        )

        // 글자색 변경
        binding.nextBtn.setTextColor(
            if (allChecked)
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.black)
        )
    }

    //전체 동의 체크박스 리스너
    private fun setupSelectAllCheckbox() {
        binding.allCheckbox.setOnCheckedChangeListener { checkBox: CompoundButton, isChecked ->
            setAllAgreementChecked(isChecked)
            setCheckBoxTint(checkBox, isChecked)
        }
    }

    //전체 동의 체크박스 활성화 검사
    private fun syncSelectAllCheckbox() {
        val allChecked = binding.term1Checkbox.isChecked &&
                binding.term2Checkbox.isChecked &&
                binding.term3Checkbox.isChecked &&
                binding.term4Checkbox.isChecked

        if (binding.allCheckbox.isChecked != allChecked) {
            binding.allCheckbox.setOnCheckedChangeListener(null)
            binding.allCheckbox.isChecked = allChecked
            setCheckBoxTint(binding.allCheckbox, allChecked)
            binding.allCheckbox.setOnCheckedChangeListener { _, isChecked ->
                setAllAgreementChecked(isChecked)
                setCheckBoxTint(binding.allCheckbox, isChecked)
            }
        }
    }

    //전체 동의 체크 시 모든 체크박스 활성화
    private fun setAllAgreementChecked(isChecked: Boolean) {
        binding.term1Checkbox.isChecked = isChecked
        binding.term2Checkbox.isChecked = isChecked
        binding.term3Checkbox.isChecked = isChecked
        binding.term4Checkbox.isChecked = isChecked
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val color = if (isChecked) {
            ContextCompat.getColor(requireContext(), R.color.black) // 선택 시 색상
        } else {
            ContextCompat.getColor(requireContext(), R.color.gray)
        }
        checkBox.buttonTintList = ColorStateList.valueOf(color)
    }

    //약관 상세 페이지 이동
    private fun openTermsDetail(termKey: String, title: String) {
        val fragment = TermsDetailFragment().apply {
            arguments = Bundle().apply {
                putString("term_key", termKey)
                putString("term_title", title)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}