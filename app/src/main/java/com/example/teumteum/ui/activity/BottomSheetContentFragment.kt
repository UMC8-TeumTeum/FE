package com.example.teumteum.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.databinding.BottomSheetContentBinding
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
        setTitle(title.toString())

        val content = arguments?.getString("content")
        setContent(content.toString())

        aiId = arguments?.getString("ai_id")
        wishId = arguments?.getLong("wish_id", -1L)
            ?.takeIf { it > 0L }
    }

    private fun setTitle(title: String){
        binding.titleTv.text = title
    }

    private fun setContent(content: String){
        binding.contentTv.text = content
    }
}