package com.umc.teumteum.ui.wish

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
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.wish.model.DeleteWishesRequest
import com.umc.teumteum.data.remote.wish.model.WishlistItem
import com.umc.teumteum.databinding.FragmentWishlistEditBinding
import com.umc.teumteum.ui.wish.adapter.WishlistEditAdapter
import com.umc.teumteum.ui.wish.viewModel.WishViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistEditFragment : Fragment() {

    private var _binding: FragmentWishlistEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WishlistEditAdapter
    private val edited: MutableList<WishlistItem> = mutableListOf()

    // 어댑터에서 실제 제거한 항목들을 서버에 일괄 반영하기 위한 보류 목록
    private val pendingDeleteIds = mutableSetOf<Long>()

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
        adapter = WishlistEditAdapter(edited)
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

            // 서버 리스트를 그대로 반영 (선택/체크 보존은 어댑터에서 id 기반으로 처리)
            edited.clear()
            edited.addAll(serverList)

            adapter.notifyDataSetChanged()
        }

        // 삭제 성공 → 다시 ALL로 최신화 후 화면 닫기
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteSuccess.collect {
                    Log.d("WISH_EDIT_FRAGMENT", "위시가 성공적으로 삭제되었습니다.")
                    pendingDeleteIds.clear()
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
            // 어댑터에서 실제 제거하고 제거된 id들을 돌려받아 보류 목록에 누적
            val removedIds: List<Long> = adapter.markCheckedItemsAsDeletedAndReturnIds()
            pendingDeleteIds.addAll(removedIds)

            if (removedIds.isEmpty()) {
                Toast.makeText(requireContext(), "삭제할 위시를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWishCancel.setOnClickListener {
            adapter.cancelAllCheckedItems()
        }

        binding.completeTv.setOnClickListener {
            // 현재 화면에서는 이미 리스트에서 제거되었으므로 보류 목록으로 서버 삭제
            if (pendingDeleteIds.isNotEmpty()) {
                viewModel.deleteWishes(DeleteWishesRequest(pendingDeleteIds.toList()))
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        binding.backArrowIv.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
