package com.bit_chronicles.viewmodel.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bit_chronicles.R
import com.bit_chronicles.model.VoiceCommandPrompt
import com.bit_chronicles.model.firebase.RealTime
import com.bit_chronicles.viewmodel.DiceRollFragment
import java.util.*

class MapActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private lateinit var worldName: String
    private val vozPorDefecto = "es-es-x-eed-network"
    private val db = RealTime()
    private var textoReconocido: String? = null

    private val jugadorCirculos = mutableMapOf<String, View>()
    private val listaJugadores = mutableListOf<String>()
    private var turnoActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        worldName = intent.getStringExtra("worldName") ?: ""

        checkAudioPermission()
        tts = TextToSpeech(this, this)

        val playerListLayout = findViewById<LinearLayout>(R.id.player_list)

        // BOTÓN DE VOZ
        findViewById<Button>(R.id.btnVoice).setOnClickListener {
            startListeningFallback()
        }


        db.getCampaignInfo(
            userId = "Mike",
            campaignName = worldName,
            onResult = { data ->
                val playersRaw = data["players"] as? String ?: ""
                val players = playersRaw.split(",").map { it.trim().replaceFirstChar { c -> c.uppercase() } }

                players.forEachIndexed { index, name ->
                    listaJugadores.add(name)
                    agregarJugador(name, index == 0, this, playerListLayout, jugadorCirculos)
                }
            },
            onError = { e ->
                Log.e("Campaña", "Error al cargar campaña: ${e.message}")
            }
        )
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

            // 👇 Guardamos el texto, pero aún no lo usamos
            textoReconocido = text

            // Mostramos el dado
            val diceFragment = DiceRollFragment()
            diceFragment.onDiceRolled = { resultado ->
                val promptFinal = "$text con un dado de resultado"

                val dado = resultado
                VoiceCommandPrompt(promptFinal).process(
                    userId = "Mike",
                    worldName = worldName,
                    dado = dado.toString(),
                    onResult = { response ->
                        avanzarTurno()
                        Log.d("IA", "Respuesta: $response")
                        speakText(response)
                    },
                    onError = { error ->
                        Log.e("IA", "Error en procesamiento: ${error.message}")
                    }
                )

                // Opcional: cerrar el fragmento después de lanzar el dado
                supportFragmentManager.popBackStack()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.game_fragment_container, diceFragment)
                .addToBackStack(null)
                .commit()
        }
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale("es", "ES")
            tts?.setPitch(0.6f)
            tts?.setSpeechRate(0.9f)

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

    fun agregarJugador(
        nombre: String,
        esActivo: Boolean,
        context: Context,
        playerListLayout: LinearLayout,
        jugadorCirculos: MutableMap<String, View>
    ) {
        val contenedor = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
        }

        val colorInicial = if (esActivo) "#00FF00" else "#FF0000"
        val circulo = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(32, 32).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = 16
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(colorInicial))
                setStroke(4, Color.WHITE)
            }
        }

        jugadorCirculos[nombre] = circulo

        val texto = TextView(context).apply {
            text = nombre
            setTextColor(ContextCompat.getColor(context, R.color.texto_oscuro))
            textSize = 16f
        }

        contenedor.addView(circulo)
        contenedor.addView(texto)

        playerListLayout.addView(contenedor)
    }

    fun actualizarEstadoJugador(nombre: String, esActivo: Boolean, jugadorCirculos: Map<String, View>) {
        val circulo = jugadorCirculos[nombre] ?: return
        val nuevoColor = if (esActivo) "#00FF00" else "#FF0000"
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(nuevoColor))
            setStroke(4, Color.WHITE)
        }
        circulo.background = drawable
    }

    private fun avanzarTurno() {
        val jugadorActual = listaJugadores[turnoActual]
        actualizarEstadoJugador(jugadorActual, false, jugadorCirculos)

        turnoActual = (turnoActual + 1) % listaJugadores.size

        val siguienteJugador = listaJugadores[turnoActual]
        actualizarEstadoJugador(siguienteJugador, true, jugadorCirculos)
    }
}
