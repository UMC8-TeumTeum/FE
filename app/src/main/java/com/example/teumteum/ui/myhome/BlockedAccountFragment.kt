package com.example.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teumteum.R
import com.example.teumteum.databinding.FragmentBlockedAccountBinding
import com.example.teumteum.ui.myhome.adapter.BlockedAccountAdapter
import com.example.teumteum.ui.myhome.data.BlockedAccount
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedAccountFragment : Fragment() {

    private var _binding: FragmentBlockedAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터
        //Todo: 차단된 계정 API
        val dummyList = listOf(
            BlockedAccount("미나리", "UX 디자이너", R.drawable.ic_empty_profile_sv),
            BlockedAccount("미나리나물", "개발자", R.drawable.ic_empty_profile_sv),
            BlockedAccount("미나리무침", "학원 강사", R.drawable.ic_empty_profile_sv),
            BlockedAccount("미미미", "매니저", R.drawable.ic_empty_profile_sv)
        )

        val adapter = BlockedAccountAdapter(dummyList) { account ->
            //Todo: 차단해제 API 연동
            Toast.makeText(requireContext(), "${account.name} 차단 해제", Toast.LENGTH_SHORT).show()
        }

        binding.blockedAccountRv.adapter = adapter
        binding.blockedAccountRv.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
