package com.umc.teumteum.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentCompleteBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import com.umc.teumteum.utils.FlowPrefs
import com.umc.teumteum.utils.NextStep
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CompleteFragment : Fragment() {

    private var _binding: FragmentCompleteBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var flowPrefs: FlowPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프로그래스바 설정
        (activity as? SignUpActivity)?.setProgressBar(100)

        // step을 ONBORDING으로 설정
        flowPrefs.setLastStep(NextStep.ONBOARDING)

        val initialMarginBottom =
            (binding.completeBtn.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.completeBtn) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + bottomInset
            }
            insets
        }

        binding.completeBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OnBoardingNicknameFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}