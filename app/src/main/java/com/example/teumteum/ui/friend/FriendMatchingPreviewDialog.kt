package com.example.teumteum.ui.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.teumteum.R
import com.example.teumteum.databinding.DialogFriendMatchingPreviewBinding
import com.example.teumteum.ui.friend.adapter.TeumImagePagerAdapter
import com.example.teumteum.ui.friend.viewModel.FriendViewModel

class FriendMatchingPreviewDialog : DialogFragment() {

    private var _binding: DialogFriendMatchingPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendViewModel by activityViewModels()

    private var currentIndex = 0

    private val imageList = listOf(
        R.drawable.friend_teum_logo,
        R.drawable.teumi_teuma_eat,
        R.drawable.teumi_teuma_ball,
        R.drawable.teumi_teuma_down,
        R.drawable.teumi_teuma_juice
    )

    private lateinit var pagerAdapter: TeumImagePagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFriendMatchingPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = window.attributes
            params.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = (resources.displayMetrics.heightPixels * 0.18).toInt()
            params.dimAmount = 0.5f
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 무조건 ViewModel 기반 전체 세팅
        updateSuggestion()

        // 2) 겹침 조회에서 넘어온 값이 있으면 덮어쓰기
        arguments?.let { args ->
            binding.title.text = args.getString("title") ?: binding.title.text
            binding.detailSentence.text = args.getString("description") ?: binding.detailSentence.text

            val start = args.getString("startTime")
            val end = args.getString("endTime")
            if (!start.isNullOrBlank() && !end.isNullOrBlank()) {
                binding.tvTime.text = "$start ~ $end"
            }
        }

        // 3) ViewPager2로 드래그(스와이프) 이미지 넘기기
        setupImagePager()

        // 버튼도 그대로 사용하고 싶으면 유지
        binding.btnNext.setOnClickListener {
            val next = (binding.vpTeum.currentItem + 1) % imageList.size
            binding.vpTeum.setCurrentItem(next, true)
        }

        binding.btnPrev.setOnClickListener {
            val prev = if (binding.vpTeum.currentItem == 0) imageList.size - 1 else binding.vpTeum.currentItem - 1
            binding.vpTeum.setCurrentItem(prev, true)
        }

        binding.btnSend.setOnClickListener {
            val request = viewModel.buildTeumRequest()
            setLoading(true)
            if (request == null) return@setOnClickListener

            viewModel.sendTeumRequest(
                request,
                onSuccess = {
                    viewModel.setTeumRequestReceiverUserIds(emptyList())
                    if (isAdded &&
                        this@FriendMatchingPreviewDialog.view != null &&
                        lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                    ) {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, FriendSendFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    dismissAllowingStateLoss()
                },
                onError = { msg ->
                    setLoading(false)
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setupImagePager() {
        pagerAdapter = TeumImagePagerAdapter(imageList)
        binding.vpTeum.adapter = pagerAdapter
        binding.vpTeum.offscreenPageLimit = 1

        // 초기 값 반영
        binding.vpTeum.setCurrentItem(currentIndex, false)
        viewModel.setTeumRequestGraphicId(currentIndex)

        // 드래그로 바뀔 때마다 currentIndex + graphicId 반영
        binding.vpTeum.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentIndex = position
                viewModel.setTeumRequestGraphicId(position)
            }
        })
    }

    private fun updateSuggestion() {
        binding.title.text = viewModel.teumRequestTitle.value
        binding.detailSentence.text = viewModel.teumRequestDescription.value

        viewModel.teumRequestReceiverUserIds.value?.size?.let {
            binding.tvName.text =
                if (it > 0) {
                    "${viewModel.teumRequestMainTargetUserName.value} 외 ${it}명"
                } else {
                    viewModel.teumRequestMainTargetUserName.value ?: ""
                }
        }

        Glide.with(binding.imgProfile)
            .load(viewModel.teumRequestMainTargetProfileImage.value)
            .placeholder(R.drawable.gray_teum)
            .error(R.drawable.gray_teum)
            .circleCrop()
            .into(binding.imgProfile)

        // 날짜 포맷 적용
        val rawDate = viewModel.teumRequestSelectedDate.value
        binding.tvDate.text = "${formatDate(rawDate)}     |"

        // 시간
        binding.tvTime.text =
            "${viewModel.teumRequestSelectedTime.value?.startTime} ~ ${viewModel.teumRequestSelectedTime.value?.endTime}"
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val parsed = java.time.LocalDate.parse(date)
            parsed.format(java.time.format.DateTimeFormatter.ofPattern("yy.MM.dd"))
        } catch (e: Exception) {
            date
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLoading(loading: Boolean) {
        // 버튼 막기
        binding.btnSend.isEnabled = !loading
        binding.btnPrev.isEnabled = !loading
        binding.btnNext.isEnabled = !loading

        // 로딩 중 드래그도 막고 싶으면 켜기
        binding.vpTeum.isUserInputEnabled = !loading

        // 다이얼로그 취소/바깥터치 방지
        isCancelable = !loading
        dialog?.setCanceledOnTouchOutside(!loading)
    }

    companion object {
        const val TAG = "FriendMatchingPreviewDialog"
    }
}
