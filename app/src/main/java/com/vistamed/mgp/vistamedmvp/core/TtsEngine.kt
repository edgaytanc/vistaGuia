package com.vistamed.mgp.vistamedmvp.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.speech.tts.UtteranceProgressListener

class TtsEngine(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    // 1. Define lambdas para los callbacks
    var onStartSpeaking: () -> Unit = {}
    var onDoneSpeaking: () -> Unit = {}

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts.language = Locale("es", "GT")

            // 2. Configura el listener
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onStartSpeaking() // Llama al callback
                }
                override fun onDone(utteranceId: String?) {
                    onDoneSpeaking() // Llama al callback
                }
                override fun onError(utteranceId: String?) {
                    onDoneSpeaking() // Llama a 'done' también en error
                }
            })
        }
    }

    fun speak(text: String) {
        if (!ready) return
        // 3. Asegúrate de pasar el ID para que el listener se dispare
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vistamed-utterance")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
