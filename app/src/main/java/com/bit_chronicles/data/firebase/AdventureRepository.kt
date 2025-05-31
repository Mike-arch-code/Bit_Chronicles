package com.bit_chronicles.data.firebase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AdventureRepository {

    private val db = RealTime()

    fun createAdventure(
        userId: String,
        worldName: String,
        metadata: Map<String, Any>,
        prompt: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val basePath = "aventuras/$userId/$worldName"

        // Guardar metadata
        db.write("$basePath/metadata", metadata, onSuccess, onError)

        // Guardar prompt original
        db.write("$basePath/historia/prompt", prompt)

        // Crear carpeta de chat con mensaje inicial del sistema (IA)
        val initialMessage = mapOf(
            "sender" to "dm",
            "message" to "Hola, bienvenido al mundo de $worldName.",
            "timestamp" to System.currentTimeMillis()
        )
        db.write("$basePath/chat/0", initialMessage)
    }

    fun addMessageToChat(
        userId: String,
        worldName: String,
        messageId: String,
        sender: String,
        message: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val path = "aventuras/$userId/$worldName/chat/$messageId"
        val msg = mapOf(
            "sender" to sender,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        db.write(path, msg, onSuccess, onError)
    }
}
