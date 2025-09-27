package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.Bitmap
//import org.tensorflow.lite.task.vision.detector.Detection

class FakeDetector : Detector {

    // Se implementa la función 'detect' que exige la interfaz
    override fun detect(bitmap: Bitmap, rotation: Int): List<Detection> {
        // Como es un detector "falso", simplemente devuelve una lista vacía.
        return emptyList()
    }

    // Se implementa la función 'close' que también exige la interfaz
    override fun close() {
        // No hace nada, porque no hay recursos que liberar.
    }
}