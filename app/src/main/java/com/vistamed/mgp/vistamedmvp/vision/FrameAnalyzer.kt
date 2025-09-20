package com.vistamed.mgp.vistamedmvp.vision

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.task.vision.detector.Detection
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class FrameAnalyzer(
    private val detector: Detector,
    private val onResults: (List<Detection>, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val bitmap = convertYuvToBitmap(image)
            if (bitmap != null) {
                val rotation = image.imageInfo.rotationDegrees
                val results = detector.detect(bitmap, rotation)
                onResults(results, image.height, image.width)
            }
        }
        image.close()
    }

    private fun convertYuvToBitmap(image: ImageProxy): Bitmap? {
        val yuvBytes = yuv420ToByteArray(image)
        if (yuvBytes == null) {
            return null
        }
        val yuvImage = YuvImage(
            yuvBytes,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun yuv420ToByteArray(image: ImageProxy): ByteArray? {
        val planes = image.planes
        if (planes.size < 3) return null

        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copiar el plano Y (luminancia)
        yBuffer.get(nv21, 0, ySize)

        // Copiar los planos U y V (croma)
        // La conversión a NV21 requiere que los datos V sigan a los datos U
        var offset = ySize
        for (i in 0 until ySize step 2) {
            nv21[offset++] = vBuffer.get(i)
            if (i < uSize) {
                nv21[offset++] = uBuffer.get(i)
            }
        }
        return nv21
    }
}