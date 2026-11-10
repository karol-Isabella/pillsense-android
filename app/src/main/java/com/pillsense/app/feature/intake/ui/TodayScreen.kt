package com.pillsense.app.feature.intake.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.util.TimeFormatHelper

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
fun getTimeBasedGreeting(userName: String): String {
    return if (userName.isNotBlank()) stringResource(R.string.today_hello, userName)
    else stringResource(R.string.today_hello_no_name)
}

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
    onAddMedicationClick: () -> Unit = onScanClick,
    onInsightsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDoseStatusChange: (doseId: String, status: DoseStatus) -> Unit = { _, _ -> },
    language: String,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = dark
    val todayProgress = remember(doses) { mutableStateOf(doses.count { it.status == DoseStatus.TAKEN } to doses.size) }
    var selectedDose by remember { mutableStateOf<DoseItem?>(null) }
    var doseNotReady by remember { mutableStateOf(false) }
    val markInteraction = remember { MutableInteractionSource() }
    var markPressed by remember { mutableStateOf(false) }
    LaunchedEffect(markInteraction) {
        markInteraction.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> markPressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> markPressed = false
            }
        }
    }
    val markScale by animateFloatAsState(if (markPressed) .96f else 1f, label = "dose_mark_press")
    selectedDose?.let { selected ->
        AlertDialog(onDismissRequest = { selectedDose = null }, title = { Text(selected.medName) },
            text = { Column {
                Text("${selected.dose} · ${selected.time}")
                listOf(DoseStatus.TAKEN to "Tomada", DoseStatus.POSTPONED to "Posponer 15 min", DoseStatus.SKIPPED to "Omitida").forEach { (status, label) ->
                    TextButton(onClick = { onDoseStatusChange(selected.id, status); selectedDose = null }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(label) }
                }
            } }, confirmButton = {}, dismissButton = { TextButton(onClick = { selectedDose = null }) { Text(stringResource(R.string.dialog_cancel)) } })
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            item {
                // Greeting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern(if (language == "en") "EEEE, MMMM d" else "EEEE, d MMMM", java.util.Locale.forLanguageTag(language))).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getTimeBasedGreeting(userName),
                            style = MaterialTheme.typography.displayLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onThemeChange,
                            modifier = Modifier.size(40.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryLight, SecondaryLight)))
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (userName.isNotBlank()) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Canvas(Modifier.size(80.dp)) {
                                        val stroke = 8.dp.toPx()
                                        drawArc(Color(0xFFDFF5E8), -90f, 360f, false,
                                            style = Stroke(stroke, cap = StrokeCap.Round), size = Size(size.width, size.height))
                                        drawArc(Color(0xFF30B98B), -90f, percent * 3.6f, false,
                                            style = Stroke(stroke, cap = StrokeCap.Round), size = Size(size.width, size.height))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$percent%", style = MaterialTheme.typography.titleLarge)
                                        Text("hoy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column {
                                    Text(stringResource(R.string.today_daily_progress), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(stringResource(R.string.today_doses_of, todayProgress.value.first, todayProgress.value.second), style = MaterialTheme.typography.titleLarge)
                                    Spacer(Modifier.height(6.dp))
                                    PsBadge(text = stringResource(R.string.today_offline), backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), textColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                    }
                }
            }

            // Next dose card
            if (nextDose != null) {
                item {
                    Text(stringResource(R.string.today_next_dose), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                    PsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        backgroundColor = MaterialTheme.colorScheme.primary
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = stringResource(R.string.today_at, TimeFormatHelper.format12Hour(nextDose.time, language)).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Text(
                                text = nextDose.medName,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )

                            Text(
                                text = "${nextDose.dose} · ${nextDose.instructions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(Modifier.fillMaxWidth().height(44.dp).scale(markScale).clip(PillSenseShape.large)
                                .background(Color.White).clickable(interactionSource = markInteraction, indication = null) {
                                    if (nextDose.canAct) { doseNotReady = false; onDoseStatusChange(nextDose.id, DoseStatus.TAKEN) }
                                    else doseNotReady = true
                                },
                                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_lucide_check), null, Modifier.size(18.dp), tint = PrimaryLight)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.today_mark_taken), color = PrimaryLight, style = MaterialTheme.typography.titleMedium)
                            }
                            AnimatedVisibility(doseNotReady, enter = fadeIn()) {
                                Text(stringResource(R.string.today_dose_not_ready), Modifier.fillMaxWidth().padding(top = 10.dp), color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
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
                    Text(stringResource(R.string.today_no_active), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.today_no_active_desc))
                    Spacer(Modifier.height(12.dp))
                    PsButton(onScanClick, stringResource(R.string.today_add_medication), Modifier.fillMaxWidth())
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
                            text = "${TimeFormatHelper.format12Hour(dose.time, language)} · ${dose.dose}",
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
                            .clickable(onClick = onAddMedicationClick)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_pill),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.today_add_medication),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    PsCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onInsightsClick)
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
                                painter = painterResource(R.drawable.ic_lucide_scan_line),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    },
                    label = stringResource(R.string.scan_button_label)
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
        TodayScreen(language = "es")
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun TodayScreenDarkPreview() {
    PillSenseTheme(darkTheme = true) {
        TodayScreen(language = "es")
    }
}
