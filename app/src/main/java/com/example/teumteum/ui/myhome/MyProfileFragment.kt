package com.example.teumteum.ui.myhome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.data.remote.todo.model.TodoListResult
import com.example.teumteum.databinding.FragmentMyProfileBinding
import com.example.teumteum.ui.main.MainActivity
import com.example.teumteum.ui.main.viewModel.HomeViewModel
import com.example.teumteum.ui.myhome.viewModel.MyHomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MyProfileFragment : Fragment() {

    private lateinit var binding: FragmentMyProfileBinding

    private val viewModel: MyHomeViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyHomeFragment())
                .addToBackStack(null)
                .commit()
        }

        homeViewModel.teumTimeDays.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeHours.observe(viewLifecycleOwner) { updateTeumTime() }
        homeViewModel.teumTimeMinutes.observe(viewLifecycleOwner) { updateTeumTime() }

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModel.fetchRecentTodos(date)

        viewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.nicknameTv.text = (nickname + "님의") ?: "닉네임님의"
            binding.profileNicknameTv.text = nickname ?: "닉네임"
        }

        viewModel.field.observe(viewLifecycleOwner) { field ->
            binding.profileFieldTv.text = if (!field.isNullOrBlank()) field else "직업 없음"
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gray_teum) // 기본 이미지 리소스
                    .error(R.drawable.gray_teum)       // 에러 시 이미지
                    .into(binding.profileIv)
            } else {
                binding.profileIv.setImageResource(R.drawable.gray_teum)
            }
        }

        //  최근 투두 관찰
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

    //  화면 내에 추가
    private fun bindTodos(list: List<TodoListResult>) {
        val l = list.take(2)

        // 컨테이너 보이기/숨기기
        binding.scheduleCardContainer.visibility = if (l.isNotEmpty()) View.VISIBLE else View.GONE

        if (l.isEmpty()) return

        // 첫 번째 카드
        val first = l[0]
        binding.schedule1TimeStartTv.text = first.startTime
        binding.schedule1TimeEndTv.text   = first.endTime
        binding.schedule1TitleTv.text     = first.title

        // 두 번째 카드
        if (l.size >= 2) {
            val second = l[1]
            binding.schedule2Cl.visibility = View.VISIBLE
            binding.schedule2TimeStartTv.text = second.startTime
            binding.schedule2TimeEndTv.text   = second.endTime
            binding.schedule2TitleTv.text     = second.title
        } else {
            binding.schedule2Cl.visibility = View.GONE
        }
    }
}
