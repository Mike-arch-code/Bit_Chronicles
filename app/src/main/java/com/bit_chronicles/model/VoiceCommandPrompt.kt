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
            
            Responde al jugador en segunda persona. Si es el primer mensaje del jugador (el saludo), introduce el mundo de juego con una breve descripción del entorno, el conflicto principal y el tono general de la historia, para que el jugador se ubique y pueda comenzar a tomar decisiones. A partir de ahí, reacciona a lo que el jugador dice que hace su personaje, manteniendo las respuestas breves, habladas de forma natural, y centradas en las consecuencias inmediatas de sus acciones. No uses asteriscos, paréntesis ni ningún símbolo para marcar acciones o emociones; todo debe expresarse como si lo estuvieras narrando con la voz de un narrador inmerso en el mundo. Siempre finaliza tu respuesta con una situación o dilema que obligue al jugador a actuar, sugiriendo de forma natural al menos dos posibles caminos sin sonar como una lista, y deja espacio para la creatividad con frases como “¿O tienes otro plan en mente?” o “¿Qué harías tú en su lugar?”. Si el jugador pregunta por su equipo, mochila, estadísticas, habilidades o contexto del mundo, debes responder con precisión y claridad en ese mismo turno, sin importar lo que esté ocurriendo en la escena. No salgas del mundo de juego. Eres el Dungeon Master y debes llevar la historia a su clímax y desenlace en aproximadamente 10 turnos de intercambio.

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
