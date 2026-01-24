package com.umc.teumteum.data.remote.wish.model.enums

import com.google.gson.annotations.SerializedName

enum class EstimatedDurationType {
    @SerializedName("10m")
    TEN_MINUTES,

    @SerializedName("20m")
    TWENTY_MINUTES,

    @SerializedName("30m")
    THIRTY_MINUTES,

    @SerializedName("1h")
    ONE_HOUR
}