package com.umc.teumteum.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentTermsDetailBinding
import com.umc.teumteum.ui.auth.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsDetailFragment : Fragment() {

    private lateinit var binding: FragmentTermsDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTermsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? SignUpActivity)?.setProgressBarVisible(false)

        val initialMarginBottom =
            (binding.nextBtn.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.nextBtn) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + bottomInset
            }
            insets
        }

        val termKey = arguments?.getString("term_key")
        binding.contentTv.text = getTermContent(termKey)

        binding.titleTv.text = arguments?.getString("term_title")

        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AgreementFragment())
                .addToBackStack(null)
                .commit()
        }

        val raw = getTermContent(termKey)

        val html = raw.replace("\n", "<br>")
        binding.contentTv.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.contentTv.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun getTermContent(termKey: String?): String{
        return when (termKey) {
            "term1" -> getString(R.string.term1_content)
            "term2" -> getString(
                R.string.term2_content,
                getString(R.string.privacy_policy_url)
            )
            "term3" -> getString(R.string.term3_content)
            "term4" -> getString(R.string.term4_content)
            else -> "약관 내용을 불러올 수 없습니다."
        }
    }
}