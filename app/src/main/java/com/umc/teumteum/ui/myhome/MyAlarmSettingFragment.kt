package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.umc.teumteum.data.remote.mypage.model.PushAlarmRequest
import com.umc.teumteum.databinding.FragmentMyAlarmSettingBinding
import com.umc.teumteum.ui.main.MainActivity
import com.umc.teumteum.ui.myhome.viewModel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyAlarmSettingFragment : Fragment() {

    private lateinit var binding: FragmentMyAlarmSettingBinding

    private val viewModel: SettingViewModel by viewModels()

    private var internalUpdate = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAlarmSettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.hideBottomBar()

        setupSwitchListeners()
        observeViewModel()

        viewModel.getAlarmSettings()

        binding.backArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupSwitchListeners() {
        binding.pushAlarmPauseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (internalUpdate) return@setOnCheckedChangeListener

            if (isChecked) {
                internalUpdate = true
                setAllDetailSwitches(false)
                internalUpdate = false
            }
            sendCurrentSetting()
        }

        val normalListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            if (internalUpdate) return@OnCheckedChangeListener

            internalUpdate = true
            syncPauseSwitchFromDetails()
            internalUpdate = false

            sendCurrentSetting()
        }

        binding.todayTodoSwitch.setOnCheckedChangeListener(normalListener)
        binding.remindSettingSwitch.setOnCheckedChangeListener(normalListener)
        binding.newFollowerSwitch.setOnCheckedChangeListener(normalListener)
        binding.teumRequestSwitch.setOnCheckedChangeListener(normalListener)
    }

    private fun setAllDetailSwitches(isOn: Boolean) {
        binding.todayTodoSwitch.isChecked = isOn
        binding.remindSettingSwitch.isChecked = isOn
        binding.newFollowerSwitch.isChecked = isOn
        binding.teumRequestSwitch.isChecked = isOn
    }

    private fun sendCurrentSetting() {
        val paused = binding.pushAlarmPauseSwitch.isChecked

        val request = PushAlarmRequest(
            todayTodo = if (paused) false else binding.todayTodoSwitch.isChecked,
            remindAlarm = if (paused) false else binding.remindSettingSwitch.isChecked,
            teum = if (paused) false else binding.teumRequestSwitch.isChecked,
            follow = if (paused) false else binding.newFollowerSwitch.isChecked
        )

        viewModel.updatePushAlarmSetting(request)
    }

    private fun observeViewModel() {
        viewModel.alarmSetting.observe(viewLifecycleOwner) { setting ->
            internalUpdate = true

            binding.todayTodoSwitch.isChecked = setting.todayTodo
            binding.remindSettingSwitch.isChecked = setting.remindAlarm
            binding.newFollowerSwitch.isChecked = setting.follow
            binding.teumRequestSwitch.isChecked = setting.teum

            syncPauseSwitchFromDetails()

            internalUpdate = false
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg.isNullOrBlank()) return@observe
        }
    }

    private fun syncPauseSwitchFromDetails() {
        val allOff =
            !binding.todayTodoSwitch.isChecked &&
                    !binding.remindSettingSwitch.isChecked &&
                    !binding.newFollowerSwitch.isChecked &&
                    !binding.teumRequestSwitch.isChecked

        binding.pushAlarmPauseSwitch.isChecked = allOff
    }
}