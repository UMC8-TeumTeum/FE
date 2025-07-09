package com.example.teumteum.SignUp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentTermsDetailBinding

class TermsDetailFragment : Fragment() {

    private lateinit var binding: FragmentTermsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTermsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? SignUpActivity)?.setProgressBarVisible(false)

        val termKey = arguments?.getString("term_key")
        binding.contentTv.text = getTermContent(termKey)

        binding.titleTv.text = arguments?.getString("term_title")

        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AgreementFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getTermContent(termKey: String?): String{
        return when (termKey) {
            "term1" -> getString(R.string.term1_content)
            "term2" -> getString(R.string.term2_content)
            "term3" -> getString(R.string.term3_content)
            "term4" -> getString(R.string.term4_content)
            else -> "약관 내용을 불러올 수 없습니다."
        }
    }


}