package com.example.teumteum.ui.wish

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentWishRegisterBinding
import com.example.teumteum.ui.todo.TodoRegisterFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class WishRegisterFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentWishRegisterBinding
    private var selectedTimeButton: View? = null
    private var selectedCategoryButton: View? = null

    private var isWishSelected = true
    private var isFromWish: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isFromWish = arguments?.getBoolean("isFromWish") ?: false

        if (isFromWish) {
            binding.btnTodo.visibility = View.GONE
        } else {
            binding.btnTodo.visibility = View.VISIBLE
        }

        binding.btnTodo.setOnClickListener {
            if (isWishSelected) {
                binding.btnWish.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teumteum_bg))
                binding.btnWish.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))

                binding.btnTodo.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                binding.btnTodo.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                isWishSelected = false

                childFragmentManager.beginTransaction()
                    .replace(R.id.register_fragment_container, TodoRegisterFragment())
                    .commit()
            }
        }

        setupTimeButtons()
        setupCategoryButtons()
    }

    private fun setupTimeButtons() {
        val timeButtons = listOf(
            binding.btnWishTime01,
            binding.btnWishTime02,
            binding.btnWishTime03,
            binding.btnWishTime04
        )

        for (button in timeButtons) {
            button.setOnClickListener {
                (selectedTimeButton as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.main_2, null)
                    )
                    setTextColor(resources.getColor(R.color.text_primary, null))
                }

                (button as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.main_1, null)
                    )
                    setTextColor(resources.getColor(R.color.white, null))
                }

                selectedTimeButton = button
            }
        }
    }

    private fun setupCategoryButtons() {
        val categoryButtons = listOf(
            binding.btnWishCategory01,
            binding.btnWishCategory02,
            binding.btnWishCategory03,
            binding.btnWishCategory04,
            binding.btnWishCategory05,
            binding.btnWishCategory06
        )

        for (button in categoryButtons) {
            button.setOnClickListener {
                (selectedCategoryButton as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.main_2, null)
                    )
                    setTextColor(resources.getColor(R.color.text_primary, null))
                }

                (button as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.main_1, null)
                    )
                    setTextColor(resources.getColor(R.color.white, null))
                }

                selectedCategoryButton = button
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