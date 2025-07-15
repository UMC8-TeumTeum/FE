package com.example.teumteum.ui.wish

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teumteum.R
import com.example.teumteum.data.entities.WishItem
import com.example.teumteum.databinding.FragmentWishEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WishEditFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentWishEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishEditBinding.inflate(inflater, container, false)
        return binding.root
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

    companion object {
        fun newInstanceWithDummy(item: WishItem): WishEditFragment {
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
}