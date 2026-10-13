package com.pillsense.app.core.util

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class IntakeLogicTest {

    private val baseTime = System.currentTimeMillis()

    @Test
    fun pendingVencidoHace3DiasMarcoComoSkipped() {
        val scheduledAt = baseTime - TimeUnit.DAYS.toMillis(3)
        val shouldSkip = IntakeLogic.shouldMarkAsSkipped("PENDING", scheduledAt, baseTime)
        assertTrue("Dosis vencida hace 3 días debe marcarse como SKIPPED", shouldSkip)
    }

    @Test
    fun pendingHace30MinutosSeguidoComoPending() {
        val scheduledAt = baseTime - TimeUnit.MINUTES.toMillis(30)
        val shouldSkip = IntakeLogic.shouldMarkAsSkipped("PENDING", scheduledAt, baseTime)
        assertFalse("Dosis hace 30 minutos sigue como PENDING (dentro de gracia)", shouldSkip)
    }

    @Test
    fun postponedNoMarcaComoSkipped() {
        val scheduledAt = baseTime - TimeUnit.DAYS.toMillis(5)
        val shouldSkip = IntakeLogic.shouldMarkAsSkipped("POSTPONED", scheduledAt, baseTime)
        assertFalse("POSTPONED nunca debe marcarse como SKIPPED", shouldSkip)
    }

    @Test
    fun takenNoMarcaComoSkipped() {
        val scheduledAt = baseTime - TimeUnit.DAYS.toMillis(5)
        val shouldSkip = IntakeLogic.shouldMarkAsSkipped("TAKEN", scheduledAt, baseTime)
        assertFalse("TAKEN nunca debe marcarse como SKIPPED", shouldSkip)
    }

    @Test
    fun skippedNoMarcaComoSkipped() {
        val scheduledAt = baseTime - TimeUnit.MINUTES.toMillis(30)
        val shouldSkip = IntakeLogic.shouldMarkAsSkipped("SKIPPED", scheduledAt, baseTime)
        assertFalse("SKIPPED ya marcado no cambia", shouldSkip)
    }

    @Test
    fun pendingVencidoExactamente61MinutosMarcoComoSkipped() {
        val scheduledAt = baseTime - TimeUnit.MINUTES.toMillis(61)
        val shouldSkip = IntakeLogic.shouldMarkAsSkipped("PENDING", scheduledAt, baseTime)
        assertTrue("Dosis vencida 61 minutos debe marcarse como SKIPPED", shouldSkip)
    }
}
