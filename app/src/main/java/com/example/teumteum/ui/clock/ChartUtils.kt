package com.example.teumteum.ui.clock

import android.graphics.Color
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.teumteum.R
import androidx.core.content.ContextCompat
import com.example.teumteum.ui.friend.data.TimeCardItem
import com.example.teumteum.ui.main.data.TimeBlock
import com.example.teumteum.ui.main.data.TimeType
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.collections.mapNotNull

object ChartUtils {

    // PieChart 초기 설정 (공용)
    fun setupPieChart(pieChart: PieChart) {
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.rotationAngle = -90f
        pieChart.isRotationEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.setTouchEnabled(false)
        pieChart.minOffset = 0f
        pieChart.setExtraOffsets(0f, 0f, 0f, 0f)
        pieChart.setBackgroundColor(Color.TRANSPARENT)
    }

    // AM/PM 분리 + EMPTY 채우기 (공용)
    fun splitAndFillTimeBlocks(allBlocks: List<TimeBlock>, isAM: Boolean): List<TimeBlock> {
        val startMinute = if (isAM) 0 else 720
        val endMinute = if (isAM) 720 else 1440

        val filtered = allBlocks.mapNotNull { block ->
            val s = block.startTime.coerceIn(startMinute, endMinute)
            val e = block.endTime.coerceIn(startMinute, endMinute)
            if (s < e) TimeBlock(s, e, block.type) else null
        }.sortedBy { it.startTime }

        val result = mutableListOf<TimeBlock>()
        var cursor = startMinute

        fun addSafeBlock(start: Int, end: Int, type: TimeType) {
            if (start < end) result.add(TimeBlock(start, end, type))
        }

        for (block in filtered) {
            if (result.isNotEmpty()) {
                val last = result.last()

                // 겹치는 경우
                if (block.startTime < last.endTime) {
                    val overlapStart = block.startTime
                    val overlapEnd = maxOf(last.endTime, block.endTime) //endTime 병합

                    // 우선순위: TODO > SLEEP > EMPTY
                    val priorityType = when {
                        block.type == TimeType.TODO || last.type == TimeType.TODO -> TimeType.TODO
                        block.type == TimeType.SLEEP || last.type == TimeType.SLEEP -> TimeType.SLEEP
                        else -> TimeType.EMPTY
                    }

                    // 마지막 블록을 우선순위 블록으로 교체 (하나로 병합)
                    result[result.lastIndex] = TimeBlock(last.startTime, overlapEnd, priorityType)
                    cursor = overlapEnd
                    continue
                }
            }

            // 빈틈 EMPTY
            if (block.startTime > cursor) {
                addSafeBlock(cursor, block.startTime, TimeType.EMPTY)
            }

            result.add(block)
            cursor = block.endTime
        }

        if (cursor < endMinute) addSafeBlock(cursor, endMinute, TimeType.EMPTY)

        return result.filter { it.startTime < it.endTime }
    }


    //실제 그래프에 넣을 데이터로 변환/ overrideColor는 FriendRoommateTimeFragment 색 통일
    fun setTimePieChartData(context: Context, pieChart: PieChart, timeBlocks: List<TimeBlock>, overrideColor: Int? = null) {
        val entries = timeBlocks.map {
            val duration = (it.endTime - it.startTime).toFloat() / 10f
            val label = when (it.type) {
                TimeType.SLEEP -> "수면"
                TimeType.TODO -> "일정"
                TimeType.EMPTY -> "빈틈"
            }
            PieEntry(duration, label)
        }

        val colors = timeBlocks.map {
            when (it.type) {
                TimeType.EMPTY -> ContextCompat.getColor(context, R.color.clock_teum)  // EMPTY는 무조건 고정
                TimeType.SLEEP -> overrideColor ?: ContextCompat.getColor(context, R.color.clock_sleep)
                TimeType.TODO -> overrideColor ?: ContextCompat.getColor(context, R.color.clock_todo)
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 0f
            selectionShift = 0f
        }

        val data = PieData(dataSet).apply { setDrawValues(false) }
        pieChart.clear()
        pieChart.data = data
        pieChart.data.notifyDataChanged()
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
    }

    //아이콘 가져오기
    fun getBitmapFromVector(context: Context, vectorResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, vectorResId)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun String.toMinuteOfDay(): Int {
        val parts = split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        // "24:00"은 하루 끝(1440분)으로 허용
        if (h == 24 && m == 0) return 1440

        // 일반 유효 범위
        val hour = h.coerceIn(0, 23)
        val minute = m.coerceIn(0, 59)
        return hour * 60 + minute
    }

    //가능한 시간 데이터(TimeCard)를 TimeBlock으로 변환
    fun buildBlocksFromTimeCardItems(cards: List<TimeCardItem>): List<TimeBlock> {
        if (cards.isEmpty()) {
            // 카드가 없으면 하루 종일 EVENT 처리
            return listOf(TimeBlock(0, 1440, TimeType.TODO))
        }

        // 문자열을 분으로 파싱하고 [0,1440] 기준으로 펼치기
        val emptySegments = mutableListOf<Pair<Int, Int>>() // (start, end) in minutes
        for (c in cards) {
            val s = c.startTime.toMinuteOfDay()
            val e = c.endTime.toMinuteOfDay()
            if (s == e) continue // 길이 0은 무시

            if (s < e) {
                // 일반 구간
                emptySegments += s to e
            } else {
                // 자정 넘김 구간: [s, 1440) + [0, e]
                emptySegments += s to 1440
                emptySegments += 0 to e
            }
        }
        if (emptySegments.isEmpty()) {
            return listOf(TimeBlock(0, 1440, TimeType.TODO))
        }

        // 2) EMPTY 구간 병합 (겹치거나 인접한 것도 하나로)
        emptySegments.sortBy { it.first }
        val mergedEmpty = mutableListOf<Pair<Int, Int>>()
        var curStart = emptySegments[0].first
        var curEnd = emptySegments[0].second
        for (i in 1 until emptySegments.size) {
            val (s, e) = emptySegments[i]
            if (s <= curEnd) {
                curEnd = maxOf(curEnd, e)
            } else if (s == curEnd) {
                // 인접: [a,b] + [b,c] -> [a,c]
                curEnd = e
            } else {
                mergedEmpty += curStart to curEnd
                curStart = s
                curEnd = e
            }
        }
        mergedEmpty += curStart to curEnd

        // 비어있는 구간은 투두로 채우기
        val result = mutableListOf<TimeBlock>()
        var cursor = 0
        fun addBlock(start: Int, end: Int, type: TimeType) {
            if (start < end) result += TimeBlock(start, end, type)
        }

        for ((s, e) in mergedEmpty) {
            addBlock(cursor, s, TimeType.TODO)
            addBlock(s, e, TimeType.EMPTY)
            cursor = e
        }
        addBlock(cursor, 1440, TimeType.TODO)

        return result
    }

}