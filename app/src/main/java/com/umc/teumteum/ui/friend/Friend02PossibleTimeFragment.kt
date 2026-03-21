package com.umc.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.umc.teumteum.R
import com.umc.teumteum.data.remote.friend.model.TeumReceivedItem
import com.umc.teumteum.databinding.FragmentFriend02PossibleTimeBinding
import com.umc.teumteum.ui.friend.adapter.FriendRequestCardAdapter
import com.umc.teumteum.ui.friend.viewModel.FriendViewModel
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class Friend02PossibleTimeFragment : Fragment() {

    private var _binding: FragmentFriend02PossibleTimeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendRequestCardAdapter
    private var teumList: List<TeumReceivedItem> = emptyList()
    private var responseId: Int = -1

    private val viewModel: FriendViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriend02PossibleTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 네비게이션 바
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.setPadding(0, 0, 0, bottomInset)
            insets
        }

        // 전달받은 데이터 받기
        val selected = viewModel.selectedTeum.value
        if (selected == null) {
            parentFragmentManager.popBackStack()
            return
        }

        teumList = listOf(selected)
        responseId = arguments?.getInt("responseId") ?: -1

        adapter = FriendRequestCardAdapter(teumList)
        binding.requestViewPager.adapter = adapter

        (activity as? MainActivity)?.hideBottomBar()

        binding.backButton.setOnClickListener {
            val selected = viewModel.selectedTeum.value

            if (selected?.resend == true) {
                // 재요청 카드: response 화면
                val frag = Friend02ResponseFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("teumItem", selected)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, frag)
                    .addToBackStack(null)
                    .commit()
            } else {
                // 원본 요청 카드: request 화면
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, Friend02RequestFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        // "찾기" 버튼 클릭 → teumList, responseId 넘기기
        binding.btnFind.setOnClickListener {
            val fragment = Friend02SuggestFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", ArrayList(teumList))
                    putInt("responseId", responseId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(teumList: ArrayList<TeumReceivedItem>, responseId: Int): Friend02PossibleTimeFragment {
            return Friend02PossibleTimeFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("teumList", teumList)
                    putInt("responseId", responseId)
                }
            }
        }
    }
}
