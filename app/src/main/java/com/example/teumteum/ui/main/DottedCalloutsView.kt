package com.example.teumteum.ui.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// 튜토리얼 콜아웃(곡선 점선) 정의
data class Callout(
    var startX: Float,                 // 라벨/설명 텍스트 근처 시작 좌표
    var startY: Float,
    var endAngleDeg: Float? = null,    // 차트 둘레로 연결할 각도
    var endX: Float? = null,           // 또는 절대 좌표로 직접 지정
    var endY: Float? = null,
    var curveOffsetDp: Float = 36f,    // 곡률
    var dashOnDp: Float = 3f,          // 대시 길이
    var dashOffDp: Float = 3f,         // 대시 간격
    var color: Int = Color.WHITE,      // 선/점 색
    var alphaPercent: Int = 80,        // 불투명도 퍼센트(0~100)
    var endDotRadiusDp: Float = 3f     // 끝 점(원) 반지름
)

// 여러 개의 곡선 점선을 그려주는 뷰
class DottedCalloutsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(1.5f)
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // 차트 중심/반지름
    var cx = 0f
    var cy = 0f
    var r = 0f

    private val items = mutableListOf<Callout>()

    // 차트 중심/반지름 직접 지정
    fun setCircle(centerX: Float, centerY: Float, radius: Float) {
        cx = centerX; cy = centerY; r = radius
        invalidate()
    }

    // 차트 중심/반지름을 자동 산정.
    fun setCircleFrom(chartView: View, insetDp: Float = 8f) {
        val thisLoc = IntArray(2)
        val chartLoc = IntArray(2)
        getLocationOnScreen(thisLoc)
        chartView.getLocationOnScreen(chartLoc)

        val relLeft = (chartLoc[0] - thisLoc[0]).toFloat()
        val relTop  = (chartLoc[1] - thisLoc[1]).toFloat()
        val w = chartView.width.toFloat()
        val h = chartView.height.toFloat()

        cx = relLeft + w / 2f
        cy = relTop  + h / 2f
        r  = min(w, h) / 2f - dp(insetDp)
        invalidate()
    }

    fun setCallouts(list: List<Callout>) {
        items.clear(); items.addAll(list)
        invalidate()
    }

    fun addCallout(c: Callout) { items.add(c); invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (c in items) {
            // 끝점 계산(각도 or 좌표)
            val (ex, ey) = if (c.endAngleDeg != null) {
                val ang = Math.toRadians(c.endAngleDeg!!.toDouble())
                val x = cx + r * cos(ang).toFloat()
                val y = cy + r * sin(ang).toFloat()
                x to y
            } else {
                // 좌표 직접 지정
                val x = requireNotNull(c.endX) { "Callout.endX is null" }
                val y = requireNotNull(c.endY) { "Callout.endY is null" }
                x to y
            }

            // 컨트롤 포인트
            val baseAngle = (c.endAngleDeg ?: -90f) - 90f
            val nx = cos(Math.toRadians(baseAngle.toDouble())).toFloat()
            val ny = sin(Math.toRadians(baseAngle.toDouble())).toFloat()
            val off = dp(c.curveOffsetDp)
            val cpx = (c.startX + ex) / 2f + nx * off
            val cpy = (c.startY + ey) / 2f + ny * off

            // 페인트 설정
            paint.color = c.color
            paint.alpha = (c.alphaPercent.coerceIn(0, 100) / 100f * 255).toInt()
            paint.pathEffect = DashPathEffect(floatArrayOf(dp(c.dashOnDp), dp(c.dashOffDp)), 0f)

            dotPaint.color = c.color
            dotPaint.alpha = paint.alpha

            // 곡선 + 끝 점
            path.reset()
            path.moveTo(c.startX, c.startY)
            path.quadTo(cpx, cpy, ex, ey)
            canvas.drawPath(path, paint)
            canvas.drawCircle(ex, ey, dp(c.endDotRadiusDp), dotPaint)

        }
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
}
