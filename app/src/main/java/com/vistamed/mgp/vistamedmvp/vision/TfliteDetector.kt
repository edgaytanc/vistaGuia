package com.vistamed.mgp.vistamedmvp.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.BufferedReader
import java.io.InputStreamReader

// Nuestras clases de datos personalizadas
data class Category(val label: String, val score: Float)
data class Detection(val boundingBox: RectF, val categories: List<Category>)

class TfliteDetector(
    context: Context,
    private val modelPath: String = "vistamed_yolov8n_float16_nms.tflite",
    private val labelPath: String = "labels.txt", // Nombre del archivo de etiquetas
    private val scoreThreshold: Float = 0.3f // Podemos subir el umbral para mayor precisión
) : Detector {

    private val interpreter: Interpreter
    private val imageWidth: Int
    private val imageHeight: Int
    private val TAG = "TfliteDetector"
    // --- LA CORRECCIÓN CLAVE ---
    // La lista de etiquetas ahora se cargará desde el archivo `labels.txt`
    private val labels: List<String>

    init {
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, Interpreter.Options().apply { numThreads = 4 })
        val inputShape = interpreter.getInputTensor(0).shape()
        imageHeight = inputShape[1]
        imageWidth = inputShape[2]

        // --- Cargar las etiquetas desde el archivo de assets ---
        labels = loadLabels(context, labelPath)

        Log.d(TAG, "✅ Intérprete inicializado para imágenes de ${imageWidth}x${imageHeight}")
        Log.d(TAG, "🏷️ Etiquetas cargadas: $labels")
    }

    // Nueva función para leer el archivo de etiquetas
    private fun loadLabels(context: Context, filePath: String): List<String> {
        val labelList = mutableListOf<String>()
        try {
            val inputStream = context.assets.open(filePath)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                labelList.add(line!!)
            }
            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar el archivo de etiquetas: ${e.message}")
        }
        return labelList
    }

    override fun detect(bitmap: Bitmap, rotation: Int): List<Detection> {
        Log.d(TAG, "➡️ Recibido nuevo frame para detectar. Tamaño: ${bitmap.width}x${bitmap.height}")

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imageHeight, imageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        val imageBuffer = tensorImage.buffer

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
                val classId = detectionData[5].toInt()

                // Usamos la lista de etiquetas cargada del archivo
                val label = labels.getOrElse(classId) { "Desconocido #$classId" }
                Log.d(TAG, "👍 Detección VÁLIDA: '$label' con confianza: ${String.format("%.2f", score)}")

                val cx = detectionData[0]
                val cy = detectionData[1]
                val w = detectionData[2]
                val h = detectionData[3]

                val left = cx - w / 2
                val top = cy - h / 2
                val right = cx + w / 2
                val bottom = cy + h / 2

                val boundingBox = RectF(left, top, right, bottom)
                val category = Category(label = label, score = score)
                val detection = Detection(boundingBox = boundingBox, categories = listOf(category))
                detections.add(detection)
            }
        }

        if (detections.isNotEmpty()) {
            Log.d(TAG, "✅ Proceso finalizado. Se devuelven ${detections.size} detecciones.")
        }
        return detections
    }

    override fun close() {
        interpreter.close()
    }
}