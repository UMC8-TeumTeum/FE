package com.example.teumteum.utils

import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.NumberPicker

data class AmPmHourMinuteIndex(
    val ampmValue: Int, // 0=오전, 1=오후
    val hour12: Int,
    val minuteIndex: Int
)

fun NumberPicker.applyPickerValue(
    hourPicker: NumberPicker,
    minutePicker: NumberPicker,
    value: AmPmHourMinuteIndex
) {
    this.value = value.ampmValue
    hourPicker.value = value.hour12
    minutePicker.value = value.minuteIndex
}

fun parseKoreanAmPmTimeToPickerValue(
    timeText: String,
    minuteOptions: Array<String> = arrayOf("00", "10", "20", "30", "40", "50"),
    startPlaceholder: String = "시작 시간",
    endPlaceholder: String = "종료 시간"
): AmPmHourMinuteIndex? {
    val t = timeText.trim()
    if (t.isEmpty() || t == startPlaceholder || t == endPlaceholder) return null

    val parts = t.split(" ")
    if (parts.size < 2) return null

    val ampmStr = parts[0]
    val hm = parts[1].split(":")
    if (hm.size < 2) return null

    val hour12 = hm[0].toIntOrNull() ?: return null
    val minuteStr = hm[1].padStart(2, '0')

    val ampmValue = if (ampmStr == "오후") 1 else 0
    val minuteIndex = minuteOptions.indexOf(minuteStr).takeIf { it >= 0 } ?: 0

    return AmPmHourMinuteIndex(
        ampmValue = ampmValue,
        hour12 = hour12.coerceIn(1, 12),
        minuteIndex = minuteIndex
    )
}

fun parse24hTimeToPickerValue(
    timeText: String,
    minuteOptions: Array<String> = arrayOf("00", "10", "20", "30", "40", "50")
): AmPmHourMinuteIndex? {
    val t = timeText.trim()
    if (t.isEmpty()) return null

    val hm = t.split(":")
    if (hm.size < 2) return null

    val hour24 = hm[0].toIntOrNull() ?: return null
    val minuteStr = hm[1].padStart(2, '0')
    val minuteIndex = minuteOptions.indexOf(minuteStr).takeIf { it >= 0 } ?: 0

    val ampmValue = if (hour24 >= 12) 1 else 0
    var hour12 = hour24 % 12
    if (hour12 == 0) hour12 = 12

    return AmPmHourMinuteIndex(
        ampmValue = ampmValue,
        hour12 = hour12.coerceIn(1, 12),
        minuteIndex = minuteIndex
    )
}

fun NumberPicker.enableTapToNext(
    wrap: Boolean = true,
    onAfterChange: ((newValue: Int) -> Unit)? = null
) {
    // 편집 모드 방지
    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

    post {
        fun goNext() {
            val cur = value
            val min = minValue
            val max = maxValue

            // 현재 값 기준으로 다음 값 계산
            val next = when {
                cur < max -> cur + 1
                wrap -> min
                else -> max
            }

            // 스크롤 액션
            val animated = (next == cur + 1) &&
                    performAccessibilityAction(
                        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,
                        null
                    )

            if (!animated) value = next

            onAfterChange?.invoke(next)
        }

        // 터치 이벤트 1: 1NumberPicker 자체 클릭
        setOnClickListener { goNext() }

        // 터치 이벤트 2: 내부 EditText 클릭
        val editText = (0 until childCount)
            .map { getChildAt(it) }
            .filterIsInstance<EditText>()
            .firstOrNull()

        editText?.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isCursorVisible = false
            setOnClickListener { goNext() }
        }
    }
}