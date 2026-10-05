package com.pillsense.app.feature.intake.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
)

enum class DoseStatus { PENDING, TAKEN, POSTPONED, SKIPPED }

@Composable
fun TodayScreen(
    userName: String = "Karol",
    onScanClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDoseStatusChange: (doseId: String, status: DoseStatus) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var isDarkTheme by remember { mutableStateOf(false) }

    // Sample data
    val todayProgress = remember { mutableStateOf(Pair(1, 5)) } // (taken, total)
    val weeklyAverage = 76
    val doses = listOf(
        DoseItem("1", "08:00", "Metformina", "850 mg", "Tomar con alimentos", DoseStatus.TAKEN),
        DoseItem("2", "09:30", "Vitamina D3", "1000 UI", "Con el desayuno", DoseStatus.SKIPPED),
        DoseItem("3", "14:00", "Losartán", "50 mg", "A la misma hora cada día", DoseStatus.PENDING),
        DoseItem("4", "20:00", "Metformina", "850 mg", "Tomar con alimentos", DoseStatus.PENDING),
        DoseItem("5", "22:00", "Atorvastatina", "20 mg", "Antes de dormir", DoseStatus.PENDING),
    )

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
                            text = stringResource(R.string.today_date),
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
                            onClick = { isDarkTheme = !isDarkTheme },
                            modifier = Modifier.size(44.dp)
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
                                .size(44.dp)
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
                                    text = stringResource(R.string.today_view_alert),
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
            items(doses) { dose ->
                PsListRow(
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
                            text = dose.dose,
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
                            .clip(PillSenseShape.large),
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
                            .clip(PillSenseShape.large),
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
        PsBottomBar(
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
                    1 -> {}
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
