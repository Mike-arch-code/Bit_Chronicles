package com.bit_chronicles.model.character

class CharacterPrompt(
    private val name: String,
    private val race: String,
    private val characterClass: String,
    private val background: String,
    private val alignment: String,
    private val personalityTraits: String,
    private val abilities: String,
    private val motivation: String
) {
    fun buildPromptString(): String {
        return """
            Eres un diseñador experto en creación de personajes jugables para juegos de rol de fantasía.

            Con los siguientes datos, genera una ficha descriptiva breve que incluya:

            1. **Introducción temática (máx. 4 líneas)**: Presentación atractiva del personaje con una idea clara de quién es.
            2. **Habilidades clave**: Lista de las habilidades o talentos destacables y cómo se reflejan en el juego (máx. 3 habilidades).
            3. **Rasgos de personalidad**: Cómo influye su personalidad en decisiones o interacción con el grupo.
            4. **Motivación**: Qué lo impulsa y cómo eso puede integrarse en la narrativa o las misiones.

            Datos del personaje:
            - Nombre: $name
            - Raza: $race
            - Clase: $characterClass
            - Trasfondo: $background
            - Alineamiento: $alignment
            - Rasgos de personalidad: $personalityTraits
            - Habilidades: $abilities
            - Motivación: $motivation

            El resultado debe ser conciso, organizado, sin clichés genéricos, ideal para presentarlo a un grupo antes de iniciar una campaña.
        """.trimIndent()
    }
}

