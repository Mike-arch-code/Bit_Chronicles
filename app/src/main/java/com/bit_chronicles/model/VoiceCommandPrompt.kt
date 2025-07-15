package com.bit_chronicles.model

import android.util.Log
import com.bit_chronicles.model.api.ApiService
import com.bit_chronicles.model.firebase.AdventureRepository
import com.bit_chronicles.model.firebase.RealTime
import com.bit_chronicles.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceCommandPrompt(private val input: String) {

    private fun buildPrompt(
        historia: String,
        ficha: Map<String, Any?>,
        chatHistory: List<Pair<String, String>>
    ): String {
        val nombre = ficha["nombre"] ?: "Tu personaje"

        val historial = chatHistory.joinToString("\n") { (sender, message) ->
            if (sender == "player") "$nombre: $message" else "Narrador: $message"
        }

        return """
            Eres el narrador de un juego de rol de fantasía. La historia principal:

            $historia

            Personaje del jugador:
            - Nombre: $nombre
            - Raza: ${ficha["raza"]}
            - Clase: ${ficha["clase"]}
            - Nivel: ${ficha["nivel"]}
            - HP: ${ficha["hp"]}
            - CA: ${ficha["ca"]}
            - Alineamiento: ${ficha["alineamiento"]}
            - Personalidad: ${ficha["personalidad"]}
            - Motivación: ${ficha["motivacion"]}
            - Equipo: ${ficha["equipo"]}
            - Mochila: ${ficha["mochila"]}

            Conversación previa:
            $historial

            El jugador, actuando como $nombre, dice: $input

             Responde al jugador en segunda persona. Mantén la respuesta breve (1 a 3 oraciones), enfocada en la acción o consecuencia inmediata. No salgas del mundo de juego.
    """.trimIndent()
    }

    fun process(
        userId: String,
        worldName: String,
        onResult: (String) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = RealTime()

                db.getCampaignInfo(
                    userId = userId,
                    campaignName = worldName,
                    onResult = { campaignData ->
                        val historia = (campaignData["historia"] ?: "").toString()
                            .replace("*", "")
                            .replace("\"", "")
                            .trim()

                        val characterName = (campaignData["nombre"] ?: userId).toString()

                        db.getCharacterInfo(
                            userId = userId,
                            characterName = characterName,
                            onResult = { characterData ->

                                AdventureRepository.getChatHistory(
                                    userId = userId,
                                    worldName = worldName,
                                    onResult = { chatList ->

                                        val orderedChat = chatList
                                            .sortedBy { it.timestamp }
                                            .map { it.sender to it.message }

                                        val prompt = buildPrompt(historia, characterData, orderedChat)

                                        val apiService = ApiService()
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                apiService.sendPrompt(prompt)

                                                val state = apiService.uiState.first {
                                                    it is UiState.Success || it is UiState.Error
                                                }

                                                when (state) {
                                                    is UiState.Success -> {
                                                        val aiResponse = state.response
                                                        val timestamp = System.currentTimeMillis()

                                                        AdventureRepository.addMessageToChat(
                                                            userId, worldName, "$timestamp", "player", input
                                                        )
                                                        AdventureRepository.addMessageToChat(
                                                            userId, worldName, "${timestamp + 1}", "dm", aiResponse
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
                                                Log.e("VoiceCommandPrompt", "Error en IA", e)
                                                withContext(Dispatchers.Main) {
                                                    onError(e)
                                                }
                                            }
                                        }

                                    },
                                    onError = { chatErr ->
                                        Log.e("VoiceCommandPrompt", "Error al obtener chat", chatErr)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            onError(chatErr)
                                        }
                                    }
                                )
                            },
                            onError = { err ->
                                Log.e("VoiceCommandPrompt", "Error al obtener personaje", err)
                                CoroutineScope(Dispatchers.Main).launch {
                                    onError(err)
                                }
                            }
                        )
                    },
                    onError = { err ->
                        Log.e("VoiceCommandPrompt", "Error al obtener historia", err)
                        CoroutineScope(Dispatchers.Main).launch {
                            onError(err)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("VoiceCommandPrompt", "Error inesperado", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}
