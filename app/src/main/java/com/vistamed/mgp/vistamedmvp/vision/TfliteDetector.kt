package com.vistamed.mgp.vistamedmvp.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class TfliteDetector(
    private val context: Context,
    private val modelPath: String = "vistamed_yolov8n_float16_nms.tflite",
    private val scoreThreshold: Float = 0.5f,
    private val maxResults: Int = 3
) : Detector {

    private var objectDetector: ObjectDetector? = null

    init {
        setupDetector()
    }

    private fun setupDetector() {
        try {
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(BaseOptions.builder().useNnapi().build())
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

        val imageProcessor = ImageProcessor.Builder()
            .add(Rot90Op(-rotation / 90))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        return objectDetector?.detect(tensorImage) ?: emptyList()
    }

    override fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}