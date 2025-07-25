package com.example.teumteum.ui.clock

import android.graphics.Bitmap
import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.cos
import kotlin.math.sin

class IconPieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val sleepIcon: Bitmap
) : PieChartRenderer(chart, animator, viewPortHandler) {

    override fun drawValues(c: Canvas) {
        super.drawValues(c)

        val dataSet = mChart.data.dataSets[0]
        val center = mChart.centerCircleBox
        val radius = mChart.radius * 0.5f

        var angle = 0f
        val rotation = mChart.rotationAngle
        val total = mChart.data.yValueSum

        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i) as PieEntry
            val sliceAngle = entry.value / total * 360f
            val angleMiddle = angle + sliceAngle / 2f

            if (entry.label == "수면") {
                val x = center.x + radius * cos(Math.toRadians((angleMiddle + rotation).toDouble())).toFloat()
                val y = center.y + radius * sin(Math.toRadians((angleMiddle + rotation).toDouble())).toFloat()

                val halfW = sleepIcon.width / 2f
                val halfH = sleepIcon.height / 2f

                val paint = android.graphics.Paint().apply {
                    alpha = 77   // 30% 투명도 (255 * 0.3)
                }

                c.drawBitmap(sleepIcon, x - halfW, y - halfH, paint)
            }

            angle += sliceAngle
        }
    }
}