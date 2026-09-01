package com.dotfield.dotcal.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
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

internal fun mergeVoiceDictationText(manualText: String, spokenText: String): String {
    return listOf(manualText.trim(), spokenText.trim())
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

internal class VoiceDictationController(
    context: Context,
    private val onState: (VoiceDictationState) -> Unit,
    private val onText: (String) -> Unit,
) {
    private companion object {
        private const val TAG = "DotCalVoiceDictation"
    }

    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var listening = false
    private var sessionToken = 0

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onState(VoiceDictationState.Unavailable)
            return
        }
        if (listening) {
            Log.d(TAG, "start ignored session=$sessionToken state=Listening")
            return
        }
        val speechRecognizer = recognizer ?: runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
            } else SpeechRecognizer.createSpeechRecognizer(appContext)
        }.getOrElse { onState(VoiceDictationState.Unavailable); return }.also {
            recognizer = it
        }
        val currentSession = sessionToken + 1
        sessionToken = currentSession
        speechRecognizer.setRecognitionListener(createListener(currentSession))
        listening = true
        Log.d(TAG, "start session=$currentSession")
        onState(VoiceDictationState.Listening)
        runCatching { speechRecognizer.startListening(recognizerIntent()) }.onFailure {
            Log.w(TAG, "startListening failed session=$currentSession", it)
            if (sessionToken == currentSession) {
                listening = false
                runCatching { speechRecognizer.cancel() }
                recognizer?.destroy()
                recognizer = null
                onState(VoiceDictationState.Failed)
            }
        }
    }

    fun cancel() {
        if (!listening) {
            Log.d(TAG, "cancel ignored session=$sessionToken state=Idle")
            return
        }
        val currentSession = sessionToken
        listening = false
        Log.d(TAG, "cancel session=$currentSession")
        runCatching { recognizer?.cancel() }
        onState(VoiceDictationState.Cancelled)
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        listening = false
        sessionToken++
    }

    private fun recognizerIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    private fun createListener(session: Int): RecognitionListener = object : RecognitionListener {
        private fun isCurrentSession(): Boolean = listening && sessionToken == session

        override fun onResults(results: Bundle?) {
            if (!isCurrentSession()) {
                Log.d(TAG, "ignore stale onResults session=$session")
                return
            }
            listening = false
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty()
            Log.d(TAG, "onResults session=$session textLength=${text.length}")
            if (text.isEmpty()) onState(VoiceDictationState.Empty) else { onText(text); onState(VoiceDictationState.Ready) }
        }
        override fun onError(error: Int) {
            if (!isCurrentSession()) {
                Log.d(TAG, "ignore stale onError session=$session error=$error")
                return
            }
            listening = false
            val state = voiceDictationStateForError(error)
            Log.w(TAG, "onError session=$session error=$error state=$state")
            onState(state)
        }
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onPartialResults(partialResults: Bundle?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
