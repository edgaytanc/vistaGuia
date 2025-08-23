package com.vistamed.mgp.vistamedmvp.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceCommandEngine(
    private val context: Context,
    private val onText: (String) -> Unit
) : RecognitionListener {

    private var recognizer: SpeechRecognizer? = null
    private var active = false

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        if (active) return
        active = true
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@VoiceCommandEngine)
        }
        listen()
    }

    fun stop() {
        active = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    private fun listen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-GT")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        recognizer?.startListening(intent)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) { if (active) listen() }
    override fun onPartialResults(partialResults: Bundle) {}

    override fun onResults(results: Bundle) {
        val list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        val text = list.firstOrNull() ?: return
        onText(text)
        if (active) listen()
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
