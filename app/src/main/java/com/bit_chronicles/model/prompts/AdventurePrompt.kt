package com.bit_chronicles.model.proms

class AdventurePrompt(
    private val worldName: String,
    private val settingType: String,
    private val dominantRaces: String,
    private val powerSystem: String,
    private val mainConflict: String,
    private val worldLore: String,
    private val tone: String,
    private val keyLocations: String,
    private val objective: String,
    private val turnos: String,
    private val playerHistory: String
) {
    fun buildPromptString(): String {
        return """
        Eres un narrador experto en campañas de rol tipo Dungeons & Dragons.

        Tu misión es redactar una introducción narrativa **original y única**, que comience con una escena impactante, emocional o extraña —nunca con “el viento sopla”, “las montañas” o descripciones genéricas del clima.

        Usa el siguiente material como trasfondo del mundo. No repitas literalmente los datos, sino que intégralos de manera creativa y contextual:

        - Nombre del mundo: $worldName
        - Ambientación: $settingType
        - Razas o facciones dominantes: $dominantRaces
        - Sistema de poder o magia: $powerSystem
        - Conflicto principal: $mainConflict
        - Historia previa del mundo: $worldLore
        - Lugares clave: $keyLocations
        - Objetivo de los jugadores: $objective
        - Duración estimada de la campaña: $turnos

        Información adicional del personaje principal del jugador:
        $playerHistory

        Requisitos:
        - comienza dando un contexto del mundo para terminar en una esena fuerte apenas para que un jugador empeice la historia poco antes del conflicto(una batalla, un descubrimiento, una traición, etc.)..
        - Integra los elementos del mundo sin sonar a lista o ficha técnica.
        - Mantén un tono **$tone**.
        - No uses frases cliché como “el viento sopla”, “tierras olvidadas”, “ecos del pasado”.

        Escribe como si fuera la introducción oral de un narrador Dungeon master que atrapa a sus jugadores desde la primera frase.
    """.trimIndent()
    }
}
