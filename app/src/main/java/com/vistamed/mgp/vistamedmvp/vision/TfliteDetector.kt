package com.vistamed.mgp.vistamedmvp.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TfliteDetector(
    private val context: Context,
    private val modelPath: String = "vistamed_yolov8n_float16_nms.tflite",
    private val scoreThreshold: Float = 0.1f,
    private val maxResults: Int = 3
) : Detector {

    private var objectDetector: ObjectDetector? = null

    init {
        setupDetector()
    }

    private fun setupDetector() {
        try {
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(BaseOptions.builder().build())
                .setScoreThreshold(scoreThreshold)
                .setMaxResults(maxResults)
                .build()
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelPath, options)
        } catch (e: Exception) {
            Log.e("TfliteDetector", "Error initializing detector", e)
        }
    }

    override fun detect(bitmap: Bitmap, rotation: Int): List<Detection> {
        if (objectDetector == null) {
            return emptyList()
        }

        // Paso 1: Rotar el bitmap si es necesario
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Paso 2: Redimensionar el bitmap a 640x640 y crear un TensorImage
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 640, 640, true)
        val tensorImage = TensorImage.fromBitmap(resizedBitmap)

        // paso adicional para el log
        val detections = objectDetector?.detect(tensorImage) ?: emptyList()
        Log.d("TfliteDetector", "Detecciones encontradas: ${detections.size}")
        for (detection in detections) {
            Log.d("TfliteDetector", "Clase: ${detection.categories[0].label}, Confianza: ${detection.categories[0].score}")
        }
        return detections

        // Paso 3: Realizar la detección
        return objectDetector?.detect(tensorImage) ?: emptyList()
    }

    override fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}