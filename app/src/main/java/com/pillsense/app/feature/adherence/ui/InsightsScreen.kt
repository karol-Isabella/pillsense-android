package com.pillsense.app.feature.adherence.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillsense.app.BuildConfig
import com.pillsense.app.R
import com.pillsense.app.core.database.*
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.model.Schedule
import com.pillsense.app.core.model.AdherenceAdvice
import com.pillsense.app.feature.adherence.domain.calculateWeekCounts
import com.pillsense.app.core.util.DateLocaleHelper
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun InsightsScreen(
    intakes: List<Intake>,
    medications: List<Medication>,
    email: String = "",
    language: String,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasInternet by remember { mutableStateOf(context.hasValidatedInternet()) }
    DisposableEffect(context) {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            override fun onLost(network: Network) { hasInternet = context.hasValidatedInternet() }
        }
        connectivity.registerDefaultNetworkCallback(callback)
        onDispose { runCatching { connectivity.unregisterNetworkCallback(callback) } }
    }
    LaunchedEffect(email) {
        viewModel.setUserEmail(email)
    }
    val now = System.currentTimeMillis()
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val recent = intakes.filter { it.scheduledAt in today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()..now }
    val average = Schedule.adherence(recent, now)
    val weekCounts = calculateWeekCounts(recent, today.minusDays(6), now, zone)
    val uiState = viewModel.uiState.collectAsState().value
    
    LaunchedEffect(email, recent.size, language) {
        viewModel.loadGeminiAdvice(recent, medications, language)
    }
    
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Text(stringResource(R.string.insights_last_7_days), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.insights_my_progress), style = MaterialTheme.typography.displayLarge) }
        item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { (average ?: 0) / 100f }, modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.tertiary, trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = .12f), strokeWidth = 10.dp)
                    Text(average?.let { "$it%" } ?: "—", style = MaterialTheme.typography.headlineMedium)
                }
                Column { Text(stringResource(R.string.insights_weekly_adherence), style = MaterialTheme.typography.titleMedium)
                    Text(if (recent.isEmpty()) stringResource(R.string.insights_no_doses) else stringResource(R.string.insights_weekly_doses, recent.count { it.status == "TAKEN" }, recent.size), style = MaterialTheme.typography.bodyMedium) }
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
                        Text(DateLocaleHelper.getShortDayName(language, day.dayOfWeek), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } } }
        if (uiState.isLoading) {
            item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.insights_loading_ai), style = MaterialTheme.typography.bodySmall)
            } } }
        } else if (uiState.geminiResponse != null) {
            // Seguro: no mostrar JSON crudo
            if (uiState.geminiResponse.summary.trim().startsWith("{") || uiState.geminiResponse.summary.contains("\"resumen\"")) {
                item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.insights_ai_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(if (language == "es") "Consulta tu historial de medicamentos y mantén la consistencia." else "Check your medication history and stay consistent.", style = MaterialTheme.typography.bodyMedium)
                } } }
            } else {
                item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.insights_ai_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    if (uiState.source == "IA" && hasInternet) PsBadge("IA")
                }
                    Text(uiState.geminiResponse.summary, style = MaterialTheme.typography.bodyMedium)
                if (uiState.geminiResponse.recommendations.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.insights_recommendations_label), style = MaterialTheme.typography.labelMedium)
                    uiState.geminiResponse.recommendations.forEach { rec ->
                        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", style = MaterialTheme.typography.bodySmall)
                            Column {
                            Text(rec.reason, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } } }
            }
        }
        item { PsSectionHeader(stringResource(R.string.insights_recommendations_label))
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.insights_local_habits), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                val adviceResult = AdherenceAdvice.generate(recent, now)
                val adviceText = when (adviceResult) {
                    is com.pillsense.app.core.model.AdviceResult.NoHistory -> stringResource(R.string.advice_no_history)
                    is com.pillsense.app.core.model.AdviceResult.MissedIntakes -> {
                        if (adviceResult.mostCommonHour != null) {
                            stringResource(R.string.advice_missed_with_hour, adviceResult.count, adviceResult.mostCommonHour)
                        } else {
                            stringResource(R.string.advice_missed_base, adviceResult.count)
                        }
                    }
                    is com.pillsense.app.core.model.AdviceResult.PostponedIntakes -> stringResource(R.string.advice_postponed, adviceResult.count)
                    is com.pillsense.app.core.model.AdviceResult.SomeIncomplete -> {
                        if (adviceResult.count == 1) {
                            stringResource(R.string.advice_some_incomplete, adviceResult.count)
                        } else {
                            stringResource(R.string.advice_some_incomplete_plural, adviceResult.count)
                        }
                    }
                    is com.pillsense.app.core.model.AdviceResult.AllCompleted -> stringResource(R.string.advice_all_completed)
                }
                Text(adviceText, style = MaterialTheme.typography.bodyMedium)
                PsBadge(stringResource(R.string.advice_badge))
            } }
        }
        item { PsSectionHeader(stringResource(R.string.insights_per_medication)) }
        item {
            val medsWithData = medications.filter { med -> recent.any { it.medicationId == med.id } }
            if (medsWithData.isNotEmpty()) PsCard(Modifier.fillMaxWidth()) {
                Column {
                    medsWithData.forEachIndexed { index, med ->
                        val value = Schedule.adherence(recent.filter { it.medicationId == med.id }, now) ?: 0
                        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(med.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Text("$value%", style = MaterialTheme.typography.labelLarge, color = if (value >= 80) SuccessStrongLight else WarningStrongLight)
                            }
                            LinearProgressIndicator(progress = { value / 100f }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)), color = if (value >= 80) MaterialTheme.colorScheme.tertiary else WarningLight, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        }
                        if (index < medsWithData.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 14.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                    }
                }
            }
        }
    }
}

private fun Context.hasValidatedInternet(): Boolean {
    val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivity.activeNetwork ?: return false
    val capabilities = connectivity.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
