package com.vistamed.mgp.vistamedmvp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.vistamed.mgp.vistamedmvp.core.LabelUtils
import com.vistamed.mgp.vistamedmvp.core.Prefs
import com.vistamed.mgp.vistamedmvp.core.TtsEngine
import com.vistamed.mgp.vistamedmvp.databinding.ActivityMainBinding
import com.vistamed.mgp.vistamedmvp.ui.AnnounceThrottlerPerKey
// IMPORT AÑADIDO: Esta línea le dice al código dónde encontrar AppMode
import com.vistamed.mgp.vistamedmvp.ui.AppMode
import com.vistamed.mgp.vistamedmvp.voice.CommandParser
import com.vistamed.mgp.vistamedmvp.voice.VoiceCommandEngine
import com.vistamed.mgp.vistamedmvp.vision.*
//import org.tensorflow.lite.task.vision.detector.Detection
import java.util.concurrent.Executors

// LÍNEA ELIMINADA: El "enum class AppMode" que estaba aquí fue removido para evitar el conflicto.

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TtsEngine
    private lateinit var voice: VoiceCommandEngine
    private lateinit var prefs: Prefs
    private lateinit var detector: Detector

    private var mode: AppMode = AppMode.EXPLORACION
    private val labelThrottler = AnnounceThrottlerPerKey(2200L)
    // Throttler para "sigo buscando"
    private val searchingThrottler = AnnounceThrottlerPerKey(8000L) // 8 segundos
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // =======================================================
    // VARIABLES PARA FILTRO DE ESTABILIDAD
    // =======================================================
    private var isCameraStabilizing = true // Para la Solución 2
    private var lastSeenLabel: String? = null
    private var consecutiveDetections = 0
    private val STABILITY_THRESHOLD = 5 // Requerir 3 frames seguidos
    private val MIN_BOX_SIZE = 0.05f // Requerir que la caja ocupe al menos 4% de la pantalla
    // =======================================================

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        val allGranted = grantedMap.values.all { it }
        if (allGranted) startCamera()
        else binding.tvStatus.text = "Permisos requeridos denegados"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TtsEngine(this)

        // 1. Define la función de reinicio UNA VEZ.
        // Usamos runOnUiThread para evitar conflictos de hilos
        val restartVoiceLoop: () -> Unit = {
            runOnUiThread {
                voice.start()
            }
        }


        // 2. Asigna esta función a AMBOS callbacks
        tts.onStartSpeaking = {
            voice.stop()
        }
        tts.onDoneSpeaking = restartVoiceLoop // Cuando el TTS termine, reinicia

        prefs = Prefs(this)

        detector = try { TfliteDetector(this) } catch (_: Exception) { FakeDetector() }
        // 3. Pasa la función de reinicio al nuevo constructor
        voice = VoiceCommandEngine(
            this,
            { text -> handleVoice(text) }, // onCommand
            restartVoiceLoop               // onRestartRequest
        )

        // ESTA ES LA LÍNEA QUE FALTABA:
        // Le dice al motor de voz que está "listo" para empezar
        voice.activate()

        // --- OCR --- (Esto parece que lo quitaste, lo cual está bien por ahora)
        // textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        updateStatus()
        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val needCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        val needAudio  = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        if (needCamera || needAudio) {
            permLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, FrameAnalyzer(detector) { detections, height, width ->
                            runOnUiThread { onDetections(detections, height, width) }
                        })
                    }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, selector, preview, analyzer)

                tts.speak("Bienvenido a VistaMed. Di, activar exploración o modo búsqueda.")

                // Ignora detecciones por 2.5 segundos mientras la cámara estabiliza
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isCameraStabilizing = false
                }, 2500) // 2.5 segundos

                updateStatus()

            } catch (e: Exception) {
                binding.tvStatus.text = "Error iniciando cámara: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // =======================================================
    // FUNCIÓN onDetections() MODIFICADA
    // =======================================================
    private fun onDetections(list: List<Detection>, imageHeight: Int, imageWidth: Int) {
        // 1. Filtro de estabilización de cámara
        if (isCameraStabilizing) return

        binding.overlayView.setResults(list, imageHeight, imageWidth)

        // 2. Obtener la mejor detección (la de mayor confianza)
        val top = list.maxByOrNull { it.categories.firstOrNull()?.score ?: 0f }

        // 3. Filtro de Tamaño y Confianza
        // Si no hay detección 'top', o si la que hay es muy pequeña, reseteamos el contador
        if (top == null || isBoxTooSmall(top.boundingBox)) {
            resetStability()
            updateStatus(extra = "(0 detecciones)")
            return
        }

        // Si llegamos aquí, 'top' es un objeto válido y de tamaño decente
        val topCategory = top.categories.first()
        val label = topCategory.label
        val score = topCategory.score

        // 4. Lógica de Estabilidad
        if (label == lastSeenLabel) {
            consecutiveDetections++ // Vimos el mismo objeto otra vez
        } else {
            lastSeenLabel = label // Vimos un objeto nuevo
            consecutiveDetections = 1 // Reseteamos el contador a 1
        }

        // 5. Comprobar si la detección es "estable"
        val isStable = consecutiveDetections >= STABILITY_THRESHOLD

        when (mode) {
            AppMode.EXPLORACION -> {
                val pos = SpatialHelper.horizontalZone(box = top.boundingBox)
                val stabilityMarker = if (isStable) "✅" else "⌛" // Feedback visual de estabilidad
                updateStatus(extra = "Detectado: $label $stabilityMarker (${String.format("%.2f", score)})")

                val key = LabelUtils.normalize(label)

                // ANUNCIAR SOLO SI ES ESTABLE
                if (isStable && labelThrottler.shouldAnnounce(key)) {
                    tts.speak("Detectado $label a la $pos")
                    vibrateShort()
                }
            }

            AppMode.BUSQUEDA -> {
                val target = prefs.targetLabel?.trim().orEmpty()
                if (target.isEmpty()) {
                    updateStatus(extra = "Búsqueda sin objetivo")
                    return
                }

                // Lógica de "mejor" coincidencia
                val matches = list.filter { det ->
                    val labelMatch = det.categories.firstOrNull()?.label ?: ""
                    !isBoxTooSmall(det.boundingBox) && LabelUtils.matches(target, labelMatch) // Filtra también por tamaño
                }
                val bestMatch = matches.maxByOrNull { it.categories.firstOrNull()?.score ?: 0f }

                if (bestMatch != null) {
                    val pos = SpatialHelper.horizontalZone(box = bestMatch.boundingBox)
                    updateStatus(extra = "Buscando: ${LabelUtils.normalize(target)} • ¡encontrado!")

                    val key = "objetivo:${LabelUtils.normalize(target)}"
                    // (Aquí no es necesaria la estabilidad, solo el throttler,
                    // porque el usuario está buscando activamente)
                    if (labelThrottler.shouldAnnounce(key)) {
                        tts.speak("${target} encontrado a la $pos")
                        vibrateShort()
                    }
                    resetStability() // Resetea el contador de exploración
                } else {
                    val targetName = LabelUtils.normalize(target)
                    updateStatus(extra = "Buscando: $targetName")
                    if (searchingThrottler.shouldAnnounce("buscando:$targetName")) {
                        tts.speak("Sigo buscando $targetName")
                    }
                    resetStability() // Resetea el contador de exploración
                }
            }
        }
    }

    // Funciones auxiliares para la estabilidad
    private fun isBoxTooSmall(box: RectF): Boolean {
        val area = (box.right - box.left) * (box.bottom - box.top)
        return area < MIN_BOX_SIZE
    }

    private fun resetStability() {
        lastSeenLabel = null
        consecutiveDetections = 0
    }
    // =======================================================
    // FIN DE FUNCIONES MODIFICADAS
    // =======================================================

    private fun handleVoice(text: String) {
        when (val cmd = CommandParser.parse(text)) {
            is com.vistamed.mgp.vistamedmvp.voice.Command.ActivarExploracion -> {
                mode = AppMode.EXPLORACION
                resetStability()
                tts.speak("Exploración activada")
                updateStatus()
            }
            is com.vistamed.mgp.vistamedmvp.voice.Command.ModoBusqueda -> {
                mode = AppMode.BUSQUEDA
                resetStability()
                tts.speak("Modo búsqueda activado. Di, buscar seguido del nombre del medicamento.")
                updateStatus()
            }
            is com.vistamed.mgp.vistamedmvp.voice.Command.Buscar -> {
                prefs.targetLabel = cmd.objetivo
                mode = AppMode.BUSQUEDA
                resetStability()
                tts.speak("Buscando ${cmd.objetivo}")
                updateStatus()
            }
            is com.vistamed.mgp.vistamedmvp.voice.Command.Detener -> {
                tts.speak("Deteniendo")
            }
            // Comando "Modo Actual"
            is com.vistamed.mgp.vistamedmvp.voice.Command.ModoActual -> {
                val modeTxt = when (mode) {
                    AppMode.EXPLORACION -> "Exploración"
                    AppMode.BUSQUEDA -> "Búsqueda"
                }
                tts.speak("El modo actual es $modeTxt")
            }
            else -> { /* ignorar */ }
        }
    }

    private fun updateStatus(extra: String = "") {
        val modeTxt = when (mode) { AppMode.EXPLORACION -> "Exploración"; AppMode.BUSQUEDA -> "Búsqueda" }
        val suffix = if (extra.isBlank()) "" else " • $extra"
        binding.tvStatus.text = "Modo: $modeTxt$suffix"
    }

    private fun vibrateShort() {
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(android.os.VibrationEffect.createOneShot(120, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") v.vibrate(120)
        }
    }

    override fun onDestroy() {
        voice.stop()
        tts.shutdown()
        cameraExecutor.shutdown()
        detector.close()
        super.onDestroy()
    }
}