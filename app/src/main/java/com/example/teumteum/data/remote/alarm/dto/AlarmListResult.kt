package com.example.teumteum.data.remote.alarm.dto

import com.example.teumteum.data.entities.Alarm
import com.google.gson.annotations.SerializedName

data class AlarmListResult (
    @SerializedName("alarmList") val alarmList: List<Alarm>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("isFirst") val isFirst: Boolean,
    @SerializedName("isLast") val isLast: Boolean
)