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

//    private val editedWishlist: MutableList<WishItem> = wishlist.map { it.copy() }.toMutableList() // 삭제 시 사용할 편집용 리스트

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistEditBinding.inflate(inflater, container, false)

        // ViewModel에서 데이터 복사
        editedWishlist = wishlistViewModel.wishlistItems.map { it.copy() }.toMutableList()

        // 변경 내용 적용 후 이동
        binding.completeTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WishlistFragment())
                .commit()
        }

        // 변경 반영 없이 뒤로감
        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = WishlistEditRVAdapter(editedWishlist)
        binding.wishlistRv.adapter = adapter

        // 바텀 내비게이션 숨기기
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav?.visibility = View.GONE

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnWishDelete.setOnClickListener {
            val selectedIds = adapter.getCheckedWishIdsAndRemove()

            if (selectedIds.isNotEmpty()) {
                val request = DeleteWishesRequest(selectedIds)
                val service = WishService()
                service.setWishDeleteView(this)
                service.deleteWishes(request)
            } else {
                Toast.makeText(requireContext(), "삭제할 위시를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWishCancel.setOnClickListener {
            adapter.cancelAllCheckedItems()
            Toast.makeText(requireContext(), "선택이 모두 해제되었어요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteWishesSuccess(code: String, message: String?) {
        Toast.makeText(requireContext(), "삭제가 완료되었어요!", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteWishesFailure(code: String, message: String?) {
        Toast.makeText(requireContext(), "삭제에 실패했어요. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
    }

}