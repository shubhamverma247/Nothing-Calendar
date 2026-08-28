package com.dotfield.dotcal.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.StringRes
import com.dotfield.dotcal.R
import java.util.Locale

internal enum class VoiceDictationState { Listening, Ready, Empty, Cancelled, Unavailable, PermissionDenied, Failed }

@StringRes
internal fun VoiceDictationState.stringRes(): Int = when (this) {
    VoiceDictationState.Listening -> R.string.quick_add_voice_listening
    VoiceDictationState.Empty -> R.string.quick_add_voice_empty
    VoiceDictationState.Cancelled -> R.string.quick_add_voice_cancelled
    VoiceDictationState.Unavailable -> R.string.quick_add_voice_unavailable
    VoiceDictationState.PermissionDenied -> R.string.quick_add_voice_denied
    VoiceDictationState.Failed -> R.string.quick_add_voice_failed
    VoiceDictationState.Ready -> 0
}

internal fun voiceDictationStateForError(error: Int): VoiceDictationState = when (error) {
    SpeechRecognizer.ERROR_CLIENT -> VoiceDictationState.Cancelled
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceDictationState.PermissionDenied
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceDictationState.Empty
    else -> VoiceDictationState.Failed
}

internal class VoiceDictationController(
    context: Context,
    private val onState: (VoiceDictationState) -> Unit,
    private val onText: (String) -> Unit,
) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onState(VoiceDictationState.Unavailable)
            return
        }
        val speechRecognizer = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
            } else SpeechRecognizer.createSpeechRecognizer(appContext)
        }.getOrElse { onState(VoiceDictationState.Unavailable); return }
        recognizer?.destroy()
        recognizer = speechRecognizer
        speechRecognizer.setRecognitionListener(listener)
        listening = true
        onState(VoiceDictationState.Listening)
        runCatching { speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }) }.onFailure {
            listening = false
            onState(VoiceDictationState.Failed)
        }
    }

    fun cancel() {
        if (!listening) return
        recognizer?.cancel()
        listening = false
        onState(VoiceDictationState.Cancelled)
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        listening = false
    }

    private val listener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            listening = false
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty()
            if (text.isEmpty()) onState(VoiceDictationState.Empty) else { onText(text); onState(VoiceDictationState.Ready) }
        }
        override fun onError(error: Int) { listening = false; onState(voiceDictationStateForError(error)) }
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onPartialResults(partialResults: Bundle?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
