package com.example.teumteum.ui.myhome

import android.app.Dialog
import android.content.Intent
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
import com.example.teumteum.ui.auth.LoginActivity
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel.DeleteUserState
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
    private var deleting = false

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

        observeDeleteUserState()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.logoutLl.setOnClickListener {
            if (loggingOut) return@setOnClickListener
            showLogoutDialog()
        }

        binding.deleteAccountLl.setOnClickListener {
            if (deleting) return@setOnClickListener
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

        yesBtn.isEnabled = !deleting
        noBtn.isEnabled = !deleting

        yesBtn.setOnClickListener {
            if (deleting) return@setOnClickListener
            dialog.dismiss()
            viewModel.deleteUser()
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

    private fun observeDeleteUserState() {
        viewModel.deleteUserState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DeleteUserState.Loading -> {
                    deleting = true
                    binding.deleteAccountLl.isEnabled = false
                }

                is DeleteUserState.Success -> {
                    deleting = false
                    binding.deleteAccountLl.isEnabled = true

                    Toast.makeText(requireContext(), "회원탈퇴가 완료되었어요.", Toast.LENGTH_SHORT).show()

                    cleanupAllSocialAndNavigate()

                    viewModel.resetDeleteUserState()
                }

                is DeleteUserState.Error -> {
                    deleting = false
                    binding.deleteAccountLl.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        state.message.ifBlank { "회원탈퇴 중 문제가 발생했어요." },
                        Toast.LENGTH_SHORT
                    ).show()

                    viewModel.resetDeleteUserState()
                }

                DeleteUserState.Idle -> Unit
            }
        }
    }

    private fun navigateToLoginAndFinish() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showBottomBar()
        _binding = null
    }

    private fun cleanupAllSocialAndNavigate() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { logoutUseCase.deactivateFcmAndLogout() }

            unlinkSocialProviderByType()

            navigateToLoginAndFinish()
        }
    }


    private fun unlinkSocialProviderByType() {
        when (viewModel.socialType.value?.trim()?.uppercase()) {
            "NAVER" -> {
                runCatching {
                    NidOAuth.disconnect(object : com.navercorp.nid.oauth.util.NidOAuthCallback {
                        override fun onSuccess() {}
                        override fun onFailure(errorCode: String, errorDesc: String) {}
                    })
                }
            }

            "KAKAO" -> {
                runCatching {
                    val client = com.kakao.sdk.user.UserApiClient.instance
                    client.unlink { error ->
                        if (error != null) {
                            client.logout {}
                        }
                    }
                }
            }

            "GOOGLE" -> {
                runCatching {
                    val gso =
                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                        )
                            .requestEmail()
                            .build()

                    val client = com.google.android.gms.auth.api.signin.GoogleSignIn
                        .getClient(requireContext(), gso)

                    client.revokeAccess().addOnCompleteListener {
                        client.signOut()
                    }
                }
            }

            else -> {
            }
        }
    }
}
