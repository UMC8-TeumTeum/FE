package com.example.teumteum.utils

import android.widget.TextView
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.math.abs

object TimeUtils {

        // yyyy-MM-dd'T'HH:mm[:ss][.SSS...][XXX]
        private val flexibleIsoFormatter: DateTimeFormatter =
                DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm")
                        .optionalStart().appendPattern(":ss").optionalEnd()
                        .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
                        .optionalStart().appendPattern("XXX").optionalEnd() // Z or +09:00
                        .toFormatter(Locale.ROOT)

        fun combineDateTime(
                dateTextView: TextView,
                timeTextView: TextView
        ): String {
                val dateStr = dateTextView.text.toString()
                val timeStr = timeTextView.text.toString()

                val combinedStr = "$dateStr $timeStr"
                val formatter = DateTimeFormatter.ofPattern("M월 d일 '('E')' a h:mm", Locale.KOREAN)
                val parsed = formatter.parse(combinedStr)

                val year = LocalDate.now().year
                val month = parsed.get(ChronoField.MONTH_OF_YEAR)
                val day = parsed.get(ChronoField.DAY_OF_MONTH)
                val time = LocalTime.from(parsed)

                val dateTime = LocalDateTime.of(year, month, day, time.hour, time.minute)
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        }

        /**
         * createdAt -> 상대시간 문자열
         * - 타임존이 없으면 '로컬(zoneId)'로 간주 (KST 환경이면 Asia/Seoul)
         * - 타임존이 있으면 그대로 사용
         * - 소수점 초 지원
         * - 미래 시간(음수 Duration)은 오표시 방지 처리
         */
        fun toRelativeTime(
                isoString: String,
                zoneId: ZoneId = ZoneId.systemDefault()
        ): String {
                return runCatching {
                        val parsed = flexibleIsoFormatter.parse(isoString)
                        val hasOffset = parsed.isSupported(ChronoField.OFFSET_SECONDS)

                        val createdZdt: ZonedDateTime = if (hasOffset) {
                                ZonedDateTime.from(parsed)
                        } else {
                                LocalDateTime.from(parsed).atZone(zoneId)
                        }

                        val createdLocal = createdZdt.withZoneSameInstant(zoneId).toLocalDateTime()
                        val now = LocalDateTime.now(zoneId)

                        var duration = Duration.between(createdLocal, now)
                        if (duration.isNegative) duration = Duration.ZERO  // 미래 튐 보정

                        val minutes = duration.toMinutes()
                        val hours = duration.toHours()
                        val days = duration.toDays()
                        val months = days / 30
                        val years = days / 365

                        when {
                                minutes < 1 -> "방금 전"
                                minutes < 60 -> "${minutes}분 전"
                                hours < 24 -> "${hours}시간 전"
                                days == 1L -> "어제"
                                days < 30 -> "${days}일 전"
                                months < 12 -> "${months}달 전"
                                else -> "${years}년 전"
                        }
                }.getOrElse { "알 수 없음" }
        }

}
