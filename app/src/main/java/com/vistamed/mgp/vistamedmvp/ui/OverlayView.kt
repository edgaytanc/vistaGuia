package com.vistamed.mgp.vistamedmvp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.vistamed.mgp.vistamedmvp.vision.Detection
import kotlin.math.min

// (Importaciones de la clase Detection)

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var detections: List<Detection> = listOf()
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val textBackgroundPaint = Paint().apply {
        // =======================================================
        // SUGERENCIA 2: Fondo sólido (Negro con 80% opacidad)
        // =======================================================
        color = Color.BLACK
        alpha = 204 // Aprox 80%
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = 50f
    }

    fun setResults(detections: List<Detection>, imageHeight: Int, imageWidth: Int) {
        this.detections = detections
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calcular el factor de escala
        scaleFactor = min(width / imageWidth.toFloat(), height / imageHeight.toFloat())

        for (detection in detections) {
            val boundingBox = detection.boundingBox
            val category = detection.categories.firstOrNull() ?: continue

            // 1. Ajustar coordenadas del BoundingBox (Coordenadas Normalizadas)
            // Las coordenadas ya vienen de 0.0 a 1.0, las escalamos a la vista
            val left = boundingBox.left * width
            val top = boundingBox.top * height
            val right = boundingBox.right * width
            val bottom = boundingBox.bottom * height
            val scaledBox = RectF(left, top, right, bottom)

            // 2. Dibujar el BoundingBox
            canvas.drawRect(scaledBox, boxPaint)

            // =======================================================
            // SUGERENCIA 2: Mostrar solo la etiqueta
            // =======================================================
            val textToDisplay = category.label // <-- SOLO LA ETIQUETA

            // 3. Dibujar fondo y texto
            val textBounds = RectF()
            textPaint.getTextBounds(textToDisplay, 0, textToDisplay.length, textBounds.toAndroidRect())
            val textWidth = textBounds.width()
            val textHeight = textBounds.height()

            // Ajusta el fondo del texto
            textBounds.set(
                scaledBox.left,
                scaledBox.top - textHeight - 10,
                scaledBox.left + textWidth + 20,
                scaledBox.top
            )
            canvas.drawRect(textBounds, textBackgroundPaint)

            // Dibuja el texto
            canvas.drawText(
                textToDisplay,
                scaledBox.left + 10,
                scaledBox.top - 10,
                textPaint
            )
        }
    }

    // Extensión para convertir RectF a Rect (necesario para getTextBounds)
    private fun RectF.toAndroidRect(): android.graphics.Rect {
        return android.graphics.Rect(
            this.left.toInt(),
            this.top.toInt(),
            this.right.toInt(),
            this.bottom.toInt()
        )
    }
}