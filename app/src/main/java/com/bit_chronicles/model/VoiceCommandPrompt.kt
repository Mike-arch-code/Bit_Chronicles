package com.bit_chronicles.model

class VoiceCommandPrompt(private val input: String) {
    fun build(): String {
        return """
            Estás en un juego de rol de fantasía. El usuario ha dicho: "$input".
            Interpreta esta frase como una orden para el personaje. Devuelve solo el comando que debe ejecutarse. 
            Usa formato simple como: "caminar", "atacar", "explorar", "hablar", etc.
            No expliques. Solo responde con una palabra o frase corta que represente la acción.
        """.trimIndent()
    }
}