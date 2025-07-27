package com.example.teumteum.utils

import android.graphics.*
import android.graphics.Bitmap.createBitmap
import android.view.View
import android.widget.ImageView

fun applyBlurShadow(
    sourceView: View,
    targetImageView: ImageView,
    blurRadius: Float = 6.2f,
    alphaRatio: Float = 0.18f,
    offsetX: Float = 0f,
    offsetY: Float = 4f
) {
    // 소프트웨어 레이어로 설정
    sourceView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

    val bitmap = createBitmap(sourceView.width, sourceView.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    sourceView.draw(canvas)

    val shadowPaint = Paint().apply {
        color = Color.BLACK
        maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        alpha = (255 * alphaRatio).toInt()
    }

    // 그림자 비트맵 생성
    val offset = IntArray(2)
    val shadowBitmap = bitmap.extractAlpha(shadowPaint, offset)

    // 그림자 적용 대상 ImageView에 설정
    targetImageView.setImageBitmap(shadowBitmap)
    targetImageView.translationX = offsetX
    targetImageView.translationY = offsetY
}