package com.bit_chronicles.viewmodel.campaign

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.R
import com.bit_chronicles.model.AdventurePrompt
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.model.firebase.AdventureRepository
import com.bit_chronicles.viewmodel.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateCampaignActivity : AppCompatActivity() {

    private val apiService: ApiService by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_camp)

        val sendButton = findViewById<Button>(R.id.sendButton)
        val responseTextView = findViewById<TextView>(R.id.responseTextView)

        val editWorldName = findViewById<EditText>(R.id.editWorldName)
        val editDominantRaces = findViewById<EditText>(R.id.editDominantRaces)
        val editWorldLore = findViewById<EditText>(R.id.editWorldLore)
        val editKeyCharacters = findViewById<EditText>(R.id.editKeyCharacters)
        val editKeyLocations = findViewById<EditText>(R.id.editKeyLocations)
        val editHooks = findViewById<EditText>(R.id.editHooks)
        val editObjective = findViewById<EditText>(R.id.editObjective)

        val spinnerSetting = findViewById<Spinner>(R.id.spinnerSetting)
        val spinnerPower = findViewById<Spinner>(R.id.spinnerPower)
        val spinnerConflict = findViewById<Spinner>(R.id.spinnerConflict)
        val spinnerTone = findViewById<Spinner>(R.id.spinnerTone)

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

        sendButton.setOnClickListener {
            val worldName = editWorldName.text.toString().trim()
            val settingType = spinnerSetting.selectedItem.toString()
            val dominantRaces = editDominantRaces.text.toString().trim()
            val powerSystem = spinnerPower.selectedItem.toString()
            val mainConflict = spinnerConflict.selectedItem.toString()
            val worldLore = editWorldLore.text.toString().trim()
            val tone = spinnerTone.selectedItem.toString()
            val keyCharacters = editKeyCharacters.text.toString().trim()
            val keyLocations = editKeyLocations.text.toString().trim()
            val hooks = editHooks.text.toString().trim()
            val objective = editObjective.text.toString().trim()

            val prompt = AdventurePrompt(
                worldName,
                settingType,
                dominantRaces,
                powerSystem,
                mainConflict,
                worldLore,
                tone,
                hooks,
                keyCharacters,
                keyLocations,
                objective
            ).buildPromptString()

            if (prompt.isNotBlank()) {
                apiService.sendPrompt(prompt)
            }
        }

        lifecycleScope.launch {
            apiService.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Initial -> responseTextView.text = ""
                    is UiState.Loading -> responseTextView.text = "Cargando..."
                    is UiState.Success -> {
                        responseTextView.text = state.response

                        val worldName = findViewById<EditText>(R.id.editWorldName).text.toString().trim()

                        val metadata = mapOf(
                            "worldName" to worldName,
                            "settingType" to findViewById<Spinner>(R.id.spinnerSetting).selectedItem.toString(),
                            "dominantRaces" to findViewById<EditText>(R.id.editDominantRaces).text.toString().trim(),
                            "powerSystem" to findViewById<Spinner>(R.id.spinnerPower).selectedItem.toString(),
                            "mainConflict" to findViewById<Spinner>(R.id.spinnerConflict).selectedItem.toString(),
                            "worldLore" to findViewById<EditText>(R.id.editWorldLore).text.toString().trim(),
                            "tone" to findViewById<Spinner>(R.id.spinnerTone).selectedItem.toString(),
                            "createdAt" to System.currentTimeMillis()
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
}
