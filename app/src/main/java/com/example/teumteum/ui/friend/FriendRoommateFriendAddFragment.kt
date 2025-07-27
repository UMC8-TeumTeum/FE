package com.example.teumteum.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentFriendRoommateFriendAddBinding

class FriendRoommateFriendAddFragment : Fragment() {

    private var _binding: FragmentFriendRoommateFriendAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendAddAdapter
    private val friendList = listOf(
        Friend("이슬민", R.drawable.gray_teum),
        Friend("장채미", R.drawable.gray_teum),
        Friend("하수연", R.drawable.gray_teum),
        Friend("최영원", R.drawable.gray_teum),
        Friend("안지현", R.drawable.gray_teum),
        Friend("장유", R.drawable.gray_teum),
        Friend("박성우", R.drawable.gray_teum),
        Friend("채남준", R.drawable.gray_teum)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendRoommateFriendAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapter = FriendAddAdapter(friendList)
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendRecyclerView.adapter = adapter

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
