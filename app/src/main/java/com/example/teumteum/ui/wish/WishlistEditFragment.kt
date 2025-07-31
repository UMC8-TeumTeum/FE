package com.example.teumteum.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.teumteum.R
import com.example.teumteum.data.remote.wish.WishService
import com.example.teumteum.data.remote.wish.dto.DeleteWishesRequest
import com.example.teumteum.data.remote.wish.dto.WishlistItem
import com.example.teumteum.databinding.FragmentWishlistEditBinding
import com.example.teumteum.ui.wish.adapter.WishlistEditRVAdapter
import com.example.teumteum.ui.wish.view.DeleteWishesView
import com.example.teumteum.ui.wish.view.WishlistViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class WishlistEditFragment() : Fragment(), DeleteWishesView {

    private lateinit var binding: FragmentWishlistEditBinding

    private lateinit var adapter: WishlistEditRVAdapter
    private lateinit var editedWishlist: MutableList<WishlistItem>
    private val wishlistViewModel: WishlistViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistEditBinding.inflate(inflater, container, false)

        // ViewModel에서 데이터 복사
        editedWishlist = wishlistViewModel.wishlistItems.map { it.copy() }.toMutableList()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        // ViewModel에서 데이터 복사
        editedWishlist = wishlistViewModel.wishlistItems.map { it.copy() }.toMutableList()

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
                val service = WishService()
                service.setWishDeleteView(this)
                service.deleteWishes(request)

                // ViewModel에도 반영
                wishlistViewModel.wishlistItems.removeAll { it.id in deletedIds }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistFragment())
                .commit()
        }

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDeleteWishesSuccess(code: String, message: String?) {
        val successMessage = message ?: "위시 정보가 성공적으로 삭제되었습니다."
        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteWishesFailure(code: String, message: String?) {
        val errorMessage = when (code) {
            "HOME4043" -> "해당 위시 정보를 찾을 수 없습니다."
            "HOME4042" -> "해당 카테고리를 찾을 수 없습니다."
            "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다."
            "PARSE_ERROR" -> "서버 응답을 해석할 수 없습니다."
            else -> message ?: "등록에 실패했습니다. 다시 시도해주세요."
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

}