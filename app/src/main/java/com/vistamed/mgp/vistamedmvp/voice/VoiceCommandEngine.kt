package com.vistamed.mgp.vistamedmvp.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.*

class VoiceCommandEngine(
    private val context: Context,
    private val onCommand: (text: String) -> Unit,

    // --- CAMBIO CLAVE 1 ---
    // Añadimos un callback para que el motor de voz pueda
    // "pedir" que se reinicie el bucle de escucha.
    private val onRestartRequest: () -> Unit
) {
    private val TAG = "VoiceCommandEngine"
    private var isAvailable = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-GT")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "🎤 Listo para escuchar...")
        }

        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) {
                Log.i(TAG, "Comando recibido: $text")
                // 1. Comando recibido: Se lo pasamos a MainActivity.
                // MainActivity llamará a TTS, y el ciclo se reiniciará solo.
                onCommand(text)
            } else {
                // 2. No se oyó texto: Pedimos reiniciar el bucle.
                Log.w(TAG, "No se recibió texto, pidiendo reinicio.")
                onRestartRequest()
            }
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "🎤 Fin de la voz.")
            // onResults se llamará después, así que no hacemos nada aquí.
        }

        override fun onError(error: Int) {
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Error: Permisos insuficientes"
                SpeechRecognizer.ERROR_NO_MATCH -> "Error: No se entendió"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Error: Silencio"
                else -> "Error: Otro ($error)"
            }
            Log.e(TAG, errorMsg)

            // --- CAMBIO CLAVE 2 ---
            // 3. Ocurrió un error (ej. silencio): Pedimos reiniciar el bucle.
            // Esto es lo que arregla el bug.
            onRestartRequest()
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /**
     * Inicia la escucha. (Llamado por MainActivity)
     */
    fun start() {
        if (!isAvailable) return
        Log.d(TAG, "Iniciando escucha...")
        try {
            speechRecognizer?.startListening(recognizerIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar escucha: ${e.message}")
        }
    }

    /**
     * Detiene la escucha. (Llamado por MainActivity)
     */
    fun stop() {
        if (!isAvailable) return
        Log.d(TAG, "Deteniendo escucha.")
        speechRecognizer?.stopListening()
    }

    /**
     * Activa el motor.
     */
    fun activate() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "❌ Reconocimiento de voz NO disponible en este dispositivo.")
            isAvailable = false
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }

        Log.d(TAG, "Motor de voz ACTIVADO y listo.")
        isAvailable = true
        // NO llamamos a start() aquí. MainActivity tiene el control.
    }
}