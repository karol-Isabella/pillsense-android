package com.pillsense.app.feature.intake.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pillsense.app.core.database.*
import com.pillsense.app.core.designsystem.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(intakes: List<Intake>, medications: List<Medication>) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val now = System.currentTimeMillis()
    val history = intakes.filter { it.scheduledAt <= now || it.actedAt != null }
    val states = listOf(null, "TAKEN", "POSTPONED", "SKIPPED")
    val visible = history.filter { states[filter] == null || it.status == states[filter] }
    val names = medications.associateBy { it.id }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("TU REGISTRO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Historial", style = MaterialTheme.typography.displayMedium) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("TAKEN" to "Tomadas", "POSTPONED" to "Pospuestas", "SKIPPED" to "Omitidas").forEach { (state, label) ->
                PsCard(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) {
                    Text(history.count { it.status == state }.toString(), style = MaterialTheme.typography.headlineMedium,
                        color = if (state == "TAKEN") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                } }
            }
        } }
        item { PsSegmentedControl(listOf("Todas", "Tomadas", "Posp.", "Omitidas"), filter, { filter = it }) }
        if (visible.isEmpty()) item { PsCard(Modifier.fillMaxWidth()) { Text("Todavía no hay tomas en este filtro.", Modifier.padding(24.dp)) } }
        val grouped = visible.groupBy { Instant.ofEpochMilli(it.scheduledAt).atZone(ZoneId.systemDefault()).toLocalDate() }
        grouped.forEach { (day, entries) ->
            item(key = day.toString()) { Text(day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), style = MaterialTheme.typography.labelLarge) }
            items(entries, key = { it.id }) { intake ->
                val med = names[intake.medicationId]
                PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(med?.name ?: "Medicamento", style = MaterialTheme.typography.titleMedium)
                    Text("${med?.dose.orEmpty()} · ${Instant.ofEpochMilli(intake.scheduledAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PsBadge(when (intake.status) { "TAKEN" -> "Tomada"; "POSTPONED" -> "Pospuesta"; "SKIPPED" -> "Omitida"; else -> "Sin registrar" })
                    intake.actedAt?.let { Text("Registro: ${Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM, HH:mm"))}", style = MaterialTheme.typography.bodySmall) }
                } }
            }
        }
    }
}
