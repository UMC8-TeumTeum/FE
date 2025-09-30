package com.example.teumteum.data.remote.auth.onboarding.model

import com.google.gson.annotations.SerializedName

data class ScheduleRequest(
    @SerializedName("routine") val routine: List<Schedule>
)

data class Schedule(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("weekday") val weekday: Week,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
)

enum class Week{
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
}