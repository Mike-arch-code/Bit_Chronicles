package com.bit_chronicles.model

object CharacterParser {

    fun parseCharacterData(historia: String): Map<String, Any> {
        val data = mutableMapOf<String, Any>()

        fun extract(pattern: String, default: String = ""): String {
            return Regex(pattern, RegexOption.DOT_MATCHES_ALL)
                .find(historia)
                ?.groupValues?.get(1)
                ?.trim() ?: default
        }

        fun extractMap(section: String): Map<String, String> {
            return extract("$section:\\s*(.*?)\\s+\\w+:")
                .split(",")
                .mapNotNull {
                    val parts = it.trim().split(" ")
                    if (parts.size >= 2) {
                        val key = parts.dropLast(1).joinToString(" ")
                        val value = parts.last()
                        key to value
                    } else null
                }.toMap()
        }

        fun extractList(sectionName: String): List<String> {
            val regex = Regex("$sectionName:\\s*(-\\s.*?)(?=\\n\\w+:|\\z)", RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(historia)?.groupValues?.get(1) ?: return emptyList()
            return match.lines()
                .filter { it.trim().startsWith("-") }
                .map { it.removePrefix("-").trim() }
        }

        // Datos básicos
        data["nombre"] = extract("Nombre:\\s*(.*?)\\s+Raza:")
        data["raza"] = extract("Raza:\\s*(.*?)\\s+Clase:")
        data["clase"] = extract("Clase:\\s*(.*?)\\s+Nivel:")
        data["nivel"] = extract("Nivel:\\s*(\\d+)")
        data["hp"] = extract("HP:\\s*(\\d+)")
        data["ca"] = extract("CA:\\s*(\\d+)")

        // Contexto
        data["alineamiento"] = extract("Alineamiento:\\s*(.*?)\\s+Personalidad:")
        data["personalidad"] = extract("Personalidad:\\s*(.*?)\\s+Motivación:")
        data["motivacion"] = extract("Motivación:\\s*(.*?)\\s+Estadísticas:")

        // Estadísticas y otros mapas
        data["estadisticas"] = extractMap("Estadísticas")
        data["tiradasSalvacion"] = extractMap("Tiradas de Salvación")
        data["habilidades"] = extractMap("Habilidades")

        // Listas
        data["habilidadesEspeciales"] = extractList("Habilidades Especiales")

        // Inventario
        data["equipo"] = extract("Equipo:\\s*(.*?)\\s+Mochila:")
        data["mochila"] = extract("Mochila:\\s*(.*?)\\s+Oro:")
        data["oro"] = extract("Oro:\\s*(\\d+)").toIntOrNull() ?: 0

        return data
    }
}
