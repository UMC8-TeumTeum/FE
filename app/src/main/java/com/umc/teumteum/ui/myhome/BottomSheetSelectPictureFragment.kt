package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.teumteum.databinding.FragmentBottomSheetSelectPictureBinding

class BottomSheetSelectPictureFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetSelectPictureBinding? = null
    private val binding get() = _binding!!

    private var onGalleryClick: (() -> Unit)? = null
    private var onDefaultProfileClick: (() -> Unit)? = null

    fun setOnGalleryClickListener(listener: () -> Unit) {
        onGalleryClick = listener
    }

    fun setOnDefaultProfileClickListener(listener: () -> Unit) {
        onDefaultProfileClick = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetSelectPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.galleryLl.setOnClickListener {
            onGalleryClick?.invoke()
            dismiss()
        }

        binding.defaultProfileLl.setOnClickListener {
            onDefaultProfileClick?.invoke()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onGalleryClick = null
        onDefaultProfileClick = null
        _binding = null
    }
}