package com.pillsense.app.feature.medication.ui.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import kotlinx.coroutines.delay

data class ScannedMedication(
    val name: String,
    val dose: String,
    val form: String,
    val frequency: String,
    val duration: String,
    val confidence: Int,
)

enum class ScanMode { CAJA, RECETA }
enum class ScanPhase { IDLE, DETECTING, IDENTIFYING, INTERPRETING, COMPLETE }

@Composable
fun ScanScreen(
    onScanComplete: (ScannedMedication) -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(ScanMode.CAJA) }
    var phase by remember { mutableStateOf(ScanPhase.IDLE) }
    var flashOn by remember { mutableStateOf(false) }
    var scannedMed by remember { mutableStateOf<ScannedMedication?>(null) }

    // Simulate scanning phases
    LaunchedEffect(phase) {
        when (phase) {
            ScanPhase.IDLE -> {}
            ScanPhase.DETECTING -> {
                delay(800)
                phase = ScanPhase.IDENTIFYING
            }
            ScanPhase.IDENTIFYING -> {
                delay(1200)
                phase = ScanPhase.INTERPRETING
            }
            ScanPhase.INTERPRETING -> {
                delay(800)
                scannedMed = ScannedMedication(
                    name = "Amoxicilina",
                    dose = "500 mg",
                    form = "Cápsula",
                    frequency = "Cada 8 horas",
                    duration = "7 días",
                    confidence = 96
                )
                phase = ScanPhase.COMPLETE
            }
            ScanPhase.COMPLETE -> {}
        }
    }

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
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_x),
                    contentDescription = stringResource(R.string.scan_close),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = stringResource(R.string.scan_doc_type),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(44.dp))
        }

        // Mode selector
        PsSegmentedControl(
            items = listOf(
                stringResource(R.string.scan_mode_caja),
                stringResource(R.string.scan_mode_receta)
            ),
            selectedIndex = if (mode == ScanMode.CAJA) 0 else 1,
            onSelectionChange = { mode = if (it == 0) ScanMode.CAJA else ScanMode.RECETA },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        )

        // Camera frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 20.dp)
                .clip(PillSenseShape.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = PillSenseShape.extraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            // Scanning animation
            if (phase != ScanPhase.IDLE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(if (phase == ScanPhase.COMPLETE) 0.dp else 2.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_images),
                    contentDescription = stringResource(R.string.scan_camera_alt),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (phase) {
                        ScanPhase.IDLE -> stringResource(
                            if (mode == ScanMode.CAJA)
                                R.string.scan_hint_box
                            else
                                R.string.scan_hint_rx
                        )
                        ScanPhase.DETECTING -> stringResource(R.string.scan_detecting)
                        ScanPhase.IDENTIFYING -> stringResource(R.string.scan_identifying)
                        ScanPhase.INTERPRETING -> stringResource(R.string.scan_interpreting)
                        ScanPhase.COMPLETE -> stringResource(R.string.scan_analyzing)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Flash toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            PsButton(
                onClick = { flashOn = !flashOn },
                text = if (flashOn)
                    stringResource(R.string.scan_flash_off)
                else
                    stringResource(R.string.scan_flash_on),
                style = PsButtonStyle.Secondary,
                modifier = Modifier.width(160.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Capture button / Result
        AnimatedVisibility(
            visible = phase == ScanPhase.COMPLETE && scannedMed != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PsButton(
                    onClick = { scannedMed?.let { onScanComplete(it) } },
                    text = stringResource(R.string.scan_capture),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        AnimatedVisibility(
            visible = phase == ScanPhase.IDLE,
            enter = fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PsButton(
                    onClick = { phase = ScanPhase.DETECTING },
                    text = stringResource(R.string.scan_capture),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.scan_gallery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun ScanScreenPreview() {
    PillSenseTheme {
        ScanScreen()
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun ScanScreenDarkPreview() {
    PillSenseTheme(darkTheme = true) {
        ScanScreen()
    }
}
