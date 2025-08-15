package com.example.teumteum.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.ActivitySignUpBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.NextStep
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

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
        val currentStep = flowPrefs.getLastStep()

        when (currentStep) {
            NextStep.AGREEMENT -> {
                // 약관 동의 화면부터 시작
                setProgressBarVisible(true)
                setProgressBar(50)
                loadFragment(CompleteFragment())
            }
            NextStep.ONBOARDING -> {
                // 온보딩 첫 번째 단계부터 시작
                setProgressBarVisible(true)
                setProgressBar(20)
                loadFragment(OnBoardingNicknameFragment())
            }
            NextStep.MAIN -> {
                // 이미 모든 과정이 완료된 경우 - 메인으로 이동
                navigateToMain()
            }
            null -> {
                // NextStep이 설정되지 않은 경우 - 약관 동의부터 시작
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

    //상단 프로그레스바 제어
    fun setProgressBar(progress: Int) {
        binding.progressBar.progress = progress
    }

    //프로그레스바 visible 설정
    fun setProgressBarVisible(visible: Boolean) {
        binding.progressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * 온보딩 단계 진행
     */
    fun proceedToNextOnboardingStep(currentFragment: Fragment) {
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

    /**
     * 온보딩 완료 후 메인 화면으로 이동
     */
    fun completeOnboarding() {
        flowPrefs.setLastStep(NextStep.MAIN)
        navigateToMain()
    }

    /**
     * 메인 화면으로 이동
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}