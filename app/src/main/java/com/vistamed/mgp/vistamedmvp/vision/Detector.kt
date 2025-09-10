package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.Bitmap
import org.tensorflow.lite.task.vision.detector.Detection

interface Detector {
    fun detect(bitmap: Bitmap, rotation: Int): List<Detection>
    fun close()
}
