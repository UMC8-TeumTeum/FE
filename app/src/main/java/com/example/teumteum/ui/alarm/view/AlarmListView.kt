package com.example.teumteum.ui.alarm.view

import com.example.teumteum.data.entities.Alarm

interface AlarmListView {
    fun onGetAlarmListSuccess(alarmList: List<Alarm>)
    fun onGetAlarmListFailure(code: String, message: String? = null)
}