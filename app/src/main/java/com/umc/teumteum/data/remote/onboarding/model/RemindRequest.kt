package com.umc.teumteum.data.remote.onboarding.model

import com.google.gson.annotations.SerializedName

data class RemindRequest (
    @SerializedName("remindAlarms") val remindAlarms: List<Int>
)
