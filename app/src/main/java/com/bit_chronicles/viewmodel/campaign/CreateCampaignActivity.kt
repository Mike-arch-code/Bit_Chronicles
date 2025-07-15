package com.bit_chronicles.viewmodel.campaign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.R
import com.bit_chronicles.model.proms.AdventurePrompt
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.model.firebase.AdventureRepository
import com.bit_chronicles.model.firebase.RealTime
import com.bit_chronicles.viewmodel.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateCampaignActivity : AppCompatActivity() {

    private val apiService: ApiService by viewModels()
    private val db = RealTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_camp)

        val sendButton = findViewById<Button>(R.id.sendButton)
        val responseTextView = findViewById<TextView>(R.id.responseTextView)

        val editWorldName = findViewById<EditText>(R.id.editWorldName)
        val editDominantRaces = findViewById<EditText>(R.id.editDominantRaces)
        val editWorldLore = findViewById<EditText>(R.id.editWorldLore)
        val editKeyLocations = findViewById<EditText>(R.id.editKeyLocations)
        val editObjective = findViewById<EditText>(R.id.editObjective)

        val spinnerSetting = findViewById<Spinner>(R.id.spinnerSetting)
        val spinnerPower = findViewById<Spinner>(R.id.spinnerPower)
        val spinnerConflict = findViewById<Spinner>(R.id.spinnerConflict)
        val spinnerTone = findViewById<Spinner>(R.id.spinnerTone)
        val spinerturnos = findViewById<Spinner>(R.id.spinnerturnos)

        val spinnerPlayer = findViewById<Spinner>(R.id.spinnerplayer)

        // Inicializar los spinners con recursos
        ArrayAdapter.createFromResource(this, R.array.setting_types, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSetting.adapter = it
        }
        ArrayAdapter.createFromResource(this, R.array.power_systems, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPower.adapter = it
        }
        ArrayAdapter.createFromResource(this, R.array.conflict_types, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerConflict.adapter = it
        }
        ArrayAdapter.createFromResource(this, R.array.tone_types, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTone.adapter = it
        }
        ArrayAdapter.createFromResource(this, R.array.turnos, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinerturnos.adapter = it
        }

        // Cargar personajes en el Spinner
        cargarPalyer()

        // Enviar prompt
        sendButton.setOnClickListener {
            val userId = "Mike"
            val characterName = spinnerPlayer.selectedItem.toString()

            db.getCharacterInfo(
                userId = userId,
                characterName = characterName,
                onResult = { result ->
                    val playerHistory = result["historia"] as? String ?: ""

                    val worldName = editWorldName.text.toString().trim()
                    val settingType = spinnerSetting.selectedItem.toString()
                    val dominantRaces = editDominantRaces.text.toString().trim()
                    val powerSystem = spinnerPower.selectedItem.toString()
                    val mainConflict = spinnerConflict.selectedItem.toString()
                    val worldLore = editWorldLore.text.toString().trim()
                    val tone = spinnerTone.selectedItem.toString()
                    val keyLocations = editKeyLocations.text.toString().trim()
                    val objective = editObjective.text.toString().trim()
                    val turnos = spinerturnos.selectedItem.toString()

                    val prompt = AdventurePrompt(
                        worldName,
                        settingType,
                        dominantRaces,
                        powerSystem,
                        mainConflict,
                        worldLore,
                        tone,
                        keyLocations,
                        objective,
                        turnos,
                        playerHistory
                    ).buildPromptString()

                    if (prompt.isNotBlank()) {
                        apiService.sendPrompt(prompt)
                    }
                },
                onError = {
                    Toast.makeText(this, "Error cargando personaje", Toast.LENGTH_SHORT).show()
                }
            )
        }


        // Observar respuesta de la API
        lifecycleScope.launch {
            apiService.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Initial -> responseTextView.text = ""
                    is UiState.Loading -> responseTextView.text = "Cargando..."
                    is UiState.Success -> {
                        responseTextView.text = state.response

                        val worldName = editWorldName.text.toString().trim()

                        val metadata = mapOf(
                            "worldName" to worldName,
                            "settingType" to spinnerSetting.selectedItem.toString(),
                            "dominantRaces" to editDominantRaces.text.toString().trim(),
                            "powerSystem" to spinnerPower.selectedItem.toString(),
                            "mainConflict" to spinnerConflict.selectedItem.toString(),
                            "worldLore" to editWorldLore.text.toString().trim(),
                            "tone" to spinnerTone.selectedItem.toString(),
                            "createdAt" to System.currentTimeMillis(),
                            "turnos" to  spinerturnos.selectedItem.toString(),
                        )

                        val userId = "Mike"
                        val adventureId = worldName

                        AdventureRepository.createAdventure(userId, adventureId, metadata, state.response)

                        val intent = Intent(this@CreateCampaignActivity, CampaignInfoActivity::class.java)
                        intent.putExtra("campaignName", adventureId)
                        startActivity(intent)
                    }
                    is UiState.Error -> {
                        val friendlyMessage = if (state.message.contains("503")) {
                            "El modelo está sobrecargado. Intenta de nuevo más tarde."
                        } else {
                            "Error: ${state.message}"
                        }
                        responseTextView.text = friendlyMessage
                    }
                }
            }
        }
    }

    private fun cargarPalyer() {
        val userId = "Mike"
        val spinnerPlayer = findViewById<Spinner>(R.id.spinnerplayer)

        db.getCharacterList(
            userId,
            onResult = { lista ->
                val nombresPersonajes = lista // Asegúrate que el campo sea 'name'

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    nombresPersonajes
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPlayer.adapter = adapter

                spinnerPlayer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val seleccionado = nombresPersonajes[position]
                        Log.d("SpinnerPlayer", "Seleccionado: $seleccionado")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Nada seleccionado
                    }
                }
            },
            onError = {
                Log.e("CreateCampaignActivity", "Error al cargar personajes: ${it.message}")
            }
        )
    }


}
