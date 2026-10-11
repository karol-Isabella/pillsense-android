package com.pillsense.app.core.ai

import org.junit.Assert.*
import org.junit.Test

class MedicationTextParserTest {
    @Test fun `reads only printed strength and never invents frequency`() {
        val result = MedicationTextParser.parse("Amoxicilina\n500 mg\n20 cápsulas")
        assertEquals("Amoxicilina", result.nombre)
        assertEquals("500 mg", result.dosis)
        assertEquals("", result.frecuencia)
        assertNull(result.duracionDias)
        assertEquals("Cápsula", result.forma)
    }
    @Test fun `extracts explicit prescription and preserves original text`() {
        val text = "Amoxicilina 500 mg\nTomar cada 8 horas durante 7 días"
        val result = MedicationTextParser.parse(text)
        assertEquals("Cada 8 horas", result.frecuencia)
        assertEquals(7, result.duracionDias)
        assertEquals(text, result.textoOriginal)
        assertEquals("Tomar cada 8 horas durante 7 días", result.indicaciones)
    }
    @Test fun `keeps concentration denominator`() {
        val result = MedicationTextParser.parse("Amoxicilina\n250 mg/5 ml\nJarabe")
        assertEquals("250 mg/5 ml", result.dosis)
        assertEquals("Jarabe", result.forma)
    }
    @Test fun `unreadable image has no fabricated medication`() {
        val result = MedicationTextParser.parse("\n  ")
        assertFalse(result.esExitoso)
        assertTrue(result.nombre.isEmpty())
        assertTrue(result.dosis.isEmpty())
    }
    @Test fun `name alone cannot supply a dose or schedule`() {
        val result = MedicationTextParser.parse("Metformina")
        assertEquals("Metformina", result.nombre)
        assertEquals("", result.dosis)
        assertEquals("", result.frecuencia)
        assertEquals("", result.indicaciones)
    }
    
    @Test fun `filters acronyms and selects correct name from problematic Acetaminofén box`() {
        val text = "AcetaminofSn\nACETAMINOTEN\nWBPM\n500 mg\nTableta"
        val result = MedicationTextParser.parse(text)
        // Debe descartar WBPM (sigla), y elegir entre AcetaminofSn y ACETAMINOTEN
        // Si hay OCR con información de tamaño, elige la línea más grande
        // Sin tamaño, elige la primera línea válida que no sea sigla
        assertEquals("500 mg", result.dosis)
        assertEquals("Tableta", result.forma)
        // El nombre debe ser vacío o similar a Acetaminofén, NUNCA "WBPM"
        assertNotEquals("WBPM", result.nombre)
        assertTrue(result.nombre.isEmpty() || result.nombre.contains("Acetaminof", ignoreCase = true) || result.nombre.contains("Acetaminoten", ignoreCase = true))
    }
    
    @Test fun `rejects lines with fewer than 3 letters`() {
        val text = "A\nBx\nMetformina\n500 mg"
        val result = MedicationTextParser.parse(text)
        // Debe descartar "A" y "Bx", elegir "Metformina"
        assertEquals("Metformina", result.nombre)
        assertEquals("500 mg", result.dosis)
    }
    
    @Test fun `ignores lines without real letters`() {
        val text = "123\n---\nAmoxicilina\n250 mg"
        val result = MedicationTextParser.parse(text)
        assertEquals("Amoxicilina", result.nombre)
        assertEquals("250 mg", result.dosis)
    }
    
    @Test fun `handles multiple dose patterns correctly`() {
        val result = MedicationTextParser.parse("Acetaminofén\n250 Tabeta\n500 mg\nTableta")
        assertEquals("500 mg", result.dosis)
        assertEquals("Tableta", result.forma)
    }
    
    @Test fun `prefers larger font size for name selection when available`() {
        val lines = listOf(
            OcrLine("WBPM", 10f),          // sigla, pequeña, debe ser rechazada
            OcrLine("Acetaminofén", 24f)  // nombre, grande, debe ser seleccionada
        )
        val result = MedicationTextParser.parse("WBPM\nAcetaminofén\n500 mg", lines)
        assertEquals("Acetaminofén", result.nombre)
        assertEquals("500 mg", result.dosis)
    }
}
