package com.example.teumteum.utils

data class AmPmHourMinuteIndex(
    val ampmValue: Int,   // 0=오전, 1=오후
    val hour12: Int,
    val minuteIndex: Int
)

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