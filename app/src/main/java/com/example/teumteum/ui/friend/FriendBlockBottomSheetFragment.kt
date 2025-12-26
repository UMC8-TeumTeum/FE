package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.databinding.BottomSheetFriendBlockBinding
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendBlockBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendBlockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFriendBlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt(ARG_USER_ID, -1) ?: -1
        val userName = arguments?.getString(ARG_USER_NAME).orEmpty()

        if (userId == -1) {
            Toast.makeText(requireContext(), "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        // ✅ 이름 동적 반영
        binding.tvTitle.text = "${userName}님을 차단하시겠어요?"

        binding.btnBlockConfirm.setOnClickListener {
            viewModel.blockUser(userId)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USER_NAME = "userName"

        fun newInstance(userId: Int, userName: String): FriendBlockBottomSheet {
            return FriendBlockBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_USER_ID, userId)
                    putString(ARG_USER_NAME, userName)
                }
            }
        }
    }
}
