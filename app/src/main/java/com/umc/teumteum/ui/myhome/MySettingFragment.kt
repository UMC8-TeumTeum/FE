package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.umc.teumteum.R
import com.umc.teumteum.databinding.FragmentMySettingBinding
import com.umc.teumteum.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySettingFragment : Fragment() {

    private lateinit var binding: FragmentMySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMySettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyHomeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.accountSettingLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyAccountSettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.alarmSettingLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyAlarmSettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.remindSettingLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyRemindAlarmSettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.sleepSettingLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MySleepPatternSettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.blockAccountLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, BlockedAccountFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.serviceInfoLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, ServiceInfoFragment())
                .addToBackStack(null)
                .commit()
        }

    }
}