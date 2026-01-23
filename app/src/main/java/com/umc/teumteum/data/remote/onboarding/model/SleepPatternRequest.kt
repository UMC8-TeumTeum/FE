package com.umc.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class SleepPatternRequest(
    @SerializedName("sleepTime") val sleepTime: String,
    @SerializedName("wakeTime") val wakeTime: String
)
