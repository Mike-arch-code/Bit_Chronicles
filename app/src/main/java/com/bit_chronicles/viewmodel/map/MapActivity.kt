package com.bit_chronicles.viewmodel.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.R
import com.bit_chronicles.model.VoiceCommandPrompt
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.viewmodel.UiState
import kotlinx.coroutines.launch
import java.util.Locale

class MapActivity : AppCompatActivity() {

    private lateinit var isoMapView: IsoMapView
    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        isoMapView = findViewById(R.id.mapView)
        checkAudioPermission()
        observeVoiceResult()

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

            // Si reconoce caminar o mover, ejecuta acción directa sin IA
            if ("caminar" in text || "mover" in text) {
                Log.d("SpeechRecognizer", "Comando directo detectado: $text")
                tryMovePlayer()
            } else {
                // Si no, envía el texto a la IA
                val prompt = VoiceCommandPrompt(text).build()
                Log.d("Prompt", "Enviando prompt a IA: $prompt")
                apiService.sendPrompt(prompt)
            }
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

    private fun tryMovePlayer() {
        val selectedRow = isoMapView.getSelectedRow()
        val selectedCol = isoMapView.getSelectedCol()

        Log.d("MapMove", "SelectedRow: $selectedRow, SelectedCol: $selectedCol")

        if (selectedRow != -1 && selectedCol != -1) {
            val deltaRow = selectedRow - 3
            val deltaCol = selectedCol - 3
            val targetRow = isoMapView.playerMapRow + deltaRow
            val targetCol = isoMapView.playerMapCol + deltaCol

            Log.d("MapMove", "Moviendo a fila=$targetRow, columna=$targetCol")

            isoMapView.animatePlayerSmoothlyTo(targetRow, targetCol, speedPerTileMs = 600L)
        } else {
            Log.w("MapMove", "No hay celda seleccionada para moverse")
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
        } else {
            Log.e("Permission", "Audio permission denied")
        }
    }
}