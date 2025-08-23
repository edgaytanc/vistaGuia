package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.RectF

data class Detection(val label: String, val score: Float, val box: RectF?)

interface Detector {
    /**
     * Recibe imagen en RGB888 (width x height, int ARGB).
     * Retorna lista de detecciones (vacía si no hay hallazgos).
     */
    fun detectRGB888(width: Int, height: Int, pixels: IntArray): List<Detection>
}
