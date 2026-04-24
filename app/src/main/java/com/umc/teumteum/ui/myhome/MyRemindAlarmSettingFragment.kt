package com.umc.teumteum.ui.myhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.umc.teumteum.databinding.FragmentMyRemindAlarmSettingBinding
import com.umc.teumteum.ui.myhome.viewModel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyRemindAlarmSettingFragment : Fragment() {

    private var _binding: FragmentMyRemindAlarmSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingViewModel by viewModels()

    private var updatingFromServer = false
    private var saveJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRemindAlarmSettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeRemindAlarms()
        setupSwitchListeners()
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.getRemindAlarms()
    }

    private fun observeRemindAlarms() {
        viewModel.remindAlarms.observe(viewLifecycleOwner) { alarms ->
            updatingFromServer = true
            applySwitchStateFromServer(alarms)
            updatingFromServer = false
        }
    }

    private fun setupSwitchListeners() {
        val listener = CompoundButton.OnCheckedChangeListener { _, _ ->
            if (updatingFromServer) return@OnCheckedChangeListener
            debounceSaveRemindAlarms()
        }

        binding.remind1mSwitch.setOnCheckedChangeListener(listener)
        binding.remind3mSwitch.setOnCheckedChangeListener(listener)
        binding.remind5mSwitch.setOnCheckedChangeListener(listener)
        binding.remind10mSwitch.setOnCheckedChangeListener(listener)
        binding.remind30mSwitch.setOnCheckedChangeListener(listener)
    }

    private fun applySwitchStateFromServer(alarms: List<Int>) {
        binding.remind1mSwitch.isChecked = 1 in alarms
        binding.remind3mSwitch.isChecked = 3 in alarms
        binding.remind5mSwitch.isChecked = 5 in alarms
        binding.remind10mSwitch.isChecked = 10 in alarms
        binding.remind30mSwitch.isChecked = 30 in alarms
    }

    private fun getSelectedRemindMinutes(): List<Int> {
        val selected = mutableListOf<Int>()
        if (binding.remind1mSwitch.isChecked) selected += 1
        if (binding.remind3mSwitch.isChecked) selected += 3
        if (binding.remind5mSwitch.isChecked) selected += 5
        if (binding.remind10mSwitch.isChecked) selected += 10
        if (binding.remind30mSwitch.isChecked) selected += 30
        return selected
    }

    private fun debounceSaveRemindAlarms() {
        val selected = getSelectedRemindMinutes()

        saveJob?.cancel()
        saveJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(250)
            viewModel.updateRemindAlarms(selected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveJob?.cancel()
        _binding = null
    }
}