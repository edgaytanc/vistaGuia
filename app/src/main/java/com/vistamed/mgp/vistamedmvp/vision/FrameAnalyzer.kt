package com.vistamed.mgp.vistamedmvp.vision

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.task.vision.detector.Detection

class FrameAnalyzer(
    private val detector: Detector,
    private val onResults: (List<Detection>, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private var bitmap: Bitmap? = null

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            if (bitmap == null || bitmap!!.width != image.width || bitmap!!.height != image.height) {
                bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            }

            // Aquí necesitarás una función para convertir el formato YUV_420_888 a Bitmap
            // Esta es una implementación común, pero puede que necesites una librería o una función más optimizada
            bitmap?.copyPixelsFromBuffer(image.planes[0].buffer)

            val rotation = image.imageInfo.rotationDegrees
            val results = detector.detect(bitmap!!, rotation)

            onResults(results, image.height, image.width)
        }
        image.close()
    }
}