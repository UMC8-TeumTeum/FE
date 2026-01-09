package com.example.teumteum.ui.wish

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.EditWishRequest
import com.example.teumteum.data.remote.wish.model.WishResult
import com.example.teumteum.databinding.BottomSheetWishEditBinding
import com.example.teumteum.databinding.DialogConfirmWishDeleteBinding
import com.example.teumteum.databinding.DialogConfirmWishEditBinding
import com.example.teumteum.ui.wish.viewModel.WishViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.max

@AndroidEntryPoint
class BottomSheetWishEditFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWishEditBinding? = null
    private val binding get() = _binding!!

    private var wishId: Long = -1L

    private var selectedTimeButton: View? = null
    private var selectedCategoryButtons = mutableListOf<MaterialButton>()

    private var originalTitle: String = ""
    private var originalContent: String = ""
    private var originalTime: String = ""
    private var originalCategoryIds: List<Long> = emptyList()

    private val viewModel: WishViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWishEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        wishId = arguments?.getLong("wish_id") ?: -1L
        if (wishId != -1L) {
            viewModel.getWish(wishId)
        }

        // 원래 스크롤뷰 패딩 저장
        val originalBottomPadding = binding.editScroll.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

            // 추가 확보 공간
            val extra = max(0, imeBottom - sysBottom)

            binding.editScroll.updatePadding(
                bottom = originalBottomPadding + extra
            )

            // 키보드 올라오면 하단 버튼 숨김
            binding.wishBottomBar.isVisible = imeBottom == 0

            insets
        }

        binding.btnWishSave.setOnClickListener {
            val titleText = binding.wishTitleEt.text.toString().trim()
            val contentText = binding.detailTextEt.text.toString().trim()
            val selectedTime = selectedTimeButton?.tag as? String
            val selectedCategoryIds = selectedCategoryButtons.mapNotNull { it.tag as? Long }

            if (titleText.isEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTime == null) {
                Toast.makeText(requireContext(), "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedCategoryButtons.isEmpty()) {
                Toast.makeText(requireContext(), "카테고리를 하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = EditWishRequest(
                title = titleText,
                content = contentText,
                estimatedDuration = selectedTime,
                categories = selectedCategoryIds
            )

            viewModel.editWish(wishId, request)
        }

        binding.btnWishDelete.setOnClickListener {
            val deleteRequest = DeleteWishesRequest(listOf(wishId))
            showWishDeleteDialog(deleteRequest)
        }

    }

    private fun setupObservers() {
        viewModel.wish.observe(viewLifecycleOwner) { wish ->
            if (wish == null) return@observe

            binding.wishTitleEt.setText(wish.title)
            binding.detailTextEt.setText(wish.content)
            setupTimeButtons(wish.estimatedDuration)
            setupCategoryButtons(wish)

            // 선택 여부 확인용 원본 저장
            originalTitle = wish.title
            originalContent = wish.content
            originalTime = wish.estimatedDuration
            originalCategoryIds = wish.categories.map { it.categoryId }.sorted()
        }

        // 수정 성공 시
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editSuccess.collect {
                    Log.d("WISH_EDIT_FRAGMENT", "위시가 성공적으로 수정되었습니다.")
                    parentFragmentManager.setFragmentResult("wish_edit", Bundle())

                    // 모든 바텀시트 닫기
                    (requireActivity().supportFragmentManager.fragments).forEach { fragment ->
                        if (fragment is BottomSheetDialogFragment) {
                            fragment.dismissAllowingStateLoss()
                        }
                    }
                }
            }
        }

        // 삭제 성공 시
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteSuccess.collect {
                    Log.d("WISH_EDIT_FRAGMENT", "위시가 성공적으로 삭제되었습니다.")
                    parentFragmentManager.setFragmentResult("wish_delete", Bundle())

                    // 모든 바텀시트 닫기
                    (requireActivity().supportFragmentManager.fragments).forEach { fragment ->
                        if (fragment is BottomSheetDialogFragment) {
                            fragment.dismissAllowingStateLoss()
                        }
                    }
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
//                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e("WishError", it)
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
                if (isModified()) {
                    showWishCancelEditDialog()
                } else {
                    dismiss() // 수정 없으면 바로 닫기
                }
                true
            } else {
                false
            }
        }

        return dialog
    }

    private fun showWishDeleteDialog(request: DeleteWishesRequest) {
        val dialogBinding = DialogConfirmWishDeleteBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.wishConfirmTv.setOnClickListener {
            viewModel.deleteWishes(request)
            dialog.dismiss()
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
        selectedTimeButton = null // 재바인딩 시 초기화
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

    private fun setupCategoryButtons(wish: WishResult) {
        selectedCategoryButtons.clear() // 누적 방지

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

        val selectedCategoryIds = wish.categories.map { it.categoryId }

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
        fun newInstance(wishId: Long): BottomSheetWishEditFragment {
            return BottomSheetWishEditFragment().apply {
                arguments = Bundle().apply {
                    putLong("wish_id", wishId)
                }
            }
        }
    }

    private fun isModified(): Boolean {
        val currentTitle = binding.wishTitleEt.text.toString().trim()
        val currentContent = binding.detailTextEt.text.toString().trim()
        val currentTime = selectedTimeButton?.tag as? String ?: ""
        val currentCategoryIds = selectedCategoryButtons.mapNotNull { it.tag as? Long }.sorted()

        return currentTitle != originalTitle ||
                currentContent != originalContent ||
                currentTime != originalTime ||
                currentCategoryIds != originalCategoryIds
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}