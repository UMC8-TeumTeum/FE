package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentServiceInfoDetailBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceInfoDetailFragment : Fragment() {

    private lateinit var binding: FragmentServiceInfoDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServiceInfoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? SignUpActivity)?.setProgressBarVisible(false)

        val raw = getString(
            R.string.term1_content
        )

        val html = raw.replace("\n", "<br>")
        binding.contentTv.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.contentTv.movementMethod = LinkMovementMethod.getInstance()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}