package com.bit_chronicles.model

class AdventurePrompt(
    private val worldName: String,
    private val settingType: String,
    private val dominantRaces: String,
    private val powerSystem: String,
    private val mainConflict: String,
    private val worldLore: String,
    private val tone: String,
    private val keyLocations: String,
    private val keyCharacters: String,
    private val hooks: String,
    private val objective: String
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
        - Personajes importantes: $keyCharacters
        - Ganchos narrativos: $hooks
        - Objetivo de los jugadores: $objective

        Requisitos:
        - Comienza con una escena fuerte (una batalla, un descubrimiento, una traición, etc.).
        - Integra los elementos del mundo sin sonar a lista o ficha técnica.
        - Mantén un tono **$tone**.
        - No uses frases cliché como “el viento sopla”, “tierras olvidadas”, “ecos del pasado”.

        Escribe como si fuera la introducción oral de un narrador que atrapa a sus jugadores desde la primera frase.
    """.trimIndent()
    }

}