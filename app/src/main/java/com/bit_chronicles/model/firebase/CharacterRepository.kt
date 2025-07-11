package com.bit_chronicles.model.firebase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CharacterRepository {

    private val db = RealTime()

    fun saveCharacter(
        userId: String,
        characterName: String,
        metadata: Map<String, Any>,
        story: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val basePath = "personajes/$userId/$characterName"

        db.write("$basePath/metadata", metadata, onSuccess, onError)
        db.write("$basePath/historia", story)
    }
}
