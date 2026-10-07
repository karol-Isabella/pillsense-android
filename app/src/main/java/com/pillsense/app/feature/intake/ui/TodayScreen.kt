package com.pillsense.app.feature.intake.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*

data class DoseItem(
    val id: String,
    val time: String,
    val medName: String,
    val dose: String,
    val instructions: String,
    val status: DoseStatus,
    val canAct: Boolean = true,
)

enum class DoseStatus { PENDING, TAKEN, POSTPONED, SKIPPED }

@Composable
fun TodayScreen(
    userName: String = "",
    doses: List<DoseItem> = emptyList(),
    weeklyAverage: Int = 0,
    dark: Boolean = false,
    onThemeChange: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    showBottomBar: Boolean = true,
    onScanClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDoseStatusChange: (doseId: String, status: DoseStatus) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = dark
    val todayProgress = remember(doses) { mutableStateOf(doses.count { it.status == DoseStatus.TAKEN } to doses.size) }
    var selectedDose by remember { mutableStateOf<DoseItem?>(null) }
    selectedDose?.let { selected ->
        AlertDialog(onDismissRequest = { selectedDose = null }, title = { Text(selected.medName) },
            text = { Column {
                Text("${selected.dose} · ${selected.time}")
                listOf(DoseStatus.TAKEN to "Tomada", DoseStatus.POSTPONED to "Posponer 15 min", DoseStatus.SKIPPED to "Omitida").forEach { (status, label) ->
                    TextButton(onClick = { onDoseStatusChange(selected.id, status); selectedDose = null }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(label) }
                }
            } }, confirmButton = {}, dismissButton = { TextButton(onClick = { selectedDose = null }) { Text("Cancelar") } })
    }
    val nextDose = doses.firstOrNull { it.status == DoseStatus.PENDING || it.status == DoseStatus.POSTPONED }
    val percent = if (todayProgress.value.second > 0) {
        (todayProgress.value.first * 100) / todayProgress.value.second
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                // Greeting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale.getDefault())),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.today_hello, userName),
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onThemeChange,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isDarkTheme) R.drawable.ic_lucide_sun else R.drawable.ic_lucide_moon
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Adherence card
            item {
                PsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.today_daily_progress),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.today_doses_of, todayProgress.value.first, todayProgress.value.second),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        PsBadge(
                            text = stringResource(R.string.today_offline),
                            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Next dose card
            if (nextDose != null) {
                item {
                    PsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        backgroundColor = MaterialTheme.colorScheme.primary
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.today_next_dose),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = if (nextDose.canAct) "Gestionar toma" else "Programada",
                                    modifier = Modifier.clickable(enabled = nextDose.canAct) { selectedDose = nextDose },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.today_at, nextDose.time),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Text(
                                text = nextDose.medName,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )

                            Text(
                                text = "${nextDose.dose} · ${nextDose.instructions}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PsButton(
                                onClick = { onDoseStatusChange(nextDose.id, DoseStatus.TAKEN) },
                                text = stringResource(R.string.today_mark_taken),
                                enabled = nextDose.canAct,
                                style = PsButtonStyle.Primary,
                                modifier = Modifier.fillMaxWidth(),
                                height = 48
                            )
                        }
                    }
                }
            }

            // Schedule title
            item {
                Text(
                    text = stringResource(R.string.today_schedule),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Dose list
            if (doses.isEmpty()) item {
                PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
                    Text("Tu tratamiento empieza aquí", style = MaterialTheme.typography.titleLarge)
                    Text("Escanea una receta o añade tu primer medicamento.")
                    Spacer(Modifier.height(12.dp))
                    PsButton(onScanClick, "Añadir medicamento", Modifier.fillMaxWidth())
                } }
            }
            items(doses, key = { it.id }) { dose ->
                PsListRow(
                    onClick = if (dose.canAct && dose.status in listOf(DoseStatus.PENDING, DoseStatus.POSTPONED)) ({ selectedDose = dose }) else null,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when (dose.status) {
                                        DoseStatus.TAKEN -> MaterialTheme.colorScheme.tertiary
                                        DoseStatus.SKIPPED -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dose.time.substringBefore(":"),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (dose.status) {
                                    DoseStatus.TAKEN -> MaterialTheme.colorScheme.tertiary
                                    DoseStatus.SKIPPED -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    },
                    trailingContent = {
                        PsBadge(
                            text = when (dose.status) {
                                DoseStatus.TAKEN -> stringResource(R.string.status_taken)
                                DoseStatus.SKIPPED -> stringResource(R.string.status_skipped)
                                DoseStatus.POSTPONED -> stringResource(R.string.status_postponed)
                                else -> ""
                            },
                            backgroundColor = when (dose.status) {
                                DoseStatus.TAKEN -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                DoseStatus.SKIPPED -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else -> Color.Transparent
                            },
                            textColor = when (dose.status) {
                                DoseStatus.TAKEN -> MaterialTheme.colorScheme.tertiary
                                DoseStatus.SKIPPED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                ) {
                    Column {
                        Text(
                            text = dose.medName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${dose.time} · ${dose.dose}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick action cards
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PsCard(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillSenseShape.large).clickable(onClick = onScanClick),
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_scan_line),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.today_scan),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    PsCard(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillSenseShape.large).clickable(onClick = onInsightsClick),
                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_sparkles),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.today_week, weeklyAverage),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Bottom bar
        if (showBottomBar) PsBottomBar(
            items = listOf(
                PsBottomBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_clock),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = stringResource(R.string.tab_today)
                ),
                PsBottomBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_history),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = stringResource(R.string.tab_history)
                ),
                PsBottomBarItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_zap),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    },
                    label = ""
                ),
                PsBottomBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_chart_bars),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = stringResource(R.string.tab_insights)
                ),
                PsBottomBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_user_round),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = stringResource(R.string.tab_profile)
                ),
            ),
            selectedIndex = 0,
            onItemSelected = { index ->
                when (index) {
                    1 -> onHistoryClick()
                    2 -> onScanClick()
                    3 -> onInsightsClick()
                    4 -> onProfileClick()
                }
            }
        )
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun TodayScreenPreview() {
    PillSenseTheme {
        TodayScreen()
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun TodayScreenDarkPreview() {
    PillSenseTheme(darkTheme = true) {
        TodayScreen()
    }
}
