package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class AnonymizedReportTest {

    @Test
    fun anonReportContainsNoRealMedicationNames() {
        val report = WeeklyAdherenceReport(
            weekStartDate = LocalDate.now(),
            weekPercentage = 80f,
            previousWeekPercentage = 75f,
            trend = 5f,
            byTimeSlot = mapOf("morning" to 85f),
            byDayOfWeek = mapOf(),
            byMedication = mapOf("med1" to 80f)
        )
        val medicationNames = mapOf("med1" to "Aspirin 500mg")
        val anon = anonymize(report, emptyList(), medicationNames)
        assertFalse(anon.byMedication.keys.any { it.contains("Aspirin") })
        assertTrue(anon.byMedication.keys.any { it.contains("Medicamento") })
    }

    @Test
    fun anonPatternLabelsAnonymized() {
        val patterns = listOf(ForgottenPattern("med1", "LOW_ADHERENCE", 2))
        val medicationNames = mapOf("med1" to "Ibuprofen 200mg")
        val anon = anonymize(WeeklyAdherenceReport(LocalDate.now(), 0f, 0f, 0f, mapOf(), mapOf(), mapOf()), patterns, medicationNames)
        assertTrue(anon.patterns.all { !it.medicationLabel.contains("Ibuprofen") })
    }

    @Test
    fun deanonymizeRestoresMapping() {
        val medicationNames = mapOf("m1" to "Drug1", "m2" to "Drug2")
        val reverse = deanonymize(AnonymizedReport(0f, 0f, 0f, mapOf(), mapOf(), mapOf(), emptyList()), medicationNames)
        assertEquals("Drug1", reverse["Medicamento 1"])
        assertEquals("Drug2", reverse["Medicamento 2"])
    }
}
