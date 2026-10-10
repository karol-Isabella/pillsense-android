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
}
