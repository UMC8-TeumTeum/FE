package com.umc.teumteum.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.alarm.dto.enums.NotificationType
import com.umc.teumteum.databinding.FragmentHomeAlarmBinding
import com.umc.teumteum.ui.alarm.adapter.AlarmRVAdapter
import com.umc.teumteum.ui.alarm.viewModel.NotificationViewModel
import com.umc.teumteum.ui.friend.FriendProfileFollowFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmFragment : Fragment() {

    private var _binding: FragmentHomeAlarmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: AlarmRVAdapter

    private var navigating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.findViewById<BottomNavigationView>(R.id.main_bnv)?.visibility = View.GONE

        // 어댑터 생성 시 콜백에서 네비게이터 + 트랜젝션 방식 적용
        adapter = AlarmRVAdapter(mutableListOf()) { notification ->
            if (navigating) return@AlarmRVAdapter
            navigating = true

            viewModel.readNotification(notification.id.toLong())

            val fragment = if (notification.type == NotificationType.FOLLOW) {
                FriendProfileFollowFragment().apply {
                    arguments = Bundle().apply {
                        putInt("userId", notification.friendId)
                    }
                }
            } else {
                AlarmNavigator.createFragmentFor(this, notification)
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }

        val lm = LinearLayoutManager(requireContext())
        binding.alarmRv.layoutManager = lm
        binding.alarmRv.adapter = adapter

        // 무한 스크롤: 끝에서 3개 남았을 때 다음 페이지 로드
        binding.alarmRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val total = lm.itemCount
                val last = lm.findLastVisibleItemPosition()
                val threshold = 3
                if (total - last <= threshold) {
                    viewModel.loadNext()
                }
            }
        })

        binding.backArrowIv.setOnClickListener { parentFragmentManager.popBackStack() }

        observeViewModel()

        // 최초 로드
        viewModel.loadFirst()
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner) { list ->

            // 알림이 오지 않은 경우
            val isEmpty = list.isNullOrEmpty()
            binding.alarmNotExistsCv.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.alarmRv.visibility = if (isEmpty) View.GONE else View.VISIBLE

            // 비어있는 경우 어댑터 비우고 종료
            if (isEmpty) {
                adapter.replaceAll(emptyList())
                return@observe
            }

            // 첫 페이지인지 아닌지에 따라 처리
            if (adapter.itemCount == 0) {
                adapter.replaceAll(list)
            } else {
                // 리스트가 누적 상태이므로, 어댑터의 사이즈보다 많아졌다면 append만
                if (list.size > adapter.itemCount) {
                    val newItems = list.subList(adapter.itemCount, list.size)
                    adapter.append(newItems)
                } else {
                    adapter.replaceAll(list)
                }
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onResume() {
        super.onResume()
        navigating = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}