package com.pillsense.app.core.model

import com.pillsense.app.core.database.Intake
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class AdherenceAdviceTest {
    private val zone = ZoneId.of("America/Bogota")
    private val now = LocalDate.of(2026, 10, 6).atTime(20, 0).atZone(zone).toInstant().toEpochMilli()
    @Test fun `empty history invites logging without invented progress`() {
        assertTrue(AdherenceAdvice.generate(emptyList(), now, zone).contains("primeras tomas"))
    }
    @Test fun `detects repeated omissions and preserves prescribed schedule`() {
        val rows = (1..3).map { Intake("$it", "u", "m", now - it * 86_400_000L, "SKIPPED") }
        val text = AdherenceAdvice.generate(rows, now, zone)
        assertTrue(text.contains("3 tomas sin completar"))
        assertTrue(text.contains("Mantén el horario indicado"))
    }
    @Test fun `future and older records cannot trigger recommendations`() {
        val rows = listOf(Intake("old", "u", "m", now - 8 * 86_400_000L, "SKIPPED"), Intake("future", "u", "m", now + 86_400_000L))
        assertTrue(AdherenceAdvice.generate(rows, now, zone).contains("primeras tomas"))
    }
}
