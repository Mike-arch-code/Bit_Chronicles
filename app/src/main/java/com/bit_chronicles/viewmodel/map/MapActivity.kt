package com.bit_chronicles.viewmodel.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bit_chronicles.R
import com.bit_chronicles.model.VoiceCommandPrompt
import java.util.Locale

class MapActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private lateinit var worldName: String

    // Esta es la voz predeterminada que quieres usar
    private val vozPorDefecto = "es-es-x-eed-network"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        worldName = intent.getStringExtra("worldName") ?: ""

        checkAudioPermission()
        tts = TextToSpeech(this, this)

        findViewById<Button>(R.id.btnVoice).setOnClickListener {
            startListeningFallback()
        }
    }

    private fun checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = result?.firstOrNull()?.lowercase(Locale.ROOT) ?: return

            Log.d("SpeechRecognizer", "Texto reconocido: $text")

            VoiceCommandPrompt(text).process(
                userId = "Mike",
                worldName =worldName,
                onResult = { response ->
                    Log.d("IA", "Respuesta: $response")
                    speakText(response)
                },
                onError = { error ->
                    Log.e("IA", "Error en procesamiento: ${error.message}")
                }
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true

            tts?.language = Locale("es", "ES")
            tts?.setPitch(0.6f) // tono más grave (1.0 es neutro)
            tts?.setSpeechRate(0.9f)

            // Establecer la voz por defecto si existe
            val voz = tts?.voices?.firstOrNull { it.name == vozPorDefecto }
            if (voz != null) {
                tts?.voice = voz
                Log.d("TTS", "Voz predeterminada establecida: ${voz.name}")
            } else {
                Log.w("TTS", "Voz '${vozPorDefecto}' no encontrada.")
            }

        } else {
            Log.e("TTS", "Fallo al inicializar TTS")
        }
    }

    private fun speakText(text: String) {
        if (!isTtsReady) {
            Log.w("TTS", "TTS no está listo aún.")
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Audio permission granted")
        } else {
            Log.e("Permission", "Audio permission denied")
        }
    }
}
