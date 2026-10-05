package com.pillsense.app.feature.medication.ui.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.PillSenseSpacing
import com.pillsense.app.core.designsystem.PillSenseRadius
import com.pillsense.app.core.designsystem.PillSenseTheme
import kotlinx.coroutines.delay

data class ScannedMedication(
    val name: String,
    val dose: String,
    val form: String,
    val frequency: String,
    val duration: String,
    val confidence: Int,
)

enum class ScanMode {
    CAJA, RECETA
}

enum class ScanPhase {
    IDLE, DETECTING, IDENTIFYING, INTERPRETING, COMPLETE
}

@Composable
fun ScanScreen(
    onClose: () -> Unit = {},
    onScanComplete: (ScannedMedication) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var scanMode by remember { mutableStateOf(ScanMode.CAJA) }
    var flashOn by remember { mutableStateOf(false) }
    var scanPhase by remember { mutableStateOf(ScanPhase.IDLE) }

    LaunchedEffect(scanPhase) {
        if (scanPhase == ScanPhase.DETECTING) {
            delay(800)
            scanPhase = ScanPhase.IDENTIFYING
            delay(800)
            scanPhase = ScanPhase.INTERPRETING
            delay(800)
            scanPhase = ScanPhase.COMPLETE
            delay(600)
            onScanComplete(
                ScannedMedication(
                    name = "Amoxicilina",
                    dose = "500 mg",
                    form = "Cápsula",
                    frequency = "Cada 8 horas",
                    duration = "7 días",
                    confidence = 96,
                )
            )
        }
    }

    val isAnalyzing = scanPhase != ScanPhase.IDLE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Background image (placeholder - in real app would be camera feed)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .then(if (isAnalyzing) Modifier.scale(1.05f).blur(2.dp) else Modifier)
        )

        // Top controls
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = PillSenseSpacing.spacing_56)
                .padding(horizontal = PillSenseSpacing.spacing_16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_x),
                    contentDescription = stringResource(R.string.scan_close),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mode tabs
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                listOf(ScanMode.CAJA, ScanMode.RECETA).forEach { mode ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { scanMode = mode }
                            .background(
                                if (scanMode == mode) Color.White else Color.Transparent
                            )
                            .padding(horizontal = PillSenseSpacing.spacing_16, vertical = PillSenseSpacing.spacing_12),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name,
                            fontSize = 15.sp,
                            color = if (scanMode == mode) Color.Black else Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Flash button
            IconButton(
                onClick = { flashOn = !flashOn },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (flashOn) Color(0xFFFFD60A) else Color(0x66000000)
                    )
            ) {
                Icon(
                    painter = painterResource(
                        id = if (flashOn) R.drawable.ic_lucide_zap else R.drawable.ic_lucide_zap_off
                    ),
                    contentDescription = stringResource(
                        if (flashOn) R.string.scan_flash_off else R.string.scan_flash_on
                    ),
                    tint = if (flashOn) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Scanner frame
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(3f / 4f)
                .fillMaxWidth(fraction = 0.75f)
                .clip(RoundedCornerShape(PillSenseRadius.threeXL))
                .border(2.dp, Color.Transparent)
        ) {
            // Corner brackets
            repeat(4) { corner ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(3.dp, Color.White)
                        .then(
                            when (corner) {
                                0 -> Modifier.align(Alignment.TopStart)
                                1 -> Modifier.align(Alignment.TopEnd)
                                2 -> Modifier.align(Alignment.BottomStart)
                                else -> Modifier.align(Alignment.BottomEnd)
                            }
                        )
                )
            }

            // Scan line animation
            if (!isAnalyzing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .height(2.dp)
                        .background(Color(0xFF30B0C7))
                )
            }
        }

        // Bottom analysis panel or capture controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = PillSenseSpacing.spacing_48)
                .padding(horizontal = PillSenseSpacing.spacing_20)
        ) {
            AnimatedVisibility(
                visible = isAnalyzing,
                enter = slideInVertically() + fadeIn()
            ) {
                AnalysisPanel(scanPhase = scanPhase)
            }

            AnimatedVisibility(
                visible = !isAnalyzing,
                enter = fadeIn()
            ) {
                CaptureControls(
                    mode = scanMode,
                    onCapture = { scanPhase = ScanPhase.DETECTING }
                )
            }
        }
    }
}

@Composable
private fun AnalysisPanel(scanPhase: ScanPhase) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(PillSenseRadius.twoXL))
            .background(Color(0x8D000000))
            .padding(PillSenseSpacing.spacing_20)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_sparkles),
                    contentDescription = null,
                    tint = Color(0xFF64D2FF),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.scan_analyzing),
                    fontSize = 13.sp,
                    color = Color(0xFF64D2FF),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.sp
                )
            }

            // Steps
            val steps = listOf(
                stringResource(R.string.scan_detecting),
                stringResource(R.string.scan_identifying),
                stringResource(R.string.scan_interpreting),
            )

            steps.forEachIndexed { index, step ->
                ScanStep(
                    label = step,
                    isCompleted = scanPhase.ordinal > index,
                    isActive = scanPhase.ordinal == index
                )
            }
        }
    }
}

@Composable
private fun ScanStep(label: String, isCompleted: Boolean, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PillSenseSpacing.spacing_8),
        horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color(0xFF34C759)
                        isActive -> Color(0xFF64D2FF)
                        else -> Color.White.copy(alpha = 0.15f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Text(
            text = label,
            fontSize = 15.sp,
            color = if (isCompleted || isActive) Color.White else Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CaptureControls(mode: ScanMode, onCapture: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_24)
    ) {
        // Hint text
        Text(
            text = when (mode) {
                ScanMode.CAJA -> stringResource(R.string.scan_hint_box)
                ScanMode.RECETA -> stringResource(R.string.scan_hint_rx)
            },
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        // Camera controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PillSenseSpacing.spacing_16),
            horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery button
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(PillSenseRadius.large))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lucide_images),
                    contentDescription = stringResource(R.string.scan_gallery),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Capture button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(4.dp, Color.White)
                    .clickable { onCapture() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            // Spacer
            Box(modifier = Modifier.size(48.dp))
        }
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun ScanScreenPreview() {
    PillSenseTheme {
        ScanScreen()
    }
}
