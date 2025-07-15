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
                val nombreJugador = jugadorActual
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
        Eres el narrador de un juego de rol de fantasía con varios jugadores. La historia principal es:

        $historia

        Personaje del jugador actual:
        - Nombre: $jugadorActual
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

        El jugador, actuando como $jugadorActual, dice: $input
        
        Responde en segunda persona y en voz natural. Si es el primer mensaje de un jugador, presenta brevemente el mundo, el conflicto principal y el tono, sin frases cliché como “tierras olvidadas” o “el viento sopla”. A partir de ahí, las respuestas deben tener alrededor de **tres frases**, centradas en las consecuencias inmediatas de las acciones. Solo da respuestas más largas si el jugador explícitamente pide contexto o explicación.

        Nunca uses símbolos como asteriscos, guiones o paréntesis. Eres un narrador oral, dentro del mundo, hablando como Dungeon Master.
        
        Cuando el jugador haga una acción peligrosa o de combate, lanza una tirada externa `$tirada` del 1 al 20. Menciona cuánto salió. Aplica bonificadores según sus estadísticas relevantes (fuerza, destreza, clase, equipo, etc.) y compáralo con la dificultad o la CA del enemigo. Si tiene éxito, indica el daño infligido y cuántos HP le quedan al enemigo. Si falla, describe las consecuencias, el daño recibido (si aplica) y cuántos HP le quedan al jugador. Si su HP llega a 0, puede morir; narra esa muerte de forma impactante pero coherente.
        
        Asigna estadísticas internas a los enemigos según su tipo:
        - **Básicos**: HP bajo (5–10), sin habilidades, vencibles en 1–2 turnos.
        - **Medianos**: HP 15–25, daño moderado.
        - **Fuertes**: HP 30–50, ataques especiales o defensas altas.
        - **Jefes**: HP 60+, múltiples fases o habilidades, combates prolongados.
        
        Usa estas estadísticas para que el combate sea justo y escale adecuadamente. **No uses tiradas ni modificadores para acciones como caminar, hablar, observar, revisar inventario o pedir información.**
        
        Si el jugador pide ver su inventario, mochila, estadísticas, habilidades, equipo o contexto, dáselo en el mismo turno sin omitir nada, sin importar lo que esté pasando en la escena.
        
        Tu respuesta debe dividirse claramente en dos mitades si hay más de un jugador:
        
        1. **Parte del jugador actual ($nombre)**: Di su nombre. Narra su acción, el resultado de la tirada `$tirada`, las consecuencias, y termina en una situación que lo obligue a decidir.
        
        2. **Parte para el siguiente jugador : Marca el cambio diciendo su nombre, luego describe lo que ve, siente o cómo lo afecta lo ocurrido. No le des una lista. Invítalo a decidir con frases como “¿Qué haces tú?” o “¿Cómo reaccionas ante esto?”
        
        Mantén el control de la narrativa y lleva la historia a su clímax y desenlace en aproximadamente $turnos turnos. Tu voz es la del mundo.
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
                                                            userId, worldName, "$timestamp", currentPlayerName , input
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
