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
        chatHistory: List<Pair<String, String>>,
        turnos: String,
        players: String,
        jugadorActual: String,
        playerList: List<String>
    ): String {
        val nombre = ficha["nombre"] ?: jugadorActual

        val historial = mutableListOf<String>()

        var playerIndex = 0

        for ((sender, message) in chatHistory) {
            if (sender == "player") {
                val nombreJugador = playerList[playerIndex % playerList.size]
                historial.add("$nombreJugador: $message")
                playerIndex++
            } else {
                historial.add("Narrador: $message")
            }
        }

        val historialTexto = historial.joinToString("\n")


        // Calculamos otros jugadores (compañeros)
        val compañeros = players.split(",")
            .map { it.trim() }
            .filter { it != jugadorActual }

        val compañerosTexto = if (compañeros.isNotEmpty()) {
            "Tus compañeros de aventura son: ${compañeros.joinToString(", ")}. Están presentes en la escena y pueden actuar o reaccionar, pero solo tú tienes el turno activo ahora."
        } else {
            "Estás solo en esta aventura, al menos por ahora."
        }

        val tirada = 10

        return """
        Eres el narrador de un juego de rol de fantasía. La historia principal:

        $historia

        Personaje del jugador actual:
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

        $compañerosTexto

        Conversación previa:
        $historial

        El jugador, actuando como $nombre, dice: $input
        
        Responde al jugador en segunda persona. [...] (resto del prompt sin cambios)
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

                        val turnos = (campaignData["turnos"] ?: userId).toString()
                        val players = (campaignData["players"] ?: userId).toString()
                        val playerList = players.split(",").map { it.trim() }


                        AdventureRepository.getChatHistory(
                            userId = userId,
                            worldName = worldName,
                            onResult = { chatList ->

                                val orderedChat = chatList.sortedBy { it.timestamp }

                                val playerMessages = orderedChat.filter { it.sender == "player" }
                                val currentPlayerIndex = playerMessages.size % playerList.size
                                val currentPlayerName = playerList[currentPlayerIndex]

                                Log.d("JugadorActual", "Es el turno de: $currentPlayerName")

                                db.getCharacterInfo(
                                    userId = userId,
                                    characterName = currentPlayerName,
                                    onResult = { characterData ->

                                        val chatHistory = orderedChat.map { it.sender to it.message }

                                        val prompt = buildPrompt(
                                            historia = historia,
                                            ficha = characterData,
                                            chatHistory = chatHistory,
                                            turnos = turnos,
                                            players = players,
                                            jugadorActual = currentPlayerName,
                                            playerList = playerList
                                        )

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
                                    onError = { err ->
                                        Log.e("VoiceCommandPrompt", "Error al obtener personaje", err)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            onError(err)
                                        }
                                    }
                                )
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
