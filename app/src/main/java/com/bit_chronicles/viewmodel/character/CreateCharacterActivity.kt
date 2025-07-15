package com.bit_chronicles.viewmodel.character

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bit_chronicles.R
import com.bit_chronicles.model.data.CharacterParser
import com.bit_chronicles.model.proms.CharacterPrompt
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.viewmodel.UiState
import com.bit_chronicles.model.firebase.CharacterRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateCharacterActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var spinnerRace: Spinner
    private lateinit var spinnerClass: Spinner
    private lateinit var spinnerBackground: Spinner
    private lateinit var radioGroupAlignment: RadioGroup
    private lateinit var editPersonality: EditText
    private lateinit var editAbilities: EditText
    private lateinit var editMotivation: EditText
    private lateinit var buttonCreate: Button
    private lateinit var responseTextView: TextView

    private var selectedImageUri: Uri? = null
    private val apiService: ApiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_person)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Bind UI
        editName = findViewById(R.id.editCharacterName)
        spinnerRace = findViewById(R.id.spinnerRace)
        spinnerClass = findViewById(R.id.spinnerClass)
        spinnerBackground = findViewById(R.id.spinnerBackground)
        editPersonality = findViewById(R.id.editPersonalityTraits)
        editAbilities = findViewById(R.id.editAbilities)
        editMotivation = findViewById(R.id.editMotivation)
        buttonCreate = findViewById(R.id.sendCharacterButton)
        responseTextView = findViewById(R.id.responseTextView)

        // Cargar los valores en los spinners
        ArrayAdapter.createFromResource(
            this,
            R.array.races,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRace.adapter = it
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.classes,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerClass.adapter = it
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.backgrounds,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBackground.adapter = it
        }

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Botón para generar el personaje con la IA
        buttonCreate.setOnClickListener {

            val prompt = CharacterPrompt(
                name = editName.text.toString().trim(),
                race = spinnerRace.selectedItem.toString(),
                characterClass = spinnerClass.selectedItem.toString(),
                background = spinnerBackground.selectedItem.toString(),
                personalityTraits = editPersonality.text.toString().trim(),
                abilities = editAbilities.text.toString().trim(),
                motivation = editMotivation.text.toString().trim()
            ).buildPromptString()

            apiService.sendPrompt(prompt)
        }


        lifecycleScope.launch {
            apiService.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Initial -> responseTextView.text = ""
                    is UiState.Loading -> responseTextView.text = "Generando..."
                    is UiState.Success -> {
                        val story = state.response
                        responseTextView.text = story

                        val characterName = editName.text.toString().trim()
                        val alignmentId = radioGroupAlignment.checkedRadioButtonId
                        val selectedRadio = findViewById<RadioButton>(alignmentId)
                        val alignment = selectedRadio?.text?.toString() ?: "Neutral"

                        val metadata = mapOf(
                            "nombre" to characterName,
                            "raza" to spinnerRace.selectedItem.toString(),
                            "clase" to spinnerClass.selectedItem.toString(),
                            "fondo" to spinnerBackground.selectedItem.toString(),
                            "alineamiento" to alignment,
                            "personalidad" to editPersonality.text.toString().trim(),
                            "habilidades" to editAbilities.text.toString().trim(),
                            "motivación" to editMotivation.text.toString().trim()
                        )

                        val structuredData = CharacterParser.parseCharacterData(story)

                        CharacterRepository.saveCharacter(
                            userId = "Mike",
                            characterName = characterName,
                            parsedData = structuredData,
                            story = story,
                            onSuccess = {
                                Toast.makeText(
                                    this@CreateCharacterActivity,
                                    "Personaje guardado en Firebase",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = {
                                Toast.makeText(
                                    this@CreateCharacterActivity,
                                    "Error al guardar personaje: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )



                        val intent = Intent(this@CreateCharacterActivity, CharacterinfoActivity::class.java)
                        intent.putExtra("characterName", characterName)
                        startActivity(intent)
                    }

                    is UiState.Error -> {
                        val error = if (state.message.contains("503")) {
                            "Modelo saturado. Intenta más tarde."
                        } else {
                            "Error: ${state.message}"
                        }
                        responseTextView.text = error
                    }
                }
            }
        }
    }
}
