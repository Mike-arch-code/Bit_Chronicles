package com.bit_chronicles.model.proms

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
            Eres un generador experto de personajes para Dungeons & Dragons 5e.

            Crea la ficha de personaje a nivel 1 con base en los siguientes datos:

            - Nombre: $name
            - Raza: $race
            - Clase: $characterClass
            - Trasfondo: $background
            - Alineamiento: $alignment
            - Rasgos de personalidad: $personalityTraits
            - Habilidades iniciales (no estadísticas): $abilities
            - Motivación personal: $motivation

            Formato de salida requerido (no agregues explicaciones ni historia libre):

            Nombre: [nombre]  
            Raza: [raza]  
            Clase: [clase]  
            Nivel: 1  
            HP: [puntos de golpe]  
            CA: [clase de armadura]  
            Velocidad: [pies]  
            Alineamiento: [alineamiento]  
            Personalidad: [frase corta]  
            Motivación: [frase corta]  
            Estadísticas: Fuerza [x], Destreza [x], Constitución [x], Inteligencia [x], Sabiduría [x], Carisma [x]  
            Bonificador de Competencia: +2  
            Tiradas de Salvación: [atributo] +[x], ...  
            Habilidades: [nombre] +[x], ...  
            Ataques:  
            - [arma]: +[x] al ataque, [daño] daño [tipo]  
            Habilidades Especiales:  
            - [nombre]: [descripción breve]  
            Equipo: [lista de objetos importantes]  
            Mochila: [contenido de la mochila de aventurero o ladrón]  
            Oro: [número] PO

            No inventes una historia extensa. Solo responde con el contenido formateado como si fuera una ficha para cargar en una app.
        """.trimIndent()
    }
}