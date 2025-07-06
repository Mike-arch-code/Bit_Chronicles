package com.bit_chronicles.viewmodel

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.R
import com.bit_chronicles.model.VoiceCommandPrompt
import com.bit_chronicles.model.api.ApiService
import kotlinx.coroutines.launch
import java.util.*

class MapActivity : AppCompatActivity() {

    private lateinit var isoMapView: IsoMapView
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var recognizerIntent: Intent
    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        isoMapView = findViewById(R.id.mapView)
        checkAudioPermission()

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.d("SpeechRecognizer", "Recognition available")
            setupSpeechRecognizer()
        } else {
            Log.e("SpeechRecognizer", "Recognition NOT available on this device")
        }

        observeVoiceResult()

        findViewById<Button>(R.id.btnVoice).setOnClickListener {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                startListening()
            } else {
                Log.e("SpeechRecognizer", "Recognition not available, can't start listening")
                startListeningFallback()
            }
        }
    }

    private fun checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                1
            )
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    private fun startListening() {
        val recognizer = speechRecognizer ?: run {
            Log.e("SpeechRecognizer", "Recognizer is null. Usando fallback.")
            startListeningFallback()
            return
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?: return

                val prompt = VoiceCommandPrompt(text).build()
                apiService.sendPrompt(prompt)
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Error: $error. Usando fallback.")
                startListeningFallback()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(recognizerIntent)
    }

    private fun startListeningFallback() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla tu comando...")
        }

        try {
            startActivityForResult(intent, 1001)
        } catch (e: Exception) {
            Log.e("SpeechFallback", "No se pudo iniciar reconocimiento: ${e.message}")
        }
    }

    private fun observeVoiceResult() {
        lifecycleScope.launch {
            apiService.uiState.collect { state ->
                when (state) {
                    is UiState.Success -> {
                        val response = state.response.lowercase(Locale.ROOT)
                        Log.d("IA", "Respuesta IA: $response")
                        if ("caminar" in response || "mover" in response) {
                            tryMovePlayer()
                        }
                    }

                    is UiState.Error -> {
                        Log.e("IA", "Error: ${state.message}")
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = result?.firstOrNull() ?: return

            val prompt = VoiceCommandPrompt(text).build()
            apiService.sendPrompt(prompt)
        }
    }

    private fun tryMovePlayer() {
        val row = isoMapView.getSelectedRow()
        val col = isoMapView.getSelectedCol()
        if (row != -1 && col != -1) {
            val deltaRow = row - 2
            val deltaCol = col - 2
            isoMapView.movePlayerBy(deltaRow, deltaCol)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Audio permission granted")
            setupSpeechRecognizer()
        } else {
            Log.e("Permission", "Audio permission denied")
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
