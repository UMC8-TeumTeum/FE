package com.example.teumteum.data.remote.onboarding.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalTime

data class SleepPatternRequest(
    @SerializedName("sleepTime") val sleepTime: LocalTime,
    @SerializedName("wakeTime") val wakeTime: LocalTime
)
