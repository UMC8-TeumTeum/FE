package com.example.teumteum.data.remote.auth.onboarding.model

import com.google.gson.annotations.SerializedName

data class RemindRequest (
    @SerializedName("remindAlarms") val remindAlarms: List<Int>
)
