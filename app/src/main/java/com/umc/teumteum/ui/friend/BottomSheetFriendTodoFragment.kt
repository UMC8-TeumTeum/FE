package com.umc.teumteum.ui.friend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TodoConflictItem
import com.umc.teumteum.databinding.BottomSheetFriendTodoBinding
import com.umc.teumteum.ui.friend.adapter.TodoEventAdapter
import com.umc.teumteum.ui.friend.data.TeumEvent
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetFriendTodoFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFriendTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodoEventAdapter
    private val viewModel: FriendViewModel by activityViewModels()

    private lateinit var indicatorLayout: LinearLayout


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

        indicatorLayout = binding.root.findViewById(R.id.clock_indicator_ll)

        val conflictList: ArrayList<TodoConflictItem> =
            arguments?.getParcelableArrayList(ARG_CONFLICT_LIST) ?: arrayListOf()

        val events: List<TeumEvent> = conflictList.map {
            TeumEvent(it.startTime, it.endTime, it.title)
        }

        adapter = TodoEventAdapter(events)
        binding.existingTodoViewPager.adapter = adapter

        if (events.isNotEmpty()) {
            setupIndicator(events.size)
            updateIndicator(0)
        } else {
            indicatorLayout.removeAllViews()
        }

        binding.existingTodoViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicator(position)
                }
            }
        )

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnAccept.setOnClickListener {
            val responseId = arguments?.getInt(ARG_RESPONSE_ID) ?: return@setOnClickListener

            binding.btnAccept.isEnabled = false
            viewModel.respondToTeum(responseId, "accepted")

            dismiss()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, FriendSendFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupIndicator(count: Int) {
        indicatorLayout.removeAllViews()

        repeat(count) {
            val indicator = View(requireContext())
            val params = LinearLayout.LayoutParams(
                dpToPx(4),
                dpToPx(4)
            ).apply {
                marginEnd = dpToPx(6)
            }

            indicator.layoutParams = params
            indicator.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.clock_indicator_dot)

            indicatorLayout.addView(indicator)
        }
    }

    private fun updateIndicator(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val v = indicatorLayout.getChildAt(i)
            val params = v.layoutParams as LinearLayout.LayoutParams

            if (i == position) {
                params.width = dpToPx(28)
                params.height = dpToPx(4)
                v.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.clock_indicator_bar_gray
                )
            } else {
                params.width = dpToPx(4)
                params.height = dpToPx(4)
                v.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.clock_indicator_dot
                )
            }
            v.layoutParams = params
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

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
        ): BottomSheetFriendTodoFragment {
            return BottomSheetFriendTodoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RESPONSE_ID, responseId)
                    putParcelableArrayList(ARG_CONFLICT_LIST, conflictList)
                }
            }
        }
    }
}
