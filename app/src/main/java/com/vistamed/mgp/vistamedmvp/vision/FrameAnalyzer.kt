package com.vistamed.mgp.vistamedmvp.vision

import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class FrameAnalyzer(
    private val detector: Detector,
    private val onDetections: (List<Detection>) -> Unit
) : ImageAnalysis.Analyzer {

    private val busy = AtomicBoolean(false)

    override fun analyze(image: ImageProxy) {
        if (busy.getAndSet(true)) {
            image.close()
            return
        }
        try {
            // Convertimos YUV a NV21 → JPEG → Bitmap → RGB (simple y estable para MVP)
            val nv21 = imageToNV21(image) ?: return
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val baos = ByteArrayOutputStream()
            yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 80, baos)
            val jpeg = baos.toByteArray()
            val bmp = android.graphics.BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: return

            val w = bmp.width
            val h = bmp.height
            val pixels = IntArray(w * h)
            bmp.getPixels(pixels, 0, w, 0, 0, w, h)

            val detections = detector.detectRGB888(w, h, pixels)
            onDetections(detections)
        } catch (_: Exception) {
            // Evitar que un error interrumpa el pipeline
        } finally {
            image.close()
            busy.set(false)
        }
    }

    private fun imageToNV21(image: ImageProxy): ByteArray? {
        if (image.format != ImageFormat.YUV_420_888) return null
        val yBuffer: ByteBuffer = image.planes[0].buffer
        val uBuffer: ByteBuffer = image.planes[1].buffer
        val vBuffer: ByteBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)

        // V y U invertidos (VU) en ImageProxy
        val vBytes = ByteArray(vSize); vBuffer.get(vBytes)
        val uBytes = ByteArray(uSize); uBuffer.get(uBytes)

        System.arraycopy(vBytes, 0, nv21, ySize, vSize)
        System.arraycopy(uBytes, 0, nv21, ySize + vSize, uSize)
        return nv21
    }
}
