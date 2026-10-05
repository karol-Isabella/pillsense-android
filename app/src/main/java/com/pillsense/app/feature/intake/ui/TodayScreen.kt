package com.pillsense.app.feature.intake.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.pillsense.app.core.designsystem.PillSenseSpacing
import com.pillsense.app.core.designsystem.PillSenseRadius
import com.pillsense.app.core.designsystem.PillSenseTheme

data class DoseItem(
    val id: String,
    val time: String,
    val medName: String,
    val dose: String,
    val instructions: String,
    val status: DoseStatus,
    val medTone: String = "blue",
)

enum class DoseStatus {
    PENDING, TAKEN, POSTPONED, SKIPPED
}

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
    val initials = userName.take(1).uppercase()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            // Header with date and user
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PillSenseSpacing.spacing_24, vertical = PillSenseSpacing.spacing_16)
            ) {
                Text(
                    text = stringResource(R.string.today_date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PillSenseSpacing.spacing_8),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_hello, userName),
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 28.sp
                    )

                    // Theme + Profile buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8)
                    ) {
                        IconButton(
                            onClick = { isDarkTheme = !isDarkTheme },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isDarkTheme) R.drawable.ic_lucide_sun else R.drawable.ic_lucide_moon
                                ),
                                contentDescription = stringResource(
                                    if (isDarkTheme) R.string.theme_to_light else R.string.theme_to_dark
                                ),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        Button(
                            onClick = onProfileClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            shape = CircleShape
                        ) {
                            Text(
                                text = initials,
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }

        // Adherence card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PillSenseSpacing.spacing_16, vertical = PillSenseSpacing.spacing_12)
                    .clip(RoundedCornerShape(PillSenseRadius.xLarge)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(PillSenseRadius.xLarge)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PillSenseSpacing.spacing_20),
                    horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_20),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress circle
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9F3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$percent%",
                                fontSize = 26.sp,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = stringResource(R.string.today_word),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8)
                    ) {
                        Text(
                            text = stringResource(R.string.today_daily_progress),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(
                            text = stringResource(
                                R.string.today_doses_of,
                                todayProgress.value.first,
                                todayProgress.value.second
                            ),
                            fontSize = 22.sp,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Surface(
                            modifier = Modifier
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_wifi_off),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                                Text(
                                    text = stringResource(R.string.today_offline),
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Next dose section
        if (nextDose != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PillSenseSpacing.spacing_24, vertical = PillSenseSpacing.spacing_12),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_next_dose),
                        style = MaterialTheme.typography.labelLarge
                    )

                    Button(onClick = {}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_bell_ring),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(PillSenseSpacing.spacing_8))
                        Text(text = stringResource(R.string.today_view_alert))
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PillSenseSpacing.spacing_16, vertical = PillSenseSpacing.spacing_8)
                        .clip(RoundedCornerShape(PillSenseRadius.twoXL)),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(PillSenseRadius.twoXL)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PillSenseSpacing.spacing_20),
                        verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_20)
                    ) {
                        // Dose info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.today_at, nextDose.time),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 0.5.sp
                                )

                                Text(
                                    text = nextDose.medName,
                                    fontSize = 28.sp,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                                Text(
                                    text = "${nextDose.dose} · ${nextDose.instructions}",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.clip(RoundedCornerShape(PillSenseRadius.large))
                            ) {
                                Text(
                                    text = nextDose.time,
                                    modifier = Modifier.padding(
                                        horizontal = PillSenseSpacing.spacing_12,
                                        vertical = 6.dp
                                    ),
                                    fontSize = 15.sp,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Action buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8)
                        ) {
                            Button(
                                onClick = {
                                    onDoseStatusChange(nextDose.id, DoseStatus.TAKEN)
                                    todayProgress.value = Pair(todayProgress.value.first + 1, todayProgress.value.second)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(PillSenseRadius.xLarge)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lucide_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(PillSenseSpacing.spacing_8))
                                Text(text = stringResource(R.string.today_mark_taken))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(PillSenseRadius.xLarge))
                                        .clickable { onDoseStatusChange(nextDose.id, DoseStatus.POSTPONED) },
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(PillSenseSpacing.spacing_12),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_lucide_clock),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(PillSenseSpacing.spacing_8))
                                        Text(
                                            text = "10 min",
                                            fontSize = 15.sp,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(PillSenseRadius.xLarge))
                                        .clickable { onDoseStatusChange(nextDose.id, DoseStatus.SKIPPED) },
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(PillSenseSpacing.spacing_12),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_lucide_x),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(PillSenseSpacing.spacing_8))
                                        Text(
                                            text = stringResource(R.string.today_skip_dose),
                                            fontSize = 15.sp,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // All done message
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PillSenseSpacing.spacing_16, vertical = PillSenseSpacing.spacing_12)
                        .clip(RoundedCornerShape(PillSenseRadius.xLarge)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PillSenseSpacing.spacing_20),
                        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_16)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            color = Color(0xFF34C759).copy(alpha = 0.1f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lucide_check),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(PillSenseSpacing.spacing_12),
                                tint = Color(0xFF34C759)
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.today_all_done),
                                fontSize = 17.sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = stringResource(R.string.today_all_done_body),
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Doses list
        item {
            Text(
                text = stringResource(R.string.today_schedule),
                modifier = Modifier.padding(
                    horizontal = PillSenseSpacing.spacing_24,
                    vertical = PillSenseSpacing.spacing_16
                ),
                style = MaterialTheme.typography.labelLarge
            )
        }

        items(doses) { dose ->
            DoseListItem(
                dose = dose,
                onStatusChange = { onDoseStatusChange(dose.id, it) }
            )
        }

        // Quick action cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PillSenseSpacing.spacing_16)
                    .padding(top = PillSenseSpacing.spacing_8),
                horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12)
            ) {
                QuickActionCard(
                    icon = R.drawable.ic_lucide_scan_line,
                    title = stringResource(R.string.today_scan),
                    subtitle = stringResource(R.string.today_scan_sub),
                    onClick = onScanClick,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    icon = R.drawable.ic_lucide_sparkles,
                    title = stringResource(R.string.today_week, weeklyAverage),
                    subtitle = stringResource(R.string.today_suggestions),
                    onClick = onInsightsClick,
                    modifier = Modifier.weight(1f),
                    showChevron = true
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_24))
        }
    }
}

@Composable
private fun DoseListItem(dose: DoseItem, onStatusChange: (DoseStatus) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PillSenseSpacing.spacing_16, vertical = 2.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PillSenseSpacing.spacing_16, vertical = PillSenseSpacing.spacing_12),
            horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dose.time,
                modifier = Modifier.widthIn(min = 44.dp),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_pill),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dose.medName,
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (dose.status == DoseStatus.SKIPPED) TextDecoration.LineThrough else TextDecoration.None
                )

                Text(
                    text = "${dose.dose} · ${dose.instructions}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            when (dose.status) {
                DoseStatus.PENDING -> {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onStatusChange(DoseStatus.TAKEN) },
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.dp, Color(0xFFC7C7CC), CircleShape)
                        )
                    }
                }
                DoseStatus.TAKEN -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_check),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF34C759)
                    )
                }
                DoseStatus.SKIPPED -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_x),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                DoseStatus.POSTPONED -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_clock),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFF9500)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showChevron: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(PillSenseRadius.large))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(PillSenseRadius.large)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PillSenseSpacing.spacing_16),
            verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(PillSenseRadius.large))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (showChevron) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFC7C7CC)
                    )
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun TodayScreenPreview() {
    PillSenseTheme {
        TodayScreen()
    }
}
