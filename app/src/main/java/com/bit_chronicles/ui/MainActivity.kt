package com.bit_chronicles.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.data.api.ApiService
import com.bit_chronicles.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.bit_chronicles.data.firebase.RealTime
import com.bit_chronicles.model.AdventurePrompt


class MainActivity : ComponentActivity() {

    private val  ApiService: ApiService by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        val db = RealTime()
        db.write("path/to/data", "Hola Mundo")

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
            val adventurePrompt = AdventurePrompt(
                editWorldName.text.toString().trim(),
                editSettingType.text.toString().trim(),
                editDominantRaces.text.toString().trim(),
                editPowerSystem.text.toString().trim(),
                editMainConflict.text.toString().trim(),
                editWorldLore.text.toString().trim(),
                editTone.text.toString().trim()
            )
            val prompt = adventurePrompt.buildPromptString()
            if (prompt.isNotBlank()) {
                ApiService.sendPrompt(prompt)
            }
        }

        lifecycleScope.launch {
            ApiService.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Initial -> responseTextView.text = ""
                    is UiState.Loading -> responseTextView.text = "Cargando..."
                    is UiState.Success -> responseTextView.text = state.response
                    is UiState.Error -> responseTextView.text = "Error: ${state.message}"
                }
            }
        }
    }
}
