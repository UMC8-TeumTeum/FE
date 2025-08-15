package com.example.teumteum.ui.myhome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMyAccountSettingBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.signin.LoginActivity
import com.example.teumteum.utils.FlowPrefs
import com.example.teumteum.utils.TokenProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyAccountSettingFragment : Fragment() {

    private var _binding: FragmentMyAccountSettingBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var tokenProvider: TokenProvider
    @Inject lateinit var flowPrefs: FlowPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MySettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.logoutLl.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        // 토큰과 플로우 상태 초기화
        tokenProvider.clearTokens()
        flowPrefs.clear()

        // 로그인 화면으로 이동 (백스택 클리어)
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showBottomBar()
        _binding = null
    }
}
