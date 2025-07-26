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
import com.example.teumteum.data.entities.Wish
import com.example.teumteum.data.remote.wish.WishService
import com.example.teumteum.data.remote.wish.dto.EditWishRequest
import com.example.teumteum.databinding.DialogConfirmWishDeleteBinding
import com.example.teumteum.databinding.DialogConfirmWishEditBinding
import com.example.teumteum.databinding.FragmentWishEditBinding
import com.example.teumteum.ui.wish.view.EditWishView
import com.example.teumteum.ui.wish.view.WishView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class WishEditFragment : BottomSheetDialogFragment(), WishView, EditWishView {

    private lateinit var binding: FragmentWishEditBinding
    private var wishId: Long = -1L

    private var selectedTimeButton: View? = null
    private var selectedCategoryButtons = mutableListOf<MaterialButton>()

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

        wishId = arguments?.getLong("wish_id") ?: -1L

        binding.btnWishSave.setOnClickListener {
            val titleText = binding.wishTitleEt.text.toString().trim()
            val contentText = binding.detailTextEt.text.toString().trim()

            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedTime = selectedTimeButton?.tag as? String
            if (selectedTime == null) {
                Toast.makeText(requireContext(), "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedCategoryButtons.isEmpty()) {
                Toast.makeText(requireContext(), "카테고리를 하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategoryIds = selectedCategoryButtons.mapNotNull { it.tag as? Long }

            val request = EditWishRequest(
                title = titleText,
                content = contentText,
                estimatedDuration = selectedTime,
                categories = selectedCategoryIds
            )

            val wishService = WishService()
            wishService.setWishEditView(this)
            wishService.editWish(wishId, request)
        }

        binding.btnWishDelete.setOnClickListener {
            showWishDummyDeleteDialog()
        }

        if (wishId != -1L) {
            get(wishId)
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

        val timeTags = listOf("10m", "20m", "30m", "1h")
        timeButtons.forEachIndexed { index, button ->
            button.tag = timeTags[index] // tag 지정

            if (button.tag == currentTime) {
                // 선택 상태로 초기 세팅
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))
                selectedTimeButton = button
            } else {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                button.setTextColor(resources.getColor(R.color.text_primary, null))
            }

            button.setOnClickListener {
                (selectedTimeButton as? MaterialButton)?.apply {
                    backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                    setTextColor(resources.getColor(R.color.text_primary, null))
                }

                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))

                selectedTimeButton = button
            }
        }
    }

    private fun setupCategoryButtons(wish: Wish) {
        val categoryButtons = listOf(
            binding.btnWishCategory01,
            binding.btnWishCategory02,
            binding.btnWishCategory03,
            binding.btnWishCategory04,
            binding.btnWishCategory05,
            binding.btnWishCategory06
        )

        val categoryIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        categoryButtons.forEachIndexed { index, button ->
            button.tag = categoryIds[index]
        }

        val selectedCategoryIds = wish.categories.map { it.id }

        categoryButtons.forEach { button ->
            val isSelected = selectedCategoryIds.contains(button.tag as Long)

            if (isSelected) {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                button.setTextColor(resources.getColor(R.color.white, null))
                selectedCategoryButtons.add(button)
            } else {
                button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                button.setTextColor(resources.getColor(R.color.text_primary, null))
            }

            button.setOnClickListener {
                if (selectedCategoryButtons.contains(button)) {
                    // 선택 해제
                    button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_2, null))
                    button.setTextColor(resources.getColor(R.color.text_primary, null))
                    selectedCategoryButtons.remove(button)
                } else {
                    // 선택 추가
                    button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_1, null))
                    button.setTextColor(resources.getColor(R.color.white, null))
                    selectedCategoryButtons.add(button)
                }
            }
        }
    }

    companion object {
        fun newInstance(wishId: Long): WishEditFragment {
            return WishEditFragment().apply {
                arguments = Bundle().apply {
                    putLong("wish_id", wishId)
                }
            }
        }
    }

    private fun get(wishId: Long) {
        val wishService = WishService()
        wishService.setWishGetView(this)
        wishService.getWish(wishId)
    }

    override fun onGetWishSuccess(wish: Wish) {

        binding.wishTitleEt.setText(wish.title)
        binding.detailTextEt.setText(wish.content)
        setupTimeButtons(wish.estimatedDuration)
        setupCategoryButtons(wish)
    }

    override fun onGetWishFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "HOME4043" -> "해당 위시 정보를 찾을 수 없습니다."
            "COMMON500" -> "서버 오류입니다. 관리자에게 문의해주세요."
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> "위시 조회에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onEditWishSuccess(code: String, message: String?) {
        val successMessage = message ?: "위시 정보가 성공적으로 수정되었습니다."
        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()

        // 이벤트 전송
        parentFragmentManager.setFragmentResult("wish_edit", Bundle())

        // 모든 바텀시트 닫기
        (requireActivity().supportFragmentManager.fragments).forEach {
            if (it is BottomSheetDialogFragment) {
                it.dismissAllowingStateLoss()
            }
        }
    }

    override fun onEditWishFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "COMMON400" -> "제목 또는 카테고리가 비어있습니다."
            "HOME4042" -> "해당 카테고리를 찾을 수 없습니다."
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> message ?: "등록에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }
}