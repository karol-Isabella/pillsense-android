package com.pillsense.app.feature.intake.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pillsense.app.core.database.*
import androidx.compose.ui.res.stringResource
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(intakes: List<Intake>, medications: List<Medication>, language: String = "es") {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val now = System.currentTimeMillis()
    val history = intakes.filter { it.scheduledAt <= now || it.actedAt != null }
    val states = listOf(null, "TAKEN", "POSTPONED", "SKIPPED")
    val visible = history.filter { states[filter] == null || it.status == states[filter] }
    val names = medications.associateBy { it.id }
    val appLocale = if (language == "en") java.util.Locale.ENGLISH else java.util.Locale("es")
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.history_heading), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.displayMedium) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("TAKEN" to stringResource(R.string.status_taken), "POSTPONED" to stringResource(R.string.status_postponed), "SKIPPED" to stringResource(R.string.status_skipped)).forEach { (state, label) ->
                PsCard(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) {
                    Text(history.count { it.status == state }.toString(), style = MaterialTheme.typography.headlineMedium,
                        color = if (state == "TAKEN") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                } }
            }
        } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(stringResource(R.string.history_all) to 0, stringResource(R.string.history_taken) to 1, stringResource(R.string.history_postponed) to 2, stringResource(R.string.history_skipped) to 3).forEach { (label, idx) ->
                FilterChip(selected = filter == idx, onClick = { filter = idx }, label = { Text(label, maxLines = 1, softWrap = false) })
            }
        } }
        if (visible.isEmpty()) item { PsCard(Modifier.fillMaxWidth()) { Text(stringResource(R.string.history_empty), Modifier.padding(24.dp)) } }
        val grouped = visible.groupBy { Instant.ofEpochMilli(it.scheduledAt).atZone(ZoneId.systemDefault()).toLocalDate() }
        grouped.forEach { (day, entries) ->
            item(key = day.toString()) { Text(day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", appLocale)), style = MaterialTheme.typography.labelLarge) }
            items(entries, key = { it.id }) { intake ->
                val med = names[intake.medicationId]
                PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(med?.name ?: stringResource(R.string.confirm_fallback), style = MaterialTheme.typography.titleMedium)
                    Text("${med?.dose.orEmpty()} · ${Instant.ofEpochMilli(intake.scheduledAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("hh:mm a", appLocale))}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PsBadge(when (intake.status) { "TAKEN" -> stringResource(R.string.status_taken); "POSTPONED" -> stringResource(R.string.status_postponed); "SKIPPED" -> stringResource(R.string.status_skipped); else -> stringResource(R.string.notification_taken) })
                    intake.actedAt?.let { Text("${stringResource(R.string.history_entry_label)} ${Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM, hh:mm a", appLocale))}", style = MaterialTheme.typography.bodySmall) }
                } }
            }
        }
    }
}
