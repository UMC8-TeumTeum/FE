package com.umc.teumteum.ui.myhome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentServiceInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class ServiceInfoFragment : Fragment() {

    private var _binding: FragmentServiceInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.termsLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, ServiceInfoDetailFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.privacyPolicyLl.setOnClickListener {
            val url = getString(R.string.privacy_policy_url)
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}