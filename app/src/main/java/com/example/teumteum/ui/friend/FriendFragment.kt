package com.example.teumteum.ui.friend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendBinding
import com.example.teumteum.ui.main.MainActivity

class FriendFragment : Fragment() {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    // 1) 시뮬레이션용 리스트
    private val possibleNames = listOf("홍길동","김영희","박민수","최서연")
    private val requesters = mutableListOf<String>()

    private val handler = Handler(Looper.getMainLooper())
    private val simulateRunnable = object : Runnable {
        override fun run() {
            simulateNewRequest()
            // 5초마다 다시 실행
            handler.postDelayed(this, 5_000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 2) 5초 뒤 시뮬레이션 시작
        handler.postDelayed(simulateRunnable, 5_000)

        if (requesters.isNotEmpty()) {
            // 상단 텍스트를 "나>홍길동" 으로 변경
            binding.textName.text = "나>${requesters.first()}"

            // 텍스트를 클릭하면 요청 화면으로 이동
            binding.textName.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, Friend02ResponseFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 추천 카드 RecyclerView 설정
        val recommendAdapter = RecommendAdapter { // 카드 클릭 시
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02RequestFragment()) // 프래그먼트 교체
                .addToBackStack(null)
                .commit()
        }

        // 약속된 틈 보러가기
//        binding.viewPromiseBtn.setOnClickListener {
//            parentFragmentManager.beginTransaction()
//                .replace(
//                    R.id.main_frm,                   // 메인 컨테이너 ID
//                    FriendPromiseFragment()          // 이동할 Fragment
//                )
//                .addToBackStack(null)              // 뒤로 가기 허용
//                .commit()
//        }

        binding.recommendRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendRecyclerView.adapter = recommendAdapter

        binding.btnSearch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend01SearchFragment()) // container는 main_container로 되어 있어야 함
                .addToBackStack(null) // 뒤로 가기 가능하도록
                .commit()
        }


        val dummyFollowingList = listOf(
            FollowUser("문혜원", "UX 디자이너"),
            FollowUser("하수연", "UX 디자이너"),
            FollowUser("장채미", "UX 디자이너"),
            FollowUser("이솔민", "UX 디자이너"),
            FollowUser("이솔", "UX 디자이너"),
            FollowUser("이솔", "UX 디자이너"),
            FollowUser("이솔", "UX 디자이너")

        )

        val followingAdapter = FollowingAdapter(dummyFollowingList)
        binding.followingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.followingRecyclerView.adapter = followingAdapter
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 핸들러 콜백 해제
        handler.removeCallbacks(simulateRunnable)
        _binding = null
    }

    // --- 테스트용 새 요청 함수 ---
    private fun simulateNewRequest() {
        // 랜덤 이름 뽑아서 리스트에 추가
        val newName = possibleNames.random()
        requesters.add(newName)

        // 상단 텍스트 업데이트
        binding.textName.text = "나>$newName"
        binding.textName.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, Friend02ResponseFragment())
                .addToBackStack(null)
                .commit()
        }

        // 토스트로도 알림
        Toast.makeText(requireContext(), "$newName 의 요청이 들어왔습니다", Toast.LENGTH_SHORT).show()
    }
}