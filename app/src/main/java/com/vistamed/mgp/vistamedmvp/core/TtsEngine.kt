package com.vistamed.mgp.vistamedmvp.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsEngine(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "TtsEngine"
    private var tts: TextToSpeech = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    // --- ¡AQUÍ ESTÁ LA LÓGICA! ---
    // Estos son los callbacks que MainActivity está escuchando.
    var onStartSpeaking: () -> Unit = {}
    var onDoneSpeaking: () -> Unit = {}

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            // Español de Guatemala; ajusta si quieres es-ES o es-MX
            tts.language = Locale("es", "GT")
            Log.d(TAG, "✅ TTS inicializado.")

            // --- ¡ESTA ES LA PARTE QUE FALTABA! ---
            // Configuramos el listener para que nos avise
            // cuándo empieza y cuándo termina de hablar.
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "TTS 🗣️ Empezando a hablar...")
                    // Llamamos al callback de MainActivity
                    onStartSpeaking()
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "TTS 🛑 Terminó de hablar.")
                    // Llamamos al callback de MainActivity
                    onDoneSpeaking()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "TTS ❌ Error al hablar.")
                    // También llamamos a 'onDone' en caso de error
                    // para que el motor de voz se reactive.
                    onDoneSpeaking()
                }
            })
            // --- FIN DE LA PARTE QUE FALTABA ---

        } else {
            Log.e(TAG, "❌ Falló la inicialización de TTS. Estado: $status")
        }
    }

    fun speak(text: String) {
        if (!ready) {
            Log.w(TAG, "TTS no está listo, ignorando: '$text'")
            // Aún así llama a onDone para desbloquear el motor de voz
            onDoneSpeaking()
            return
        }

        // Usamos un ID ("vistamed") para que el listener pueda rastrearlo
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vistamed-utterance")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}