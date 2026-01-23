package com.umc.teumteum.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.ActivitySignUpBinding
import com.umc.teumteum.ui.onboarding.OnBoardingNicknameFragment
import com.umc.teumteum.ui.onboarding.OnBoardingProfileFragment
import com.umc.teumteum.ui.onboarding.OnBoardingRemindFragment
import com.umc.teumteum.ui.onboarding.OnBoardingScheduleFragment
import com.umc.teumteum.ui.onboarding.OnBoardingSleepPatternFragment
import com.umc.teumteum.ui.onboarding.AgreementFragment
import com.umc.teumteum.ui.main.MainActivity
import com.umc.teumteum.utils.FlowPrefs
import com.umc.teumteum.utils.NextStep
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NEXT_STEP = "extra_next_step"
    }

    private lateinit var binding: ActivitySignUpBinding

    @Inject
    lateinit var flowPrefs: FlowPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeSignUpFlow()
    }

    private fun initializeSignUpFlow() {
        val stepFromIntent = intent.getStringExtra(EXTRA_NEXT_STEP)
            ?.let { runCatching { NextStep.valueOf(it) }.getOrNull() }

        // 1순위: 인텐트, 2순위: 로컬 캐시
        val currentStep = stepFromIntent ?: flowPrefs.getLastStep()

        when (currentStep) {
            NextStep.ONBOARDING -> {
                setProgressBarVisible(true)
                setProgressBar(20)
                loadFragment(OnBoardingNicknameFragment())
            }
            NextStep.MAIN -> navigateToMain()
            NextStep.AGREEMENT, null -> {
                setProgressBarVisible(false)
                loadFragment(AgreementFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // 상단 프로그레스바 제어
    fun setProgressBar(progress: Int) {
        binding.progressBar.progress = progress
    }

    // 프로그레스바 visible 설정
    fun setProgressBarVisible(visible: Boolean) {
        binding.progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // 온보딩 단계
    fun proceedToNextOnboardingStep(currentFragment: Fragment) {
        // 온보딩 도중에는 항상 ONBOARDING 유지
        flowPrefs.setLastStep(NextStep.ONBOARDING)

        when (currentFragment) {
            is OnBoardingNicknameFragment -> {
                setProgressBar(40)
                loadFragment(OnBoardingProfileFragment())
            }
            is OnBoardingProfileFragment -> {
                setProgressBar(60)
                loadFragment(OnBoardingSleepPatternFragment())
            }
            is OnBoardingSleepPatternFragment -> {
                setProgressBar(80)
                loadFragment(OnBoardingScheduleFragment())
            }
            is OnBoardingScheduleFragment -> {
                setProgressBar(100)
                loadFragment(OnBoardingRemindFragment())
            }
            is OnBoardingRemindFragment -> {
                completeOnboarding()
            }
        }
    }

    // 온보딩 완료 후 메인 화면으로 이동
    fun completeOnboarding() {
        flowPrefs.setLastStep(NextStep.MAIN)
        navigateToMain()
    }

    // 메인 화면으로 이동
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
