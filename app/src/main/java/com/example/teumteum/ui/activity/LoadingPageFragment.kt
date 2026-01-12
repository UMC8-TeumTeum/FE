package com.example.teumteum.ui.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentLoadingPageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingPageFragment : Fragment() {

    companion object {
        const val TAG = "LoadingPageFragment"
        fun newInstance() = LoadingPageFragment()
    }

    private var _binding: FragmentLoadingPageBinding? = null
    private val binding get() = _binding!!

    private val progress: ProgressBar get() = binding.linearProgress
    private var animator: ValueAnimator? = null
    private var current = 0 // 진행률 캐시(선택)

    private var backCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기화
        progress.isIndeterminate = false
        progress.progress = 0
        current = 0

        animateProgress(to = 90)

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dismissNow() // 로딩 화면 제거
                requireActivity().supportFragmentManager.popBackStack()
            }
        }.also {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }
    }

    private fun animateProgress(to: Int, onEnd: (() -> Unit)? = null) {
        val start = progress.progress
        val end = to.coerceIn(0, 100)

        animator?.cancel()
        animator = null
        progress.clearAnimation()
        progress.jumpDrawablesToCurrentState()

        animator = ValueAnimator.ofInt(start, end).apply {
            duration = 1500L
            addUpdateListener { a ->
                val v = a.animatedValue as Int
                progress.progress = v
                current = v
            }
            doOnEnd { onEnd?.invoke() }
            start()
        }
    }

    // 외부에서 완료 신호가 오면 100%까지 채우고 스스로 닫힘
    fun completeAndDismiss() {
        animateProgress(to = 100) {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .remove(this)
                .commitAllowingStateLoss()
        }
    }

    fun dismissNow() {
        animator?.cancel()
        animator = null
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animator?.cancel()
        animator = null
        backCallback = null
        _binding = null
    }
}
