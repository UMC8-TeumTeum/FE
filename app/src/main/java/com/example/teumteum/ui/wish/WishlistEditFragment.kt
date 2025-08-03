package com.example.teumteum.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.FragmentWishlistEditBinding
import com.example.teumteum.ui.wish.adapter.WishlistEditRVAdapter
import com.example.teumteum.ui.wish.viewModel.WishViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WishlistEditFragment() : Fragment() {

    private lateinit var binding: FragmentWishlistEditBinding

    private lateinit var adapter: WishlistEditRVAdapter
    private lateinit var editedWishlist: MutableList<WishlistItem>

    private val wishViewModel: WishViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistEditBinding.inflate(inflater, container, false)

        // ViewModel에서 데이터 복사
        editedWishlist = wishViewModel.wishlistItems.value?.map { it.copy() }?.toMutableList() ?: mutableListOf()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        if (editedWishlist.isEmpty()) {
            // 위시가 없을 경우
            binding.wishlistRv.visibility = View.GONE
            binding.wishNotExistsCv.visibility = View.VISIBLE
        } else {
            // 위시가 있을 경우
            binding.wishlistRv.visibility = View.VISIBLE
            binding.wishNotExistsCv.visibility = View.GONE

            adapter = WishlistEditRVAdapter(editedWishlist)
            binding.wishlistRv.adapter = adapter

            setupButtons()
        }

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupObservers()
    }

    private fun setupButtons() {
        binding.btnWishDelete.setOnClickListener {
            val deletedCount = adapter.markCheckedItemsAsDeleted()
            if (deletedCount > 0) {
                Toast.makeText(requireContext(), "${deletedCount}개 위시가 삭제되었어요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "삭제할 위시를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWishCancel.setOnClickListener {
            adapter.cancelAllCheckedItems()
            Toast.makeText(requireContext(), "선택이 모두 해제되었어요.", Toast.LENGTH_SHORT).show()
        }

        binding.completeTv.setOnClickListener {
            val deletedIds = editedWishlist.filter { it.isDeleted }.map { it.id }

            if (deletedIds.isNotEmpty()) {
                val request = DeleteWishesRequest(deletedIds)
                wishViewModel.deleteWishes(request)
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistFragment())
                .commit()
        }

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        wishViewModel.successMessage.observe(viewLifecycleOwner) { _ ->
            Toast.makeText(requireContext(), "삭제가 완료되었어요.", Toast.LENGTH_SHORT).show()

            // ViewModel에서 삭제 반영
            val deletedIds = editedWishlist.filter { it.isDeleted }.map { it.id }
            val updatedList = wishViewModel.wishlistItems.value?.filterNot { it.id in deletedIds } ?: emptyList()
            wishViewModel.wishlistItems.value = updatedList

            // 결과 전달
            parentFragmentManager.setFragmentResult("wish_delete", Bundle())

            // 뒤로 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistFragment())
                .commit()
        }

        wishViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), "삭제 실패: $error", Toast.LENGTH_SHORT).show()
        }
    }

}