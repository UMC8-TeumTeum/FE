package com.umc.teumteum.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc.teumteum.R
import com.umc.teumteum.databinding.BottomSheetContentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetContentFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetContentBinding? = null
    private val binding get() = _binding!!

    private var aiId: String? = null
    private var wishId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetContentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("title")
        binding.titleTv.text = title

        val content = arguments?.getString("content")
        binding.contentTv.text = content

        val estimatedDuration = arguments?.getString("time")
        binding.wishTimeTv.text = estimatedDuration

        aiId = arguments?.getString("ai_id")
        wishId = arguments?.getLong("wish_id", -1L)
            ?.takeIf { it > 0L }

        binding.selectBtn.setOnClickListener {
            val fragment = FillingSetting01Fragment().apply {
                arguments = Bundle().apply {
                    aiId?.let { putString("ai_id", it)}
                    wishId?.let { putLong("wish_id", it) }
                    putString("title", title)
                    putString("content", content)
                    putString("time", estimatedDuration)
                }
            }

            dismiss()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}