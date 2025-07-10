package com.bit_chronicles.viewmodel

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
import kotlinx.coroutines.launch
import java.util.*

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
            val text = result?.firstOrNull() ?: return

            Log.d("SpeechRecognizer", "Texto reconocido (fallback): $text") // ✅ VER QUÉ RECONOCIÓ

            val prompt = VoiceCommandPrompt(text).build()
            apiService.sendPrompt(prompt)
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

        if (selectedRow != -1 && selectedCol != -1) {
            val deltaRow = selectedRow - 3
            val deltaCol = selectedCol - 3
            val targetRow = isoMapView.playerMapRow + deltaRow
            val targetCol = isoMapView.playerMapCol + deltaCol

            isoMapView.animatePlayerSmoothlyTo(targetRow, targetCol, speedPerTileMs = 600L)
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
