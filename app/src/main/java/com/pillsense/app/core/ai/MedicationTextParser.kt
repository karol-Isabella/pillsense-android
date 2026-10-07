package com.pillsense.app.core.ai

/** Conservative suggestions from literal OCR text. Never supplies a missing prescription. */
object MedicationTextParser {
    private val strength = Regex("""\b\d+(?:[.,]\d+)?\s*(?:mcg|µg|mg|g|ml|UI|%)(?:\s*/\s*\d*(?:[.,]\d+)?\s*(?:ml|mg|g))?\b""", RegexOption.IGNORE_CASE)
    private val interval = Regex("""\b(?:cada|every)\s+(\d{1,2})\s*(?:horas?|hours?|h)\b""", RegexOption.IGNORE_CASE)
    private val duration = Regex("""\b(?:durante|por|for)\s+(\d{1,3})\s*(?:d[ií]as?|days?)\b""", RegexOption.IGNORE_CASE)
    private val excluded = Regex("""(?i)^(?:tomar|take|dosis|dose|indicaciones|v[ií]a|oral|registro|lote|vence|exp|fabricado|laboratorio|paciente|doctor|receta|contenido|mantener)\b""")
    fun parse(text: String): MedicationExtractResult {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return MedicationExtractResult(error = "No se encontró texto legible. Prueba con más luz o registra manualmente.")
        val dose = strength.find(text)?.value.orEmpty()
        val doseLine = lines.indexOfFirst { strength.containsMatchIn(it) }
        val candidates = if (doseLine >= 0) listOf(lines[doseLine]) + lines.take(doseLine).asReversed() else lines
        val name = candidates.map { strength.replace(it, "").trim(' ', '-', ':', '·') }
            .firstOrNull { it.length in 3..80 && it.count(Char::isLetter) >= 3 && !excluded.containsMatchIn(it) && !interval.containsMatchIn(it) }.orEmpty()
        val hours = interval.find(text)?.groupValues?.get(1)?.toIntOrNull()
        val frequency = when {
            hours != null -> if (hours == 24) "Una vez al día" else "Cada $hours horas"
            Regex("(?i)una vez al d[ií]a|once daily").containsMatchIn(text) -> "Una vez al día"
            else -> ""
        }
        val form = when {
            Regex("(?i)c[aá]psul|capsule").containsMatchIn(text) -> "Cápsula"
            Regex("(?i)tableta|comprimido|tablet").containsMatchIn(text) -> "Tableta"
            Regex("(?i)jarabe|syrup").containsMatchIn(text) -> "Jarabe"
            Regex("(?i)gotas|drops").containsMatchIn(text) -> "Gotas"
            Regex("(?i)inyecci[oó]n|inject").containsMatchIn(text) -> "Inyección"
            else -> ""
        }
        return MedicationExtractResult(nombre = name, dosis = dose, frecuencia = frequency, forma = form,
            duracionDias = duration.find(text)?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it > 0 },
            esExitoso = true, textoOriginal = text,
            indicaciones = lines.filter { Regex("(?i)^(tomar|administrar|con alimentos|con el|despu[eé]s de|antes de|take)\\b").containsMatchIn(it) }.joinToString("\n"),
            error = "Son sugerencias del texto leído. Verifica cada campo con la receta; no se infieren dosis ni frecuencias ausentes.")
    }
}
