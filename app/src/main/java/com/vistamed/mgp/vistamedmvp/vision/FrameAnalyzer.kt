package com.vistamed.mgp.vistamedmvp.vision

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FrameAnalyzer(
    private val detector: Detector,
    private val onDetections: (detections: List<Detection>, imageHeight: Int, imageWidth: Int) -> Unit
) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        if (bitmap != null) {
            val detections = detector.detect(bitmap, imageProxy.imageInfo.rotationDegrees)
            onDetections(detections, bitmap.height, bitmap.width)
        }
        imageProxy.close()
    }
}