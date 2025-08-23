package com.vistamed.mgp.vistamedmvp.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions

/**
 * Detector real usando TensorFlow Lite Task Vision.
 * - Carga el modelo desde assets: "meds_detector.tflite".
 * - detectRGB888 recibe pixeles ARGB_8888 (el canal A será ignorado).
 */
class TfliteDetector(ctx: Context) : Detector {

    private val detector: ObjectDetector? = try {
        val base = BaseOptions.builder()
            .setNumThreads(2)       // Ajusta según dispositivo
            .build()

        val options = ObjectDetectorOptions.builder()
            .setBaseOptions(base)
            .setMaxResults(3)       // Top-K
            .setScoreThreshold(0.50f) // Umbral de confianza
            .build()

        // El archivo debe existir en app/src/main/assets/meds_detector.tflite
        ObjectDetector.createFromFileAndOptions(ctx, "meds_detector.tflite", options)
    } catch (_: Exception) {
        null
    }

    override fun detectRGB888(width: Int, height: Int, pixels: IntArray): List<Detection> {
        // Construimos un Bitmap a partir del buffer de pixeles
        val bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        // TensorImage correcto (de la support library)
        val tensor = TensorImage.fromBitmap(bmp)

        val results = detector?.detect(tensor) ?: return emptyList()
        if (results.isEmpty()) return emptyList()

        return results.map { det ->
            val cat = det.categories.firstOrNull()
            Detection(
                label = cat?.label ?: "objeto",
                score = cat?.score ?: 0f,
                box = RectF(
                    det.boundingBox.left,
                    det.boundingBox.top,
                    det.boundingBox.right,
                    det.boundingBox.bottom
                )
            )
        }
    }

    /** Permite saber si el modelo cargó correctamente. */
    fun isLoaded(): Boolean = detector != null
}
