package com.example.teumteum.ui.wish

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.teumteum.R
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.databinding.DialogConfirmWishDeleteBinding
import com.example.teumteum.databinding.DialogConfirmWishEditBinding
import com.example.teumteum.databinding.FragmentWishEditBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class WishEditFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentWishEditBinding
    private var wishId: Int = -1

    private var selectedTimeButton: View? = null
    private var selectedCategoryButton: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wishId = arguments?.getInt("wish_id") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDummy = arguments?.getBoolean("is_dummy") ?: false
        if (isDummy) {
            val title = arguments?.getString("title") ?: ""
            val time = arguments?.getString("time") ?: ""
            val category = arguments?.getString("category") ?: ""

            binding.wishTitleEt.setText(title)
            setupTimeButtons(time)
            setupCategoryButtons(category)
        }

        binding.btnWishSave.setOnClickListener {
            val titleText = binding.wishTitleEt.text.toString().trim()

            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 더미 처리
            Toast.makeText(requireContext(), "수정되었습니다. (더미)", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.btnWishDelete.setOnClickListener {
            showWishDummyDeleteDialog()
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

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                showWishCancelEditDialog()
                true
            } else {
                false
            }
        }

        return dialog
    }

    companion object {
        fun newInstanceWithWishDummy(item: WishItem): WishEditFragment {
            return WishEditFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("is_dummy", true)
                    putInt("wish_id", item.id)
                    putString("title", item.title)
                    putString("time", item.time)
                    putString("category", item.category)
                }
            }
        }
    }

    private fun showWishDummyDeleteDialog() {
        val dialogBinding = DialogConfirmWishDeleteBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.wishConfirmTv.setOnClickListener {
            Toast.makeText(requireContext(), "삭제되었습니다. (더미)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            dismiss()
        }

        dialogBinding.wishCancelTv.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            dialog.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.y = (resources.displayMetrics.heightPixels * 0.37).toInt()
                layoutParams.dimAmount = 0.5f
                window.attributes = layoutParams

                window.setDimAmount(0.5f)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }

        dialog.show()
    }

    private fun showWishCancelEditDialog() {
        val dialogBinding = DialogConfirmWishEditBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.wishConfirmTv.setOnClickListener {
            dialog.dismiss()
            dismiss()
        }

        dialogBinding.wishCancelTv.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            dialog.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                layoutParams.y = (resources.displayMetrics.heightPixels * 0.37).toInt()
                layoutParams.dimAmount = 0.5f
                window.attributes = layoutParams

                window.setDimAmount(0.5f)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }

        dialog.show()
    }

    private fun setupTimeButtons(currentTime: String) {
        val timeButtons = listOf(
            binding.btnWishTime01,
            binding.btnWishTime02,
            binding.btnWishTime03,
            binding.btnWishTime04
        )

        for (button in timeButtons) {
            if (button.text == currentTime) {
                // 선택 상태로 초기 세팅
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))
                selectedTimeButton = button
            } else {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                button.setTextColor(resources.getColor(R.color.black, null))
            }

            button.setOnClickListener {
                (selectedTimeButton as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                    setTextColor(resources.getColor(R.color.black, null))
                }

                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))

                selectedTimeButton = button
            }
        }
    }

    private fun setupCategoryButtons(currentCategory: String) {
        val categoryButtons = listOf(
            binding.btnWishCategory01,
            binding.btnWishCategory02,
            binding.btnWishCategory03,
            binding.btnWishCategory04,
            binding.btnWishCategory05,
            binding.btnWishCategory06
        )

        for (button in categoryButtons) {
            if (button.text == currentCategory) {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))
                selectedCategoryButton = button
            } else {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                button.setTextColor(resources.getColor(R.color.black, null))
            }

            button.setOnClickListener {
                (selectedCategoryButton as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                    setTextColor(resources.getColor(R.color.black, null))
                }

                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))

                selectedCategoryButton = button
            }
        }
    }
}