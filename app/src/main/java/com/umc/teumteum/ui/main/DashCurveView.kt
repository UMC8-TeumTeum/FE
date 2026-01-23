package com.umc.teumteum.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.PathParser
import com.umc.teumteum.R

class DashCurveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var pathData: String = ""
    private var viewportW: Float = 0f
    private var viewportH: Float = 0f

    private var strokeWidthPx: Float = dp(1.2f)
    private var strokeColor: Int = ContextCompat.getColor(context, R.color.teumteum_gray)
    private var strokeAlpha: Float = 0.8f
    private var dashLengthPx: Float = dp(3f)
    private var dashGapPx: Float = dp(4f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var rawPath: Path? = null
    private val drawPath = Path()
    private val matrix = Matrix()

    init {
        context.withStyledAttributes(attrs, R.styleable.DashCurveView) {
            pathData = getString(R.styleable.DashCurveView_pathData).orEmpty()
            viewportW = getFloat(R.styleable.DashCurveView_viewportWidth, 0f)
            viewportH = getFloat(R.styleable.DashCurveView_viewportHeight, 0f)

            strokeWidthPx = getDimension(R.styleable.DashCurveView_strokeWidth, strokeWidthPx)
            strokeColor = getColor(R.styleable.DashCurveView_strokeColor, strokeColor)
            strokeAlpha = getFloat(R.styleable.DashCurveView_strokeAlpha, strokeAlpha)

            dashLengthPx = getDimension(R.styleable.DashCurveView_dashLength, dashLengthPx)
            dashGapPx = getDimension(R.styleable.DashCurveView_dashGap, dashGapPx)
        }

        rawPath = if (pathData.isNotBlank()) PathParser.createPathFromPathData(pathData) else null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildPath(w, h)
    }

    private fun rebuildPath(w: Int, h: Int) {
        val p = rawPath ?: return
        if (viewportW <= 0f || viewportH <= 0f) return

        drawPath.reset()
        drawPath.set(p)

        val sx = w / viewportW
        val sy = h / viewportH
        matrix.reset()
        matrix.setScale(sx, sy)
        drawPath.transform(matrix)

        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = strokeColor
        paint.alpha = (strokeAlpha * 255).toInt().coerceIn(0, 255)
        paint.strokeWidth = strokeWidthPx
        paint.pathEffect = DashPathEffect(floatArrayOf(dashLengthPx, dashGapPx), 0f)

        canvas.drawPath(drawPath, paint)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}