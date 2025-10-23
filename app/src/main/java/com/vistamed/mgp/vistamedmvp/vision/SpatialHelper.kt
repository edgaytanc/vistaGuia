package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.RectF

object SpatialHelper {
    /**
     * Devuelve "izquierda", "centro" o "derecha" según la caja.
     * ¡Esta función ha sido corregida para trabajar con coordenadas normalizadas (0.0 a 1.0)!
     * @param box El cuadro delimitador (boundingBox) con coordenadas normalizadas del detector.
     */
    fun horizontalZone(box: RectF?): String {
        // Ya no se necesita frameWidth, por lo que se elimina de los parámetros.
        if (box == null) return "centro"

        // El centro de la caja también estará en coordenadas normalizadas (0.0 a 1.0).
        val centerX = (box.left + box.right) / 2f

        // Comparamos contra los tercios de la pantalla en formato normalizado (1/3 y 2/3).
        return when {
            centerX < 1f / 3f -> "izquierda"
            centerX > 2f / 3f -> "derecha"
            else -> "centro"
        }
    }
}