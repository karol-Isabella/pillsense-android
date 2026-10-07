@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pillsense.app.feature.medication.ui.confirm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.feature.medication.ui.scan.ScannedMedication
import com.pillsense.app.core.model.Schedule

data class ConfirmMedicationData(
    val name: String,
    val dose: String,
    val form: String,
    val frequency: String,
    val times: List<String>,
    val instructions: String,
    val intervalHours: Int,
    val firstTime: String,
    val durationDays: Int?,
)

@Composable
fun ConfirmScreen(
    scannedMed: ScannedMedication? = null,
    onCancel: () -> Unit = {},
    onSave: (ConfirmMedicationData) -> Unit = {},
    modifier: Modifier = Modifier,
    saving: Boolean = false,
) {
    // Pre-evaluate string resources at Composable scope
    val cancelText = stringResource(R.string.confirm_cancel)
    val titleText = stringResource(R.string.confirm_title)
    val saveText = stringResource(R.string.confirm_save)
    val medSectionText = stringResource(R.string.confirm_med_section)
    val nameLabel = stringResource(R.string.confirm_name)
    val doseLabel = stringResource(R.string.confirm_dose)
    val formLabel = stringResource(R.string.confirm_form)
    val scheduleSectionText = stringResource(R.string.confirm_schedule_section)
    val frequencyLabel = stringResource(R.string.confirm_frequency)
    val firstDoseLabel = stringResource(R.string.confirm_first_dose)
    val instructionsLabel = stringResource(R.string.confirm_instructions)
    val offlineNoteText = stringResource(R.string.confirm_offline_note)
    val saveScheduleText = stringResource(R.string.confirm_save_schedule)
    val defaultFormText = stringResource(R.string.med_form_capsule)
    val defaultFrequencyText = stringResource(R.string.freq_once_daily)

    var name by rememberSaveable { mutableStateOf(scannedMed?.name ?: "") }
    var dose by rememberSaveable { mutableStateOf(scannedMed?.dose ?: "") }
    var form by rememberSaveable { mutableStateOf(scannedMed?.form ?: defaultFormText) }
    var frequency by rememberSaveable { mutableStateOf(scannedMed?.frequency ?: defaultFrequencyText) }
    var duration by rememberSaveable { mutableStateOf(scannedMed?.duration ?: "") }
    var startTime by rememberSaveable { mutableStateOf("08:00") }
    var instructions by rememberSaveable { mutableStateOf(scannedMed?.instructions.orEmpty()) }

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

    val interval = listOf(24, 12, 8, 6).getOrElse(frequencies.indexOf(frequency)) { 24 }
    val times = runCatching { Schedule.times(startTime, interval) }.getOrDefault(emptyList())
    val durationDays = duration.toIntOrNull()
    var reviewed by rememberSaveable { mutableStateOf(false) }
    val isValid = name.isNotBlank() && dose.isNotBlank() && form.isNotBlank() && frequency in frequencies && times.isNotEmpty() &&
        (duration.isBlank() || (durationDays != null && durationDays in 1..3650)) && !saving && (scannedMed == null || reviewed)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                                instructions = instructions.trim(),
                                intervalHours = interval, firstTime = startTime, durationDays = durationDays
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

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )

        // Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Medication Display
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = PillSenseShape.large
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = name.ifBlank { stringResource(R.string.confirm_fallback) },
                        style = MaterialTheme.typography.displayMedium
                    )

                    if (scannedMed != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PsBadge(
                            text = "Texto reconocido · confirma cada dato",
                            backgroundColor = Color(0xFF5856D6).copy(alpha = 0.1f),
                            textColor = Color(0xFF5856D6)
                        )
                    }
                }
            }

            if (scannedMed != null) item {
                PsCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Texto de la foto", style = MaterialTheme.typography.titleMedium)
                        Text(scannedMed.originalText.take(4000), style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = reviewed, onCheckedChange = { reviewed = it })
                            Text("He comparado nombre, dosis y frecuencia con mi receta.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            // Medication Section
            item {
                PsSectionHeader(title = medSectionText)
            }

            item {
                PsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PsTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = nameLabel,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PsTextField(
                            value = dose,
                            onValueChange = { dose = it },
                            label = doseLabel,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PsDropdownField(
                            label = formLabel,
                            value = form,
                            options = forms,
                            onValueChange = { form = it }
                        )
                    }
                }
            }

            // Schedule Section
            item {
                PsSectionHeader(title = scheduleSectionText)
            }

            item {
                PsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PsDropdownField(
                            label = frequencyLabel,
                            value = frequency,
                            options = frequencies,
                            onValueChange = { frequency = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PsTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = "$firstDoseLabel (HH:mm)",
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PsTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duración en días (vacío = continuo)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PsTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            label = instructionsLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Schedule Summary
            item {
                PsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        if (times.isEmpty()) Text("Introduce una hora válida en formato HH:mm (por ejemplo, 08:30).", color = MaterialTheme.colorScheme.error)
                        if (duration.isNotBlank() && (durationDays == null || durationDays !in 1..3650)) Text("La duración debe ser un número entre 1 y 3650 días.", color = MaterialTheme.colorScheme.error)
                        if (frequency !in frequencies) Text("Selecciona y confirma la frecuencia indicada en tu receta.", color = MaterialTheme.colorScheme.error)
                        // Time pills
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            times.forEach { time ->
                                PsBadge(
                                    text = time,
                                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    textColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = offlineNoteText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Save button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            PsButton(
                onClick = {
                    if (isValid) {
                        onSave(
                            ConfirmMedicationData(
                                name = name.trim(),
                                dose = dose.trim(),
                                form = form,
                                frequency = frequency,
                                times = times,
                                instructions = instructions.trim(),
                                intervalHours = interval, firstTime = startTime, durationDays = durationDays
                            )
                        )
                    }
                },
                text = saveScheduleText,
                enabled = isValid,
                isLoading = saving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PsDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = PillSenseShape.medium
                )
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = PillSenseShape.medium
                )
                .clickable { expanded = !expanded }
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                painter = painterResource(R.drawable.ic_lucide_chevron_down),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
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
                duration = "7",
            )
        )
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun ConfirmScreenDarkPreview() {
    PillSenseTheme(darkTheme = true) {
        ConfirmScreen(
            scannedMed = ScannedMedication(
                name = "Amoxicilina",
                dose = "500 mg",
                form = "Cápsula",
                frequency = "Cada 8 horas",
                duration = "7",
            )
        )
    }
}
