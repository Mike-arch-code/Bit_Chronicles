package com.bit_chronicles.model.firebase

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CharacterRepository {

    private val db = RealTime()

    fun saveCharacter(
        userId: String,
        characterName: String,
        parsedData: Map<String, Any>,
        story: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val basePath = "personajes/$userId/$characterName"

        // Agrega la historia al parsedData
        val completeData = parsedData.toMutableMap().apply {
            put("historia", story)
            put("fecha_creacion", date)
        }

        val sanitizedData = sanitizeKeys(completeData)

        db.write(basePath, sanitizedData, onSuccess, onError)
    }

    private fun sanitizeKeys(map: Map<String, Any?>): Map<String, Any?> {
        val illegalChars = Regex("[./#\$\\[\\]\n\r\t]")
        val whitespace = Regex("\\s+")

        return map.mapNotNull { (key, value) ->
            val sanitizedKey = key
                .replace(illegalChars, "")
                .replace(whitespace, "_")
                .takeIf { it.isNotBlank() } ?: return@mapNotNull null

            val sanitizedValue = when (value) {
                is Map<*, *> -> sanitizeKeys(value as Map<String, Any?>)
                is List<*> -> value.map {
                    if (it is Map<*, *>) {
                        sanitizeKeys(it as Map<String, Any?>)
                    } else it
                }
                else -> value
            }

            sanitizedKey to sanitizedValue
        }.toMap()
    }
}
