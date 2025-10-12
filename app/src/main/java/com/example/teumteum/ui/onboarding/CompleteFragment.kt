package com.example.teumteum.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentCompleteBinding
import com.example.teumteum.ui.auth.SignUpActivity
import com.example.teumteum.ui.onboarding.OnBoardingNicknameFragment
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.NextStep
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