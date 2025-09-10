package com.vistamed.mgp.vistamedmvp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.vistamed.mgp.vistamedmvp.R
import org.tensorflow.lite.task.vision.detector.Detection
import kotlin.math.max

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<Detection> = listOf()
    private val boxPaint = Paint()
    private val textBackgroundPaint = Paint()
    private val textPaint = Paint()

    private var scaleFactor: Float = 1f

    init {
        initPaints()
    }

    private fun initPaints() {
        boxPaint.color = Color.BLACK // Puedes definir este color en colors.xml
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 8f

        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        for (result in results) {
            val boundingBox = result.boundingBox
            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            // Dibuja el rectángulo
            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

            // Dibuja la etiqueta con el texto
            val drawableText = "${result.categories[0].label} " +
                    String.format("%.2f", result.categories[0].score)

            val textWidth = textPaint.measureText(drawableText)
            val textHeight = textPaint.descent() - textPaint.ascent()
            val textBackgroundRect = RectF(left, top, left + textWidth + 8, top + textHeight)
            canvas.drawRect(textBackgroundRect, textBackgroundPaint)
            canvas.drawText(drawableText, left + 4, top + textHeight - textPaint.descent(), textPaint)
        }
    }

    fun setResults(
        detectionResults: List<Detection>,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        results = detectionResults
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
        invalidate() // Redibuja la vista
    }

    fun clear() {
        results = listOf()
        invalidate()
    }
}