package com.example.teumteum.ui.myhome

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.teumteum.R
import com.example.teumteum.data.remote.auth.LogoutUseCase
import com.example.teumteum.databinding.FragmentMyAccountSettingBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.google.android.material.button.MaterialButton
import com.navercorp.nid.NidOAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyAccountSettingFragment : Fragment() {

    private var _binding: FragmentMyAccountSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyHomeViewModel by activityViewModels()

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
            parentFragmentManager.popBackStack()
        }

        binding.logoutLl.setOnClickListener {
            if (loggingOut) return@setOnClickListener
            showLogoutDialog()
        }

        binding.deleteAccountLl.setOnClickListener {
            showDeleteAccountDialog()
        }

        viewModel.getMySocialInfo()
        observeAccountInfo()
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }

        val yesBtn = dialogView.findViewById<MaterialButton>(R.id.yes_btn)
        val noBtn = dialogView.findViewById<MaterialButton>(R.id.no_btn)

        yesBtn.setOnClickListener {
            dialog.dismiss()
            performLogout()

            //네이버 로그아웃
            NidOAuth.logout(object : com.navercorp.nid.oauth.util.NidOAuthCallback {
                override fun onSuccess() {
                }

                override fun onFailure(errorCode: String, errorDesc: String) {
                }
            })
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        val displayMetrics = resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }

        val yesBtn = dialogView.findViewById<MaterialButton>(R.id.yes_btn)
        val noBtn = dialogView.findViewById<MaterialButton>(R.id.no_btn)

        yesBtn.setOnClickListener {
            dialog.dismiss()
            //todo: 회원탈퇴 호출 로직
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        val displayMetrics = resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun performLogout() {
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

    private fun observeAccountInfo() {
        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.myAccountTv.text = email ?: "이메일"
        }

        viewModel.socialType.observe(viewLifecycleOwner) { socialType ->
            binding.socialTypeTv.text = socialType ?: "TeumTeum"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showBottomBar()
        _binding = null
    }
}
