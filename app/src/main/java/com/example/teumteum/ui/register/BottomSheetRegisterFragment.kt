package com.example.teumteum.ui.register

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentBottomSheetRegisterBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetRegisterFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentBottomSheetRegisterBinding
    private var isTodoSelected = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(R.id.register_fragment_container, TodoRegisterFragment())
            .commit()

        binding.btnTodo.setOnClickListener {
            if (!isTodoSelected) {
                binding.btnTodo.setImageResource(R.drawable.btn_todo_active_sv)
                binding.btnWish.setImageResource(R.drawable.btn_wish_deactive_sv)
                isTodoSelected = true

                childFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, TodoRegisterFragment())
                    .commit()
            }
        }

        binding.btnWish.setOnClickListener {
            if (isTodoSelected) {
                binding.btnTodo.setImageResource(R.drawable.btn_todo_deactive_sv)
                binding.btnWish.setImageResource(R.drawable.btn_wish_active_sv)
                isTodoSelected = false

                childFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, WishRegisterFragment())
                    .commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val screenHeight = resources.displayMetrics.heightPixels
                val desiredHeight = (screenHeight * 0.84).toInt()

                it.layoutParams.height = desiredHeight
                it.requestLayout()

                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = desiredHeight
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.isDraggable = false // 확장 불가능
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(R.drawable.calendar_background)
        }

        return dialog
    }
}