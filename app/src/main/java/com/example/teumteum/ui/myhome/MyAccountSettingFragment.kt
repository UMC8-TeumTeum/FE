package com.example.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentMyAccountSettingBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.data.remote.login.LogoutUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyAccountSettingFragment : Fragment() {

    private var _binding: FragmentMyAccountSettingBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var logoutUseCase: LogoutUseCase

    private var loggingOut = false

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
            if (loggingOut) return@setOnClickListener
            loggingOut = true
            binding.logoutLl.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    logoutUseCase.deactivateFcmAndLogout()
                } catch (_: Exception) {
                    Toast.makeText(requireContext(), "로그아웃 중 문제가 발생했어요.", Toast.LENGTH_SHORT).show()
                } finally {
                    loggingOut = false
                    if (isAdded) binding.logoutLl.isEnabled = true
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showBottomBar()
        _binding = null
    }
}
