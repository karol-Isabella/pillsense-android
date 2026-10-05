@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pillsense.app.feature.medication.ui.confirm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.PillSenseSpacing
import com.pillsense.app.core.designsystem.PillSenseRadius
import com.pillsense.app.core.designsystem.PillSenseTheme
import com.pillsense.app.feature.medication.ui.scan.ScannedMedication

data class ConfirmMedicationData(
    val name: String,
    val dose: String,
    val form: String,
    val frequency: String,
    val times: List<String>,
    val instructions: String,
)

@Composable
fun ConfirmScreen(
    scannedMed: ScannedMedication? = null,
    onCancel: () -> Unit = {},
    onSave: (ConfirmMedicationData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Evalúa todos los stringResource al inicio (contexto Composable)
    val defaultInstructions = stringResource(R.string.confirm_default_instructions)
    val cancelText = stringResource(R.string.confirm_cancel)
    val titleText = stringResource(R.string.confirm_title)
    val saveText = stringResource(R.string.confirm_save)
    val medSectionText = stringResource(R.string.confirm_med_section)
    val nameText = stringResource(R.string.confirm_name)
    val doseText = stringResource(R.string.confirm_dose)
    val formText = stringResource(R.string.confirm_form)
    val scheduleSectionText = stringResource(R.string.confirm_schedule_section)
    val frequencyText = stringResource(R.string.confirm_frequency)
    val firstDoseText = stringResource(R.string.confirm_first_dose)
    val durationText = stringResource(R.string.confirm_duration)
    val instructionsText = stringResource(R.string.confirm_instructions)
    val offlineNoteText = stringResource(R.string.confirm_offline_note)
    val saveScheduleText = stringResource(R.string.confirm_save_schedule)
    val fallbackText = stringResource(R.string.confirm_fallback)
    val verifyText = stringResource(R.string.confirm_verify)
    
    var name by remember { mutableStateOf(scannedMed?.name ?: "") }
    var dose by remember { mutableStateOf(scannedMed?.dose ?: "") }
    var form by remember { mutableStateOf(scannedMed?.form ?: "Cápsula") }
    var frequency by remember { mutableStateOf(scannedMed?.frequency ?: "Una vez al día") }
    var duration by remember { mutableStateOf(scannedMed?.duration ?: "") }
    var startTime by remember { mutableStateOf("08:00") }
    var instructions by remember { mutableStateOf(defaultInstructions) }

    val frequencies = listOf(
        stringResource(R.string.freq_once_daily),
        stringResource(R.string.freq_every_12h),
        stringResource(R.string.freq_every_8h),
        stringResource(R.string.freq_every_6h),
    )

    val forms = listOf(
        stringResource(R.string.med_form_capsule),
        stringResource(R.string.med_form_tablet),
        stringResource(R.string.med_form_syrup),
        stringResource(R.string.med_form_drops),
        stringResource(R.string.med_form_injection),
    )

    // Generate times based on frequency
    val times = generateTimes(frequency, startTime)
    val isValid = name.isNotBlank() && dose.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = PillSenseSpacing.spacing_8)
                .padding(top = PillSenseSpacing.spacing_12, bottom = PillSenseSpacing.spacing_12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = cancelText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(
                onClick = {
                    if (isValid) {
                        onSave(
                            ConfirmMedicationData(
                                name = name.trim(),
                                dose = dose.trim(),
                                form = form,
                                frequency = frequency,
                                times = times,
                                instructions = instructions.trim()
                            )
                        )
                    }
                },
                enabled = isValid
            ) {
                Text(
                    text = saveText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline)

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PillSenseSpacing.spacing_24)
        ) {
            // Header with medication display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PillSenseSpacing.spacing_24),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pill icon (simplified)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color(0xFFF5A0F5).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(PillSenseRadius.large)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lucide_pill),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_12))

                Text(
                    text = name.ifBlank { fallbackText },
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_8))

                // AI confidence badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(PillSenseRadius.large))
                        .background(Color(0xFF5856D6).copy(alpha = 0.1f)),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = PillSenseSpacing.spacing_12,
                            vertical = PillSenseSpacing.spacing_8
                        ),
                        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_sparkles),
                            contentDescription = null,
                            tint = Color(0xFF5856D6),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.confirm_extracted, scannedMed?.confidence ?: 96),
                            fontSize = 13.sp,
                            color = Color(0xFF5856D6),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_12))

                Text(
                    text = verifyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Medication Section
            SectionTitle(text = medSectionText)

            ConfirmField(
                label = nameText,
                value = name,
                onValueChange = { name = it }
            )

            ConfirmField(
                label = doseText,
                value = dose,
                onValueChange = { dose = it }
            )

            ConfirmDropdown(
                label = formText,
                value = form,
                options = forms,
                onValueChange = { form = it }
            )

            // Schedule Section
            Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_24))

            SectionTitle(text = scheduleSectionText)

            ConfirmDropdown(
                label = frequencyText,
                value = frequency,
                options = frequencies,
                onValueChange = { frequency = it }
            )

            ConfirmField(
                label = firstDoseText,
                value = startTime,
                onValueChange = { startTime = it },
                keyboardType = KeyboardType.Number
            )

            ConfirmField(
                label = durationText,
                value = duration,
                onValueChange = { duration = it }
            )

            ConfirmField(
                label = instructionsText,
                value = instructions,
                onValueChange = { instructions = it }
            )

            // Schedule summary
            Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_24))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(PillSenseRadius.large))
                    .background(MaterialTheme.colorScheme.secondary),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PillSenseSpacing.spacing_16),
                    verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lucide_bell_ring),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.confirm_will_schedule, times.size),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Time pills
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8),
                        verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8)
                    ) {
                        times.forEach { time ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(PillSenseRadius.large))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = time,
                                    modifier = Modifier.padding(
                                        horizontal = PillSenseSpacing.spacing_12,
                                        vertical = PillSenseSpacing.spacing_8
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_8))

                    Text(
                        text = offlineNoteText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_24))
        }

        // Save button
        Button(
            onClick = {
                if (isValid) {
                    onSave(
                        ConfirmMedicationData(
                            name = name.trim(),
                            dose = dose.trim(),
                            form = form,
                            frequency = frequency,
                            times = times,
                            instructions = instructions.trim()
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(PillSenseSpacing.spacing_16)
                .height(56.dp),
            enabled = isValid,
            shape = RoundedCornerShape(PillSenseRadius.xLarge)
        ) {
            Text(
                text = saveScheduleText,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PillSenseSpacing.spacing_16),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.sp
    )
}

@Composable
private fun ConfirmField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = PillSenseSpacing.spacing_8),
        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.widthIn(min = 112.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 17.sp
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ConfirmDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { expanded = !expanded }
            .padding(vertical = PillSenseSpacing.spacing_8),
        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.widthIn(min = 112.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 17.sp
        )

        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                modifier = Modifier.align(Alignment.CenterEnd),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_chevron_down),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterEnd),
                tint = Color(0xFFC7C7CC)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun generateTimes(frequency: String, startTime: String): List<String> {
    val hour = startTime.substringBefore(":").toIntOrNull() ?: 8
    return when (frequency) {
        "Una vez al día" -> listOf(String.format("%02d:00", hour))
        "Cada 12 horas" -> listOf(String.format("%02d:00", hour), String.format("%02d:00", (hour + 12) % 24))
        "Cada 8 horas" -> listOf(
            String.format("%02d:00", hour),
            String.format("%02d:00", (hour + 8) % 24),
            String.format("%02d:00", (hour + 16) % 24)
        )
        "Cada 6 horas" -> listOf(
            String.format("%02d:00", hour),
            String.format("%02d:00", (hour + 6) % 24),
            String.format("%02d:00", (hour + 12) % 24),
            String.format("%02d:00", (hour + 18) % 24)
        )
        else -> listOf(String.format("%02d:00", hour))
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun ConfirmScreenPreview() {
    PillSenseTheme {
        ConfirmScreen(
            scannedMed = ScannedMedication(
                name = "Amoxicilina",
                dose = "500 mg",
                form = "Cápsula",
                frequency = "Cada 8 horas",
                duration = "7 días",
                confidence = 96
            )
        )
    }
}
