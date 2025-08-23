package com.vistamed.mgp.vistamedmvp

import android.Manifest
import android.content.pm.PackageManager
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
import com.vistamed.mgp.vistamedmvp.core.Prefs
import com.vistamed.mgp.vistamedmvp.core.TtsEngine
import com.vistamed.mgp.vistamedmvp.core.LabelUtils
import com.vistamed.mgp.vistamedmvp.databinding.ActivityMainBinding
import com.vistamed.mgp.vistamedmvp.ui.AnnounceThrottlerPerKey
import com.vistamed.mgp.vistamedmvp.voice.CommandParser
import com.vistamed.mgp.vistamedmvp.voice.VoiceCommandEngine
import com.vistamed.mgp.vistamedmvp.vision.*
import java.util.concurrent.Executors

enum class AppMode { EXPLORACION, BUSQUEDA }

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TtsEngine
    private lateinit var voice: VoiceCommandEngine
    private lateinit var prefs: Prefs
    private lateinit var detector: Detector

    private var mode: AppMode = AppMode.EXPLORACION

    // Throttling por etiqueta (exploración) o por “objetivo” (búsqueda)
    private val labelThrottler = AnnounceThrottlerPerKey(2200L)

    private val cameraExecutor = Executors.newSingleThreadExecutor()

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
        prefs = Prefs(this)

        detector = try { TfliteDetector(this) } catch (_: Exception) { FakeDetector() }
        voice = VoiceCommandEngine(this) { text -> handleVoice(text) }

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
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, FrameAnalyzer(detector) { detections ->
                            runOnUiThread { onDetections(detections) }
                        })
                    }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, selector, preview, analyzer)

                tts.speak("Bienvenido a VistaMed. Di, activar exploración o modo búsqueda.")
                voice.start()
                updateStatus()

            } catch (e: Exception) {
                binding.tvStatus.text = "Error iniciando cámara: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun onDetections(list: List<Detection>) {
        if (list.isEmpty()) {
            updateStatus(extra = "(0 detecciones)")
            return
        }

        when (mode) {
            AppMode.EXPLORACION -> {
                // Tomamos la detección con mayor score
                val top = list.maxByOrNull { it.score } ?: return
                val label = top.label
                val pos = SpatialHelper.horizontalZone(
                    frameWidth = binding.cameraPreview.width.takeIf { it > 0 } ?: 1080,
                    box = top.box
                )
                updateStatus(extra = "Detectado: $label • $pos (${String.format("%.2f", top.score)})")

                // Cooldown por etiqueta normalizada (para evitar spam)
                val key = LabelUtils.normalize(label)
                if (labelThrottler.shouldAnnounce(key)) {
                    tts.speak("Detectado $label a la $pos")
                }
            }

            AppMode.BUSQUEDA -> {
                val target = prefs.targetLabel?.trim().orEmpty()
                if (target.isEmpty()) {
                    updateStatus(extra = "Búsqueda sin objetivo")
                    return
                }
                // ¿Alguna detección coincide con el objetivo? (normalización + sinónimos)
                val match = list.firstOrNull { det -> LabelUtils.matches(target, det.label) }
                if (match != null) {
                    val pos = SpatialHelper.horizontalZone(
                        frameWidth = binding.cameraPreview.width.takeIf { it > 0 } ?: 1080,
                        box = match.box
                    )
                    updateStatus(extra = "Buscando: ${LabelUtils.normalize(target)} • ¡encontrado!")
                    val key = "objetivo:${LabelUtils.normalize(target)}"
                    if (labelThrottler.shouldAnnounce(key)) {
                        tts.speak("${target} encontrado a la $pos")
                        vibrateShort()
                    }
                } else {
                    updateStatus(extra = "Buscando: ${LabelUtils.normalize(target)}")
                }
            }
        }
    }

    private fun handleVoice(text: String) {
        when (val cmd = CommandParser.parse(text)) {
            is com.vistamed.mgp.vistamedmvp.voice.Command.ActivarExploracion -> {
                mode = AppMode.EXPLORACION
                tts.speak("Exploración activada")
                updateStatus()
            }
            is com.vistamed.mgp.vistamedmvp.voice.Command.ModoBusqueda -> {
                mode = AppMode.BUSQUEDA
                tts.speak("Modo búsqueda activado. Di, buscar seguido del nombre del medicamento.")
                updateStatus()
            }
            is com.vistamed.mgp.vistamedmvp.voice.Command.Buscar -> {
                prefs.targetLabel = cmd.objetivo
                mode = AppMode.BUSQUEDA
                tts.speak("Buscando ${cmd.objetivo}")
                updateStatus()
            }
            is com.vistamed.mgp.vistamedmvp.voice.Command.Detener -> {
                tts.speak("Deteniendo")
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
        super.onDestroy()
    }
}
