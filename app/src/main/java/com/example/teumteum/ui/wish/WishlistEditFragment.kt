package com.example.teumteum.ui.wish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.model.WishlistItem
import com.example.teumteum.databinding.FragmentWishlistEditBinding
import com.example.teumteum.ui.wish.adapter.WishlistEditRVAdapter
import com.example.teumteum.ui.wish.viewModel.WishViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistEditFragment() : Fragment() {

    private var _binding: FragmentWishlistEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WishlistEditRVAdapter
    private lateinit var editedWishlist: MutableList<WishlistItem>

    private val viewModel: WishViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistEditBinding.inflate(inflater, container, false)

        // ViewModel에서 데이터 복사
        editedWishlist = viewModel.wishlistItems.value?.map { it.copy() }?.toMutableList() ?: mutableListOf()

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
                Log.d("WISH_LIST_EDIT_FRAGMENT", "${deletedCount}개 위시가 삭제되었어요.")
            } else {
                Toast.makeText(requireContext(), "삭제할 위시를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWishCancel.setOnClickListener {
            adapter.cancelAllCheckedItems()
        }

        binding.completeTv.setOnClickListener {
            val deletedIds = editedWishlist.filter { it.isDeleted }.map { it.id }

            if (deletedIds.isNotEmpty()) {
                val request = DeleteWishesRequest(deletedIds)
                viewModel.deleteWishes(request)
            }

            parentFragmentManager.setFragmentResult("wish_delete", Bundle())
            parentFragmentManager.popBackStack()
        }

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteSuccess.collect {
                    Toast.makeText(requireContext(), "삭제가 완료되었어요.", Toast.LENGTH_SHORT).show()

                    // ViewModel 내 리스트 갱신
                    val deletedIds = editedWishlist.filter { it.isDeleted }.map { it.id }
                    val updatedList =
                        viewModel.wishlistItems.value?.filterNot { it.id in deletedIds }
                            ?: emptyList()
                    viewModel.updateWishlistItems(updatedList)

                    // 삭제 결과 전달
                    parentFragmentManager.setFragmentResult("wish_delete", Bundle())

                    // 위시리스트 화면으로 이동
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, WishlistFragment())
                        .commit()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
//            Toast.makeText(requireContext(), "삭제 실패: $error", Toast.LENGTH_SHORT).show()
            Log.e("WISH_LIST_EDIT_FRAGMENT", "삭제 실패: $error")
        }
    }
}