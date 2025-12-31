package com.example.teumteum.utils

import android.os.SystemClock
import android.view.View

fun View.setOnSingleClickListener(
    interval: Long = 600L,
    onClick: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < interval) return@setOnClickListener
        lastClickTime = now
        onClick(v)
    }
}

fun View.dpToPx(dp: Int): Int =
    (dp * resources.displayMetrics.density).toInt()