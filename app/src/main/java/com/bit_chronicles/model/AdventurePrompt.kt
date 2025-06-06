package com.bit_chronicles.model

class AdventurePrompt(
    private val worldName: String,
    private val settingType: String,
    private val dominantRaces: String,
    private val powerSystem: String,
    private val mainConflict: String,
    private val worldLore: String,
    private val tone: String
) {
    fun buildPromptString(): String {
        return """
            Imagina un mundo llamado **$worldName**, ambientado en un escenario de **$settingType**. 
            En este universo habitan principalmente **$dominantRaces**, cuyas culturas y alianzas moldean el destino de las tierras.

            La fuente de poder que rige este mundo es **$powerSystem**, y su uso ha influenciado profundamente el desarrollo de civilizaciones y conflictos.

            Actualmente, el mundo se ve amenazado por un conflicto central: **$mainConflict**, un evento que altera el equilibrio de todo lo conocido.

            El trasfondo de este mundo incluye la siguiente historia: *$worldLore*.

            Quiero que crees una introducción narrativa para una aventura de rol inspirada en Dungeons & Dragons, con un tono **$tone**. 
            La historia debe ser inmersiva, coherente y ofrecer un gancho poderoso para que los jugadores quieran explorar este universo.
        """.trimIndent()
    }
}