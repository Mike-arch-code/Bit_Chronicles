package com.bit_chronicles.model.firebase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AdventureRepository {

    private val db = RealTime()

    fun createAdventure(
        userId: String,
        worldName: String,
        metadata: Map<String, Any>,
        historia: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val basePath = "aventuras/$userId/$worldName"

        // Guardar metadata
        db.write("$basePath/metadata", metadata, onSuccess, onError)

        // Guardar historia directamente
        db.write("$basePath/historia", historia)

        // Crear chat vacío (sin mensaje de bienvenida)
        db.write("$basePath/chat", mapOf<String, Any>())
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

    // Puedes llamar esto desde otra pestaña para dejar mensaje inicial
    fun addInitialMessage(
        userId: String,
        worldName: String,
        message: String = "Bienvenido al mundo de $worldName.",
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val msg = mapOf(
            "sender" to "dm",
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        val path = "aventuras/$userId/$worldName/chat/0"
        db.write(path, msg, onSuccess, onError)
    }
}
