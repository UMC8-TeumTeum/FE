package com.umc.teumteum.ui.main.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeBlock(
    val startTime: Int,
    val endTime: Int,
    val type: TimeType
) : Parcelable
