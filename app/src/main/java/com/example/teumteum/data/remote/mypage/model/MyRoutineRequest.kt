package com.example.teumteum.data.remote.mypage.model

import com.example.teumteum.data.remote.onboarding.model.Week
import com.google.gson.annotations.SerializedName

data class MyRoutineRequest (
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("weekday") val weekday: Week,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)