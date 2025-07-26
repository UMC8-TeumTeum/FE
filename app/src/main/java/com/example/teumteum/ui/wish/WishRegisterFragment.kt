package com.example.teumteum.ui.wish

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.dto.RegisterWishRequest
import com.example.teumteum.data.remote.wish.WishService
import com.example.teumteum.databinding.FragmentWishRegisterBinding
import com.example.teumteum.ui.todo.TodoRegisterFragment
import com.example.teumteum.ui.wish.view.RegisterWishView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class WishRegisterFragment : BottomSheetDialogFragment(), RegisterWishView {

    private lateinit var binding: FragmentWishRegisterBinding
    private var selectedTimeButton: View? = null
    private val selectedCategoryButtons = mutableListOf<MaterialButton>()

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

        binding.btnWishRegister.setOnClickListener {
            register()
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

        val timeTags = listOf("10m", "20m", "30m", "1h")
        timeButtons.forEachIndexed { index, button ->
            button.tag = timeTags[index]  // tag 지정

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

        val categoryIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        categoryButtons.forEachIndexed { index, button ->
            button.tag = categoryIds[index]

            button.setOnClickListener {

                if (selectedCategoryButtons.contains(button)) {
                    // 이미 선택된 경우 → 선택 해제
                    selectedCategoryButtons.remove(button)
                    button.backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.main_2, null)
                    )
                    button.setTextColor(resources.getColor(R.color.text_primary, null))
                } else {
                    // 선택 안 된 경우 → 추가
                    selectedCategoryButtons.add(button)
                    button.backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.main_1, null)
                    )
                    button.setTextColor(resources.getColor(R.color.white, null))
                }
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

    private fun getWishRequest(): RegisterWishRequest {
        val title = binding.wishTitleEt.text.toString()
        val content = binding.detailTextEt.text.toString()
        val estimatedDuration = (selectedTimeButton as MaterialButton).tag.toString()
        val categories = selectedCategoryButtons.mapNotNull { it.tag as? Long }

        return RegisterWishRequest(
            title = title,
            content = content,
            estimatedDuration = estimatedDuration,
            categories = categories
        )
    }

    private fun register() {
        if (binding.wishTitleEt.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTimeButton == null) {
            Toast.makeText(requireContext(), "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategoryButtons.isEmpty()) {
            Toast.makeText(requireContext(), "카테고리를 1개 이상 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = getWishRequest()

        val wishService = WishService()
        wishService.setWishRegisterView(this)
        wishService.registerWish(request)
    }

    override fun onRegisterWishSuccess(code: String, message: String?) {
        val successMessage = message ?: "위시가 성공적으로 생성되었습니다."
        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()

        // 이벤트 전송
        parentFragmentManager.setFragmentResult("wish_register", Bundle())

        // 모든 바텀시트 닫기
        (requireActivity().supportFragmentManager.fragments).forEach {
            if (it is BottomSheetDialogFragment) {
                it.dismissAllowingStateLoss()
            }
        }
    }

    override fun onRegisterWishFailure(code: String, message: String?) {
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