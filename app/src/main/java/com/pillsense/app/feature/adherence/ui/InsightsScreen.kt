package com.pillsense.app.feature.adherence.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pillsense.app.core.database.*
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.model.Schedule
import com.pillsense.app.core.model.AdherenceAdvice
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun InsightsScreen(intakes: List<Intake>, medications: List<Medication>) {
    val now = System.currentTimeMillis()
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val recent = intakes.filter { it.scheduledAt in today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()..now }
    val average = Schedule.adherence(recent, now)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Text("ÚLTIMOS 7 DÍAS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Mi progreso", style = MaterialTheme.typography.displayMedium) }
        item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { (average ?: 0) / 100f }, modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.tertiary, trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = .12f), strokeWidth = 10.dp)
                    Text(average?.let { "$it%" } ?: "—", style = MaterialTheme.typography.headlineMedium)
                }
                Column { Text("Adherencia semanal", style = MaterialTheme.typography.titleMedium)
                    Text(if (average == null) "Tus estadísticas aparecerán con las primeras tomas." else "${recent.count { it.status == "TAKEN" }} de ${recent.size} dosis vencidas registradas como tomadas.", style = MaterialTheme.typography.bodyMedium) }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                (6 downTo 0).forEach { offset ->
                    val day = today.minusDays(offset.toLong())
                    val value = Schedule.adherence(recent.filter { Instant.ofEpochMilli(it.scheduledAt).atZone(zone).toLocalDate() == day }, now)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(value?.toString() ?: "—", style = MaterialTheme.typography.labelSmall)
                        Box(Modifier.height(96.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondary.copy(alpha = .1f)), contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.fillMaxWidth().fillMaxHeight((value ?: 0) / 100f).background(if (offset == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary))
                        }
                        Text(day.format(DateTimeFormatter.ofPattern("EE")).take(2), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } } }
        item { PsSectionHeader("Recomendaciones")
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pequeños hábitos, más constancia", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(AdherenceAdvice.generate(recent, now))
                PsBadge("Basado en tus registros · sin conexión")
            } }
        }
        item { PsSectionHeader("Por medicamento") }
        medications.filter { med -> recent.any { it.medicationId == med.id } }.forEach { med ->
            item(key = med.id) { val value = Schedule.adherence(recent.filter { it.medicationId == med.id }, now) ?: 0
                PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${med.name} · $value%", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { value / 100f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary)
                } }
            }
        }
    }
}
