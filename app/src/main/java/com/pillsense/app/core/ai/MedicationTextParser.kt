package com.pillsense.app.core.ai

/** Conservative suggestions from literal OCR text. Never supplies a missing prescription. */
object MedicationTextParser {
    private val strength = Regex("""\b\d+(?:[.,]\d+)?\s*(?:mcg|µg|mg|g|ml|UI|%)(?:\s*/\s*\d*(?:[.,]\d+)?\s*(?:ml|mg|g))?\b""", RegexOption.IGNORE_CASE)
    private val interval = Regex("""\b(?:cada|every)\s+(\d{1,2})\s*(?:horas?|hours?|h)\b""", RegexOption.IGNORE_CASE)
    private val duration = Regex("""\b(?:durante|por|for)\s+(\d{1,3})\s*(?:d[ií]as?|days?)\b""", RegexOption.IGNORE_CASE)
    private val excluded = Regex("""(?i)^(?:tomar|take|dosis|dose|indicaciones|v[ií]a|oral|registro|lote|vence|exp|fabricado|laboratorio|paciente|doctor|receta|contenido|mantener|tabletas|cápsulas|comprimidos|tableta|cápsula|comprimido)\b""")
    
    fun parse(text: String, ocrLines: List<OcrLine> = emptyList()): MedicationExtractResult {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotBlank() && isValidLine(it) }.toList()
        if (lines.isEmpty()) return MedicationExtractResult(error = "No se encontró texto legible. Prueba con más luz o registra manualmente.")
        
        val dose = strength.find(text)?.value.orEmpty()
        val doseLine = lines.indexOfFirst { strength.containsMatchIn(it) }
        
        // Seleccionar nombre basado en tamaño de línea (mayor altura = mayor tamaño de letra)
        val name = selectNameBySize(lines, ocrLines, doseLine)
        
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
    
    private fun isValidLine(line: String): Boolean {
        // Descartar líneas de 1 a 2 caracteres
        if (line.length <= 2) return false
        // Descartar líneas sin letras reales
        if (line.count(Char::isLetter) < 1) return false
        return true
    }
    
    private fun selectNameBySize(lines: List<String>, ocrLines: List<OcrLine>, doseLine: Int): String {
        if (ocrLines.isEmpty()) {
            // Fallback si no hay información de tamaño
            return selectNameFallback(lines, doseLine)
        }
        
        // Mapear líneas a su altura (tamaño)
        val lineHeights = mutableMapOf<String, Float>()
        ocrLines.forEach { ocrLine ->
            val normalized = ocrLine.text.trim()
            if (normalized.isNotEmpty() && isValidLine(normalized)) {
                lineHeights[normalized] = maxOf(lineHeights[normalized] ?: 0f, ocrLine.height)
            }
        }
        
        // Ordenar candidatos por tamaño (mayor altura primero)
        val candidates = if (doseLine >= 0) {
            listOf(lines[doseLine]) + lines.take(doseLine).asReversed()
        } else {
            lines
        }
        
        // Limpiar y filtrar candidatos
        val cleaned = candidates.map { strength.replace(it, "").trim(' ', '-', ':', '·') }
            .filter { it.length in 3..80 && it.count(Char::isLetter) >= 3 && !excluded.containsMatchIn(it) && !interval.containsMatchIn(it) }
            .filter { !isAcronym(it) } // Descartar siglas como WBPM, BPM, INVIMA
        
        // Seleccionar por tamaño (la línea más grande)
        return cleaned.maxByOrNull { lineHeights[it] ?: 0f } ?: ""
    }
    
    private fun selectNameFallback(lines: List<String>, doseLine: Int): String {
        val candidates = if (doseLine >= 0) listOf(lines[doseLine]) + lines.take(doseLine).asReversed() else lines
        return candidates.map { strength.replace(it, "").trim(' ', '-', ':', '·') }
            .firstOrNull { it.length in 3..80 && it.count(Char::isLetter) >= 3 && !excluded.containsMatchIn(it) && !interval.containsMatchIn(it) && !isAcronym(it) } ?: ""
    }
    
    private fun isAcronym(text: String): Boolean {
        // Descartar candidatos de 4 letras o menos, todo mayúsculas, sin vocales
        if (text.length > 4) return false
        if (!text.all(Char::isLetter)) return false
        if (text != text.uppercase()) return false
        val vowels = setOf('A', 'E', 'I', 'O', 'U')
        if (text.none { it in vowels }) return true
        return false
    }
}
