package com.pillsense.app.feature.intake.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pillsense.app.core.database.*
import androidx.compose.ui.res.stringResource
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.util.DateLocaleHelper
import com.pillsense.app.core.util.TimeFormatHelper
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(intakes: List<Intake>, medications: List<Medication>, language: String) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val now = System.currentTimeMillis()
    val history = intakes.filter { it.scheduledAt <= now || it.actedAt != null }
    val states = listOf(null, "TAKEN", "POSTPONED", "SKIPPED")
    val visible = history.filter { states[filter] == null || it.status == states[filter] }
    val names = medications.associateBy { it.id }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.history_heading), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.displayLarge) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("TAKEN" to stringResource(R.string.status_taken), "POSTPONED" to stringResource(R.string.status_postponed), "SKIPPED" to stringResource(R.string.status_skipped)).forEach { (state, label) ->
                val metricColor = when (state) { "TAKEN" -> Color(0xFF27A65B); "POSTPONED" -> Color(0xFFD97706); else -> Color(0xFFFF3B30) }
                PsCard(Modifier.weight(1f)) { Column(Modifier.padding(12.dp)) {
                    Text(history.count { it.status == state }.toString(), style = MaterialTheme.typography.headlineMedium,
                        color = metricColor)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                } }
            }
        } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(stringResource(R.string.history_all) to 0, stringResource(R.string.history_taken) to 1, stringResource(R.string.history_postponed) to 2, stringResource(R.string.history_skipped) to 3).forEach { (label, idx) ->
                Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(50))
                    .background(if (filter == idx) Color(0xFF1C1C1E) else MaterialTheme.colorScheme.surface)
                    .clickable { filter = idx }, contentAlignment = Alignment.Center) {
                    Text(label, maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = if (filter == idx) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        } }
        if (visible.isEmpty()) item { PsCard(Modifier.fillMaxWidth()) { Text(stringResource(R.string.history_empty), Modifier.padding(24.dp)) } }
        val grouped = visible.groupBy { Instant.ofEpochMilli(it.scheduledAt).atZone(ZoneId.systemDefault()).toLocalDate() }
        grouped.forEach { (day, entries) ->
            item(key = day.toString()) { Text(day.format(DateTimeFormatter.ofPattern(if (language == "en") "EEEE, MMMM d" else "EEEE, d MMMM", java.util.Locale.forLanguageTag(language))).uppercase(), Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item(key = "entries-${day}") {
                PsCard(Modifier.fillMaxWidth()) {
                    Column {
                        entries.forEachIndexed { index, intake ->
                            val med = names[intake.medicationId]
                            val (statusLabel, statusColor, statusBg) = when (intake.status) {
                                "TAKEN" -> Triple(stringResource(R.string.status_taken), Color(0xFF248A3D), Color(0x1F34C759))
                                "SKIPPED" -> Triple(stringResource(R.string.status_skipped), MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = .1f))
                                "POSTPONED" -> Triple(stringResource(R.string.status_postponed), Color(0xFFC76F00), Color(0x1FFF9500))
                                else -> Triple(stringResource(R.string.notification_taken), MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                            }
                            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(8.dp).background(statusColor, CircleShape))
                                Column(Modifier.weight(1f)) {
                                    Text(med?.name ?: stringResource(R.string.confirm_fallback), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    val scheduled = Instant.ofEpochMilli(intake.scheduledAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
                                    Text("${med?.dose.orEmpty()} · ${TimeFormatHelper.format12Hour(scheduled, language)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                PsBadge(statusLabel, backgroundColor = statusBg, textColor = statusColor)
                            }
                            if (index < entries.lastIndex) HorizontalDivider(Modifier.padding(start = 32.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                        }
                    }
                }
            }
        }
    }
}
