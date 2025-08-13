package com.example.teumteum.ui.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import com.example.teumteum.databinding.FragmentLoadingPageBinding
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingPageFragment : Fragment() {

    companion object {
        const val TAG = "LoadingPageFragment"
        fun newInstance() = LoadingPageFragment()
    }

    private var _binding: FragmentLoadingPageBinding? = null
    private val binding get() = _binding!!

    private val progress: LinearProgressIndicator get() = binding.linearProgress
    private var animator: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        animateProgress(to = 90)
    }

    private fun animateProgress(to: Int, onEnd: (() -> Unit)? = null) {
        val start = progress.progress
        val end = to.coerceIn(0, 100)

        // 중복 애니메이터 정리 (겹침 방지)
        animator?.cancel()
        animator = null
        progress.clearAnimation()
        progress.jumpDrawablesToCurrentState()

        ValueAnimator.ofInt(start, end).apply {
            duration = 1500L
            addUpdateListener { animator ->
                progress.setProgressCompat(animator.animatedValue as Int, true)
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

    override fun onDestroyView() {
        animator?.cancel()
        animator = null
        _binding = null
        super.onDestroyView()
    }
}
