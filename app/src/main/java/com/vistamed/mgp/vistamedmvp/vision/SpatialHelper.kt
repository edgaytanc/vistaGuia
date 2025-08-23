package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.RectF

object SpatialHelper {
    /**
     * Devuelve "izquierda", "centro" o "derecha" según la caja.
     * @param frameWidth ancho en px del frame ya rotado (el que pasas al detector).
     */
    fun horizontalZone(frameWidth: Int, box: RectF?): String {
        if (box == null) return "centro"
        val centerX = (box.left + box.right) / 2f
        val third = frameWidth / 3f
        return when {
            centerX < third -> "izquierda"
            centerX > 2 * third -> "derecha"
            else -> "centro"
        }
    }
}
