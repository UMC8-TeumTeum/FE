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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
class WishlistEditFragment : Fragment() {

    private var _binding: FragmentWishlistEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WishlistEditRVAdapter
    private val edited: MutableList<WishlistItem> = mutableListOf()

    // 페이지 append 시에 선택(삭제 체크) 유지용
    private val selectedIds = mutableSetOf<Long>()

    private val viewModel: WishViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.findViewById<BottomNavigationView>(R.id.main_bnv)?.visibility = View.GONE

        // 리사이클러/어댑터
        adapter = WishlistEditRVAdapter(edited)
        val lm = LinearLayoutManager(requireContext())
        binding.wishlistRv.layoutManager = lm
        binding.wishlistRv.adapter = adapter

        setupButtons()
        observeViewModel()

        // 무한 스크롤: 끝 근처에서 다음 페이지 로드
        binding.wishlistRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val last = lm.findLastVisibleItemPosition()
                val total = adapter.itemCount
                if (last >= total - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        // 진입 시 무조건 ALL 1페이지부터
        viewModel.refreshWishlist("all")
    }

    private fun observeViewModel() {
        viewModel.wishlistItems.observe(viewLifecycleOwner) { serverList ->
            // 가시성
            if (serverList.isNullOrEmpty()) {
                binding.wishlistRv.visibility = View.GONE
                binding.wishNotExistsCv.visibility = View.VISIBLE
            } else {
                binding.wishlistRv.visibility = View.VISIBLE
                binding.wishNotExistsCv.visibility = View.GONE
            }

            // 선택(삭제 체크) 보존
            val prevSelected = selectedIds.toSet()

            edited.clear()
            edited.addAll(
                serverList.map { item ->
                    item.copy(isDeleted = item.id in prevSelected)
                }
            )

            // (선택) 동기화: 내부 selectedIds를 현재 edited 상태로 갱신
            selectedIds.clear()
            selectedIds.addAll(edited.filter { it.isDeleted }.map { it.id })

            adapter.notifyDataSetChanged()
        }

        // 삭제 성공 → 다시 ALL로 최신화 후 화면 닫기
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteSuccess.collect {
                    Toast.makeText(requireContext(), "삭제가 완료되었어요.", Toast.LENGTH_SHORT).show()
                    viewModel.refreshWishlist("all") // 요구사항: 삭제는 항상 ALL
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { err ->
            Log.e("WISHLIST_EDIT", "오류: $err")
        }
    }

    private fun setupButtons() {
        binding.btnWishDelete.setOnClickListener {
            // 어댑터에서 체크 표시 반영
            val cnt = adapter.markCheckedItemsAsDeleted()

            // 현재 편집 리스트에서 선택 집합 갱신(보존의 핵심)
            selectedIds.clear()
            selectedIds.addAll(edited.filter { it.isDeleted }.map { it.id })

            if (cnt <= 0) {
                Toast.makeText(requireContext(), "삭제할 위시를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWishCancel.setOnClickListener {
            adapter.cancelAllCheckedItems()
            // 선택 취소 시에도 선택 집합 초기화
            selectedIds.clear()
        }

        binding.completeTv.setOnClickListener {
            val ids = edited.filter { it.isDeleted }.map { it.id }
            if (ids.isNotEmpty()) {
                viewModel.deleteWishes(DeleteWishesRequest(ids))
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        binding.backArrowIv.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}
