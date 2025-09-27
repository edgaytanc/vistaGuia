package com.vistamed.mgp.vistamedmvp.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.vistamed.mgp.vistamedmvp.vision.Detection
import kotlin.math.max

class OverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var results: List<Detection> = emptyList()
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val textBackgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 50f
    }

    fun setResults(detections: List<Detection>, imageHeight: Int, imageWidth: Int) {
        results = detections
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (results.isEmpty()) return

        val scaleFactor = max(width.toFloat() / imageWidth, height.toFloat() / imageHeight)

        for (result in results) {
            val boundingBox = result.boundingBox
            val score = result.categories.first().score
            val label = result.categories.first().label

            val scaledBoundingBox = RectF(
                boundingBox.left * imageWidth * scaleFactor,
                boundingBox.top * imageHeight * scaleFactor,
                boundingBox.right * imageWidth * scaleFactor,
                boundingBox.bottom * imageHeight * scaleFactor
            )

            canvas.drawRect(scaledBoundingBox, boxPaint)
            val drawableText = "$label ${String.format("%.2f", score)}"
            val textBounds = Rect()
            textPaint.getTextBounds(drawableText, 0, drawableText.length, textBounds)
            val textWidth = textBounds.width()
            val textHeight = textBounds.height()

            canvas.drawRect(
                scaledBoundingBox.left,
                scaledBoundingBox.top - textHeight - 8f,
                scaledBoundingBox.left + textWidth + 8f,
                scaledBoundingBox.top,
                textBackgroundPaint
            )
            canvas.drawText(
                drawableText,
                scaledBoundingBox.left + 4f,
                scaledBoundingBox.top - 4f,
                textPaint
            )
        }
    }
}