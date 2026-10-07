package com.pillsense.app.core.model

import com.pillsense.app.core.database.Intake
import java.time.*

object AdherenceAdvice {
    fun generate(intakes: List<Intake>, now: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val start = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val recent = intakes.filter { it.scheduledAt in start..now }
        if (recent.isEmpty()) return "Registra tus primeras tomas para conocer tus hábitos. Tus consejos aparecerán aquí de forma automática."
        val missed = recent.count { it.status in listOf("PENDING", "SKIPPED") }
        val postponed = recent.count { it.status == "POSTPONED" }
        if (missed >= 3) {
            val hour = recent.filter { it.status in listOf("PENDING", "SKIPPED") }
                .groupingBy { Instant.ofEpochMilli(it.scheduledAt).atZone(zone).hour }.eachCount().maxByOrNull { it.value }?.key
            return "Hay $missed tomas sin completar en los últimos siete días${hour?.let { ", con más olvidos alrededor de las %02d:00".format(it) }.orEmpty()}. Revisa que los avisos tengan sonido y vincula el recordatorio a una rutina que ya tengas. Mantén el horario indicado en tu receta."
        }
        if (postponed >= 2) return "Has pospuesto $postponed tomas esta semana. Activa el sonido de los avisos y confirma la toma cuando la realices para mantener un registro claro."
        if (missed > 0) return "Tienes $missed toma${if (missed == 1) "" else "s"} sin completar. Revisa el historial; si no sabes qué hacer con una dosis olvidada, consulta al profesional que indicó el tratamiento."
        return "Has registrado todas las tomas que ya correspondían. Mantén la constancia y registra cada toma cuando la realices."
    }
}
