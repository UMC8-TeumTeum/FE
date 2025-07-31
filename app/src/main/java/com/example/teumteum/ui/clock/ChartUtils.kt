package com.example.teumteum.ui.clock

import android.graphics.Color
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.teumteum.R
import androidx.core.content.ContextCompat
import com.example.teumteum.data.TimeBlock
import com.example.teumteum.data.TimeType
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

        //AM,PM 필터
        val filtered = allBlocks.mapNotNull { block ->
            val s = block.startTime.coerceIn(startMinute, endMinute)
            val e = block.endTime.coerceIn(startMinute, endMinute)
            if (s < e) TimeBlock(s, e, block.type) else null
        }.sortedBy { it.startTime }

        val result = mutableListOf<TimeBlock>()
        var cursor = startMinute
        for (block in filtered) {
            if (result.isNotEmpty()) {
                val last = result.last()

                // 중복된 일정 병합
                if (last.type == block.type && block.startTime <= last.endTime) {
                    val merged = TimeBlock(last.startTime, maxOf(last.endTime, block.endTime), last.type)
                    result[result.lastIndex] = merged
                    cursor = merged.endTime
                    continue
                }
            }

            //비어 있는 일정 EMPTY로 채우기
            if (block.startTime > cursor) result.add(TimeBlock(cursor, block.startTime, TimeType.EMPTY))

            result.add(block)
            cursor = block.endTime
        }

        //마지막 빈틈 EMPTY로 채우기
        if (cursor < endMinute) result.add(TimeBlock(cursor, endMinute, TimeType.EMPTY))

        return result
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
        pieChart.data = data
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

}