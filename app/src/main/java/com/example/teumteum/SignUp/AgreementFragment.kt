package com.example.teumteum.SignUp

import android.content.res.ColorStateList
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
    }

    //체크박스 리스너
    private fun setupCheckBoxListeners() {
        val listener = { checkBox: CompoundButton ->
            updateNextButtonState()
            syncSelectAllCheckbox()
            setCheckBoxTint(checkBox, checkBox.isChecked)
        }

        binding.check1.setOnCheckedChangeListener { _, _ -> listener(binding.check1) }
        binding.check2.setOnCheckedChangeListener { _, _ -> listener(binding.check2) }
        binding.check3.setOnCheckedChangeListener { _, _ -> listener(binding.check3) }
        binding.check4.setOnCheckedChangeListener { _, _ -> listener(binding.check4) }
    }

    //체크박스 상태에 따른 다음으로 버튼 상태 변경
    private fun updateNextButtonState() {
        val allChecked = binding.check1.isChecked && binding.check2.isChecked
        binding.nextBtn.isEnabled = allChecked

        // 배경색 변경
        binding.nextBtn.setBackgroundColor(
            if (allChecked)
                requireContext().getColor(R.color.black)
            else
                android.graphics.Color.parseColor("#F6F6F6")
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
        binding.check5.setOnCheckedChangeListener { checkBox: CompoundButton, isChecked ->
            setAllAgreementChecked(isChecked)
            setCheckBoxTint(checkBox, isChecked)
        }
    }

    //전체 동의 체크박스 활성화 검사
    private fun syncSelectAllCheckbox() {
        val allChecked = binding.check1.isChecked &&
                binding.check2.isChecked &&
                binding.check3.isChecked &&
                binding.check4.isChecked

        if (binding.check5.isChecked != allChecked) {
            binding.check5.setOnCheckedChangeListener(null)
            binding.check5.isChecked = allChecked
            binding.check5.setOnCheckedChangeListener { _, isChecked ->
                setAllAgreementChecked(isChecked)
            }
        }
    }

    //전체 동의 체크 시 모든 체크박스 활성화
    private fun setAllAgreementChecked(isChecked: Boolean) {
        binding.check1.isChecked = isChecked
        binding.check2.isChecked = isChecked
        binding.check3.isChecked = isChecked
        binding.check4.isChecked = isChecked
    }

    private fun setCheckBoxTint(checkBox: CompoundButton, isChecked: Boolean) {
        val color = if (isChecked) {
            ContextCompat.getColor(requireContext(), R.color.black) // 선택 시 색상
        } else {
            ContextCompat.getColor(requireContext(), R.color.gray)
        }
        checkBox.buttonTintList = ColorStateList.valueOf(color)
    }
}