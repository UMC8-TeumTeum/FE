package com.example.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.friend.model.TodoConflictItem
import com.example.teumteum.databinding.BottomSheetFriendTodoBinding
import com.example.teumteum.ui.friend.adapter.TodoEventAdapter
import com.example.teumteum.ui.friend.data.TeumEvent
import com.example.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendTodoBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodoEventAdapter
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
        _binding = BottomSheetFriendTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val conflictList: ArrayList<TodoConflictItem> =
            arguments?.getParcelableArrayList(ARG_CONFLICT_LIST) ?: arrayListOf()

        val events: List<TeumEvent> = conflictList.map {
            TeumEvent(it.startTime, it.endTime, it.title)
        }

        adapter = TodoEventAdapter(events)
        binding.existingTodoViewPager.adapter = adapter
        binding.dotsIndicator.setViewPager2(binding.existingTodoViewPager)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnAccept.setOnClickListener {
            val responseId = arguments?.getInt(ARG_RESPONSE_ID) ?: return@setOnClickListener

            viewModel.respondToTeum(responseId, "accepted")

            dismiss()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendSendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RESPONSE_ID = "responseId"
        private const val ARG_CONFLICT_LIST = "conflictList"

        fun newInstance(
            responseId: Int,
            conflictList: ArrayList<TodoConflictItem>
        ): FriendTodoBottomSheetFragment {
            return FriendTodoBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RESPONSE_ID, responseId)
                    putParcelableArrayList(ARG_CONFLICT_LIST, conflictList)
                }
            }
        }
    }
}
