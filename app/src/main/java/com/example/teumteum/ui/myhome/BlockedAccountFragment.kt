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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.DialogUnblockBinding
import com.example.teumteum.databinding.FragmentBlockedAccountBinding
import com.example.teumteum.ui.myhome.adapter.BlockedAccountAdapter
import com.example.teumteum.ui.myhome.data.BlockedAccount
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedAccountFragment : Fragment() {

    private var _binding: FragmentBlockedAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터
        //Todo: 차단된 계정 API
        val dummyList = listOf(
            BlockedAccount("미나리", "UX 디자이너", R.drawable.ic_empty_profile_sv),
            BlockedAccount("미나리나물", "개발자", R.drawable.ic_empty_profile_sv),
            BlockedAccount("미나리무침", "학원 강사", R.drawable.ic_empty_profile_sv),
            BlockedAccount("미미미", "매니저", R.drawable.ic_empty_profile_sv)
        )

        val adapter = BlockedAccountAdapter(dummyList) { account ->
            showUnblockDialog(account)
        }

        binding.blockedAccountRv.adapter = adapter
        binding.blockedAccountRv.layoutManager = LinearLayoutManager(requireContext())

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MySettingFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUnblockDialog(account: BlockedAccount) {
        val dialogView = DialogUnblockBinding.inflate(layoutInflater)

        dialogView.descriptionTv.text = "${account.name} 님을\n차단 해제하시겠어요?"

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }

        dialogView.yesBtn.setOnClickListener {
            Toast.makeText(requireContext(), "${account.name} 차단 해제 완료", Toast.LENGTH_SHORT).show()

            // TODO: 차단 해제 API 연동
            dialog.dismiss()
        }

        dialogView.noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        val displayMetrics = resources.displayMetrics
        val dialogWidth = (displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}
