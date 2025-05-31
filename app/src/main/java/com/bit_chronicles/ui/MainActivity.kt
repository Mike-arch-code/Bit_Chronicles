package com.bit_chronicles.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.R
import com.bit_chronicles.data.api.ApiService
import com.bit_chronicles.data.firebase.AdventureRepository
import com.bit_chronicles.model.AdventurePrompt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val ApiService: ApiService by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sendButton = findViewById<Button>(R.id.sendButton)
        val responseTextView = findViewById<TextView>(R.id.responseTextView)

        val editWorldName = findViewById<EditText>(R.id.editWorldName)
        val editSettingType = findViewById<EditText>(R.id.editSettingType)
        val editDominantRaces = findViewById<EditText>(R.id.editDominantRaces)
        val editPowerSystem = findViewById<EditText>(R.id.editPowerSystem)
        val editMainConflict = findViewById<EditText>(R.id.editMainConflict)
        val editWorldLore = findViewById<EditText>(R.id.editWorldLore)
        val editTone = findViewById<EditText>(R.id.editTone)

        sendButton.setOnClickListener {
            val worldName = editWorldName.text.toString().trim()
            val settingType = editSettingType.text.toString().trim()
            val dominantRaces = editDominantRaces.text.toString().trim()
            val powerSystem = editPowerSystem.text.toString().trim()
            val mainConflict = editMainConflict.text.toString().trim()
            val worldLore = editWorldLore.text.toString().trim()
            val tone = editTone.text.toString().trim()

            val prompt = AdventurePrompt(
                worldName,
                settingType,
                dominantRaces,
                powerSystem,
                mainConflict,
                worldLore,
                tone
            ).buildPromptString()

            if (prompt.isNotBlank()) {
                val metadata = mapOf(
                    "worldName" to worldName,
                    "settingType" to settingType,
                    "dominantRaces" to dominantRaces,
                    "powerSystem" to powerSystem,
                    "mainConflict" to mainConflict,
                    "worldLore" to worldLore,
                    "tone" to tone,
                    "createdAt" to System.currentTimeMillis()
                )

                val userId = "Mike"
                val adventureId = worldName // puedes usar un UUID si prefieres

                AdventureRepository.createAdventure(userId, adventureId, metadata, prompt)
                AdventureRepository.addMessageToChat(userId, adventureId, "1", "dm", "Hola, bienvenido a tu aventura.")

                ApiService.sendPrompt(prompt)
            }
        }

        lifecycleScope.launch {
            ApiService.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Initial -> responseTextView.text = ""
                    is UiState.Loading -> responseTextView.text = "Cargando..."
                    is UiState.Success -> {
                        responseTextView.text = state.response
                        val userId = "Mike"
                        val adventureId = editWorldName.text.toString().trim()
                        AdventureRepository.addMessageToChat(userId, adventureId, "2", "dm", state.response)
                    }
                    is UiState.Error -> responseTextView.text = "Error: ${state.message}"
                }
            }
        }
    }
}
