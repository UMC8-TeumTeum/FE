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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.DialogUnblockBinding
import com.example.teumteum.databinding.FragmentBlockedAccountBinding
import com.example.teumteum.ui.myhome.adapter.BlockedAccountAdapter
import com.example.teumteum.ui.myhome.data.BlockedAccount
import com.example.teumteum.ui.myhome.viewModel.BlockedAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedAccountFragment : Fragment() {

    private var _binding: FragmentBlockedAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BlockedAccountViewModel by viewModels()
    private lateinit var adapter: BlockedAccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeBlockedAccounts()
        observeError()

        //  차단된 계정 목록 조회
        viewModel.getBlockedAccounts()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = BlockedAccountAdapter { account ->
            showUnblockDialog(account)
        }

        binding.blockedAccountRv.layoutManager = LinearLayoutManager(requireContext())
        binding.blockedAccountRv.adapter = adapter
    }

    private fun observeBlockedAccounts() {
        viewModel.blockedAccountList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    private fun observeError() {
        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUnblockDialog(account: BlockedAccount) {
        val dialogView = DialogUnblockBinding.inflate(layoutInflater)

        dialogView.descriptionTv.text =
            "${account.nickName} 님을\n차단 해제하시겠어요?"

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }

        dialogView.yesBtn.setOnClickListener {
            // 차단 해제 API 호출
            viewModel.unblockUser(account.userId)

            dialog.dismiss()
        }

        dialogView.noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val dialogWidth = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
