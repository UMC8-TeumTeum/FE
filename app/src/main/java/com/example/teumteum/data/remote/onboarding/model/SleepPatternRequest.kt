package com.example.teumteum.data.remote.auth.onboarding.model

import com.google.gson.annotations.SerializedName

data class SleepPatternRequest(
    @SerializedName("sleepTime") val sleepTime: String,
    @SerializedName("wakeTime") val wakeTime: String
)
