package com.vistamed.mgp.vistamedmvp.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp // Volvemos a necesitar este import
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

// ===================================================================
// Estas son nuestras clases personalizadas, no las borres.
data class Category(val label: String, val score: Float)
data class Detection(val boundingBox: RectF, val categories: List<Category>)
// ===================================================================

class TfliteDetector(
    context: Context,
    private val modelPath: String = "vistamed_yolov8n_float16_nms.tflite",
    private val scoreThreshold: Float = 0.3f
) : Detector {

    private val interpreter: Interpreter
    private val imageWidth: Int
    private val imageHeight: Int
    private val TAG = "TfliteDetector"

    init {
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, Interpreter.Options().apply { numThreads = 4 })
        val inputShape = interpreter.getInputTensor(0).shape()
        imageHeight = inputShape[1]
        imageWidth = inputShape[2]
        Log.d(TAG, "✅ Intérprete inicializado para imágenes de ${imageWidth}x${imageHeight}")
    }

    override fun detect(bitmap: Bitmap, rotation: Int): List<Detection> {
        Log.d(TAG, "➡️ Recibido nuevo frame para detectar. Tamaño: ${bitmap.width}x${bitmap.height}")

        // --- LA CORRECCIÓN FINAL: USAR EL PROCESADOR OFICIAL ---
        // Este método es el estándar y garantiza la correcta normalización de píxeles.
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imageHeight, imageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f)) // Normaliza los píxeles al rango [0, 1]
            .build()

        // Creamos un TensorImage y lo procesamos.
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        // El 'imageBuffer' ahora contiene la imagen perfectamente formateada.
        val imageBuffer = tensorImage.buffer
        // --- FIN DE LA CORRECCIÓN ---

        val outputBuffer = Array(1) { Array(300) { FloatArray(6) } }
        interpreter.run(imageBuffer, outputBuffer)

        Log.d(TAG, "🧠 Modelo ejecutado. Procesando resultados...")

        return postProcess(outputBuffer[0])
    }

    private fun postProcess(output: Array<FloatArray>): List<Detection> {
        val detections = mutableListOf<Detection>()
        val maxScore = output.maxOfOrNull { it[4] } ?: -1f
        Log.d(TAG, "🔎 Confianza máxima encontrada en este frame: ${String.format("%.4f", maxScore)}")

        for (detectionData in output) {
            val score = detectionData[4]
            if (score > scoreThreshold) {
                val cx = detectionData[0]
                val cy = detectionData[1]
                val w = detectionData[2]
                val h = detectionData[3]

                Log.d(TAG, "👍 Detección VÁLIDA encontrada con confianza: ${String.format("%.2f", score)}")

                val left = cx - w / 2
                val top = cy - h / 2
                val right = cx + w / 2
                val bottom = cy + h / 2

                val boundingBox = RectF(left, top, right, bottom)
                val category = Category(label = "caja", score = score)
                val detection = Detection(boundingBox = boundingBox, categories = listOf(category))
                detections.add(detection)
            }
        }
        return detections
    }

    override fun close() {
        interpreter.close()
    }
}