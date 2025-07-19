package com.example.teumteum.ui.wish

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentWishRegisterBinding
import com.google.android.material.button.MaterialButton

class WishRegisterFragment : Fragment() {

    private lateinit var binding: FragmentWishRegisterBinding
    private var selectedTimeButton: View? = null
    private var selectedCategoryButton: View? = null

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
}