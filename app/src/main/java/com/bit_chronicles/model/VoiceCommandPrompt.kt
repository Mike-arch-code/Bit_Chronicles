package com.bit_chronicles.model

import android.util.Log
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.model.firebase.AdventureRepository
import com.bit_chronicles.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceCommandPrompt(private val input: String) {
    fun build(): String {
        return """
            Estás narrando un juego de rol de fantasía. El jugador ha dicho: "$input".
            
            Interpreta su frase como una acción o diálogo del personaje y responde con una narración breve y envolvente en tercera persona. Incluye reacciones del entorno o personajes si es relevante, sin salir del mundo del juego.
        """.trimIndent()
    }

    fun process(
        userId: String,
        worldName: String,
        onResult: (String) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val apiService = ApiService()
        val prompt = build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Enviar prompt a la IA
                apiService.sendPrompt(prompt)

                // Esperar respuesta de la IA del flow
                val state = apiService.uiState.first {
                    it is UiState.Success || it is UiState.Error
                }

                when (state) {
                    is UiState.Success -> {
                        val aiResponse = state.response
                        val timestamp = System.currentTimeMillis()

                        // Guardar en el chat: mensaje del jugador
                        AdventureRepository.addMessageToChat(
                            userId = userId,
                            worldName = worldName,
                            messageId = "$timestamp",
                            sender = "player",
                            message = input
                        )

                        // Guardar en el chat: respuesta de la IA
                        AdventureRepository.addMessageToChat(
                            userId = userId,
                            worldName = worldName,
                            messageId = "${timestamp + 1}",
                            sender = "dm",
                            message = aiResponse
                        )

                        withContext(Dispatchers.Main) {
                            onResult(aiResponse)
                        }
                    }

                    is UiState.Error -> {
                        withContext(Dispatchers.Main) {
                            onError(Exception(state.message))
                        }
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("VoiceCommandPrompt", "Error al procesar comando", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}
