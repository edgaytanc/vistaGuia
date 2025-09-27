package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.Bitmap

interface Detector {
    fun detect(bitmap: Bitmap, rotation: Int): List<Detection> // Ahora usará nuestra clase Detection
    fun close()
}