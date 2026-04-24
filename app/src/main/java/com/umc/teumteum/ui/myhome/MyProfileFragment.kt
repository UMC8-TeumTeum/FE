package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.mypage.model.PublicTodoResponse
import com.umc.teumteum.databinding.FragmentMyProfileBinding
import com.umc.teumteum.ui.main.HomeFragment
import com.umc.teumteum.ui.main.MainActivity
import com.umc.teumteum.ui.main.viewModel.HomeViewModel
import com.umc.teumteum.ui.myhome.viewModel.MyHomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyHomeViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentMyProfileContainer) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentMyProfileContainer.updatePadding(bottom = systemBars.bottom)
            insets
        }

        // 최초 1회 조회
        if (!viewModel.isLoaded) {
            viewModel.getMyInfo()
        }

        // 프로필 수정 후 돌아왔을 때만 재조회
        parentFragmentManager.setFragmentResultListener(
            "profile_modify_result",
            viewLifecycleOwner
        ) { _, bundle ->
            val updated = bundle.getBoolean("profile_updated", false)
            if (updated) {
                viewModel.getMyInfo()
            }
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyHomeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.modifyProfileBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyProfileModifyFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.seeMoreTv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        homeViewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

        viewModel.fetchRecentTodos()

        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.profileNicknameTv.text = nickname ?: "닉네임"
        }

        viewModel.field.observe(viewLifecycleOwner) { field ->
            binding.profileFieldTv.text = if (!field.isNullOrBlank()) field else "직업 없음"
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_teum)
                    .error(R.drawable.gray_teum)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.profileIv)
            } else {
                binding.profileIv.setImageResource(R.drawable.gray_teum)
            }
        }

        viewModel.recentTodos.observe(viewLifecycleOwner) { list ->
            bindTodos(list)
        }
    }

    private fun updateTeumTime() {
        val days = homeViewModel.teumTimeDays.value ?: 0
        val hours = homeViewModel.teumTimeHours.value ?: 0
        val minutes = homeViewModel.teumTimeMinutes.value ?: 0
        binding.profileTimerTv.text = "${days}일 ${hours}시간 ${minutes}분"
    }

    private fun bindTodos(list: List<PublicTodoResponse>) {
        val l = list.take(2)

        binding.scheduleCardContainer.visibility =
            if (l.isNotEmpty()) View.VISIBLE else View.GONE

        if (l.isEmpty()) return

        val first = l[0]
        binding.schedule1TimeStartTv.text = first.startTime
        binding.schedule1TimeEndTv.text = first.endTime
        binding.schedule1TitleTv.text = first.title

        if (l.size >= 2) {
            val second = l[1]
            binding.schedule2Cl.visibility = View.VISIBLE
            binding.schedule2TimeStartTv.text = second.startTime
            binding.schedule2TimeEndTv.text = second.endTime
            binding.schedule2TitleTv.text = second.title
        } else {
            binding.schedule2Cl.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}