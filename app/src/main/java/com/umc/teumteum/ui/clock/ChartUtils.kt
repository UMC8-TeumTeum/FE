package com.umc.teumteum.ui.clock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.umc.teumteum.R
import com.umc.teumteum.ui.friend.data.TimeCardItem
import com.umc.teumteum.ui.main.data.TimeBlock
import com.umc.teumteum.ui.main.data.TimeType
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

object ChartUtils {

    // PieChart 초기 설정
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

    // AM/PM 분리 + EMPTY 채우기
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

                // 겹침 처리
                if (block.startTime < last.endTime) {
                    val overlapStart = block.startTime
                    val overlapEnd = maxOf(last.endTime, block.endTime)

                    val priorityType = when {
                        block.type == TimeType.TODO || last.type == TimeType.TODO -> TimeType.TODO
                        block.type == TimeType.SLEEP || last.type == TimeType.SLEEP -> TimeType.SLEEP
                        else -> TimeType.EMPTY
                    }

                    result[result.lastIndex] = TimeBlock(last.startTime, overlapEnd, priorityType)
                    cursor = overlapEnd
                    continue
                }
            }

            if (block.startTime > cursor) addSafeBlock(cursor, block.startTime, TimeType.EMPTY)

            result.add(block)
            cursor = block.endTime
        }

        if (cursor < endMinute) addSafeBlock(cursor, endMinute, TimeType.EMPTY)

        return result.filter { it.startTime < it.endTime }
    }

    fun detectOverlapBlocks(blocks: List<TimeBlock>): List<TimeBlock> {
        val result = mutableListOf<TimeBlock>()

        for (i in blocks.indices) {
            val a = blocks[i]

            for (j in i + 1 until blocks.size) {
                val b = blocks[j]

                val check = (a.type == TimeType.SLEEP && b.type == TimeType.TODO) ||
                        (a.type == TimeType.TODO && b.type == TimeType.SLEEP)

                if (!check) continue

                val overlapStart = maxOf(a.startTime, b.startTime)
                val overlapEnd = minOf(a.endTime, b.endTime)

                if (overlapStart < overlapEnd) {
                    result += TimeBlock(overlapStart, overlapEnd, TimeType.OVERLAP)
                }
            }
        }

        return result
    }

    fun mergeWithOverlap(
        baseBlocks: List<TimeBlock>,
        overlapBlocks: List<TimeBlock>
    ): List<TimeBlock> {

        val all = (baseBlocks + overlapBlocks).sortedBy { it.startTime }
        val result = mutableListOf<TimeBlock>()

        for (b in all) {

            if (result.isEmpty()) {
                result += b
                continue
            }

            val last = result.last()

            // 겹침 없음
            if (b.startTime >= last.endTime) {
                result += b
                continue
            }

            // 겹침 있음 → 구간 3개로 쪼갬
            result.removeAt(result.lastIndex)

            // 앞 부분
            if (last.startTime < b.startTime)
                result += TimeBlock(last.startTime, b.startTime, last.type)

            // 가운데(겹침)
            val overlapStart = maxOf(last.startTime, b.startTime)
            val overlapEnd = minOf(last.endTime, b.endTime)

            val midType = when {
                last.type == TimeType.OVERLAP || b.type == TimeType.OVERLAP -> TimeType.OVERLAP
                last.type != b.type -> TimeType.OVERLAP
                else -> last.type
            }
            result += TimeBlock(overlapStart, overlapEnd, midType)

            // 뒤 부분
            val endMax = maxOf(last.endTime, b.endTime)
            if (overlapEnd < endMax) {
                val tailType =
                    if (last.endTime > b.endTime) last.type else b.type
                result += TimeBlock(overlapEnd, endMax, tailType)
            }
        }

        return result
    }

    // PieChart 데이터 설정
    fun setTimePieChartData(
        context: Context,
        pieChart: PieChart,
        timeBlocks: List<TimeBlock>,
        overrideColor: Int? = null
    ) {

        android.util.Log.d("ClockChart", "=== 최종 차트 블록 ===")
        timeBlocks.forEach {
            android.util.Log.d(
                "ClockChart",
                "• ${(it.startTime)} ~ ${(it.endTime)} | type=${it.type}"
            )
        }
        android.util.Log.d("ClockChart", "======================")

        val entries = timeBlocks.map {
            val duration = (it.endTime - it.startTime).toFloat() / 10f
            val label = when (it.type) {
                TimeType.SLEEP -> "수면"
                TimeType.TODO -> "일정"
                TimeType.EMPTY -> "빈틈"
                TimeType.OVERLAP -> "겹침"
            }
            PieEntry(duration, label)
        }

        val colors = timeBlocks.map {
            when (it.type) {
                TimeType.EMPTY -> ContextCompat.getColor(context, R.color.clock_teum)
                TimeType.SLEEP -> overrideColor ?: ContextCompat.getColor(context, R.color.clock_sleep)
                TimeType.TODO -> overrideColor ?: ContextCompat.getColor(context, R.color.clock_todo)
                TimeType.OVERLAP -> ContextCompat.getColor(context, R.color.clock_overlap)
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

    // Drawable → Bitmap 변환
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

        if (h == 24 && m == 0) return 1440

        val hour = h.coerceIn(0, 23)
        val minute = m.coerceIn(0, 59)
        return hour * 60 + minute
    }

    // TimeCardItem → TimeBlock 변환
    fun buildBlocksFromTimeCardItems(cards: List<TimeCardItem>): List<TimeBlock> {
        if (cards.isEmpty()) {
            return listOf(TimeBlock(0, 1440, TimeType.TODO))
        }

        val emptySegments = mutableListOf<Pair<Int, Int>>()
        for (c in cards) {
            val s = c.startTime.toMinuteOfDay()
            val e = c.endTime.toMinuteOfDay()
            if (s == e) continue

            if (s < e) {
                emptySegments += s to e
            } else {
                emptySegments += s to 1440
                emptySegments += 0 to e
            }
        }

        if (emptySegments.isEmpty()) return listOf(TimeBlock(0, 1440, TimeType.TODO))

        emptySegments.sortBy { it.first }
        val mergedEmpty = mutableListOf<Pair<Int, Int>>()
        var curStart = emptySegments[0].first
        var curEnd = emptySegments[0].second

        for (i in 1 until emptySegments.size) {
            val (s, e) = emptySegments[i]
            if (s <= curEnd) {
                curEnd = maxOf(curEnd, e)
            } else {
                mergedEmpty += curStart to curEnd
                curStart = s
                curEnd = e
            }
        }
        mergedEmpty += curStart to curEnd

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

    fun buildBlocksFromSleepTodo(
        sleepBlocks: List<TimeBlock>,
        todoBlocks: List<TimeBlock>,
        isAM: Boolean
    ): List<TimeBlock> {

        // 1) 두 리스트를 하나로 합치기
        val baseBlocks = (sleepBlocks + todoBlocks)
            .sortedBy { it.startTime }

        // 2) SLEEP ↔ TODO 겹침 계산
        val overlapBlocks = detectOverlapBlocks(baseBlocks)

        // 3) 겹침 블록 적용하여 최종 병합
        val merged = mergeWithOverlap(baseBlocks, overlapBlocks)

        // 4) AM/PM 분리 및 EMPTY 채우기
        val half = splitAndFillTimeBlocks(merged, isAM)

        return half
    }

}
