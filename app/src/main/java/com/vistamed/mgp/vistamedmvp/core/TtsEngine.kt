package com.vistamed.mgp.vistamedmvp.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsEngine(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            // Español de Guatemala; ajusta si quieres es-ES o es-MX
            tts.language = Locale("es", "GT")
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vistamed-utterance")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
