package com.example.teumteum.data.remote.friend.model

import com.google.gson.annotations.SerializedName

data class TeumScheduledResult(
    @SerializedName("teumId") val teumId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,           // format: YYYY-MM-DD
    @SerializedName("time") val time: List<TeumTime>
)

data class TeumTime(
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String
)
