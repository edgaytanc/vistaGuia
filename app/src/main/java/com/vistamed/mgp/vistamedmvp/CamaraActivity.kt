package com.vistamed.mgp.vistamedmvp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vistamed.mgp.vistamedmvp.databinding.ActivityCameraBinding
// --- IMPORTACIONES NUEVAS ---
import com.vistamed.mgp.vistamedmvp.core.TtsEngine
import com.vistamed.mgp.vistamedmvp.ui.AnnounceThrottlerPerKey
import com.vistamed.mgp.vistamedmvp.vision.FrameAnalyzer
import com.vistamed.mgp.vistamedmvp.vision.SpatialHelper // Asegúrate de importar tu SpatialHelper
import com.vistamed.mgp.vistamedmvp.vision.TfliteDetector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var detector: TfliteDetector

    // --- PROPIEDADES NUEVAS PARA EL ANUNCIO DE VOZ ---
    private lateinit var ttsEngine: TtsEngine
    private val throttler = AnnounceThrottlerPerKey(5000) // Evita anuncios repetitivos (5 segundos)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detector = TfliteDetector(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // --- INICIALIZAR EL MOTOR DE TEXTO A VOZ (TTS) ---
        ttsEngine = TtsEngine(this)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalyzer.setAnalyzer(cameraExecutor, FrameAnalyzer(detector) { results, height, width ->
                runOnUiThread {
                    binding.overlayView.setResults(results, height, width)

                    // ------------------------------------------------------------------
                    // --- ¡AQUÍ ESTÁ LA LÓGICA AÑADIDA PARA EL ANUNCIO DE VOZ! ---
                    // ------------------------------------------------------------------
                    if (results.isNotEmpty()) {
                        // Tomamos la primera detección como la principal
                        val mainDetection = results.first()
                        val label = mainDetection.categories.first().label

                        // ¡Llamamos a la función corregida de SpatialHelper!
                        // Nota que ya no se pasa 'width' como parámetro.
                        val zona = SpatialHelper.horizontalZone(mainDetection.boundingBox)

                        // Construimos el texto para el anuncio
                        val textoAnuncio = "Detectado $label a la $zona"

                        // Usamos el throttler para no repetir el mismo anuncio constantemente
                        if (throttler.shouldAnnounce(label)) {
                            ttsEngine.speak(textoAnuncio)
                        }
                    }
                    // ------------------------------------------------------------------
                }
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector.close()
        // --- LIBERAR RECURSOS DE TTS ---
        ttsEngine.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}