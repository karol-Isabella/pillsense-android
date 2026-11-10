package com.pillsense.app.feature.auth.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.PsButton
import com.pillsense.app.core.designsystem.SecondaryLight

@Composable
fun WelcomeScreen(
    dark: Boolean,
    language: String,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit,
    onStart: () -> Unit,
    onLogin: () -> Unit,
) {
    val motion = rememberInfiniteTransition(label = "welcome_blobs")
    val driftOne by motion.animateFloat(
        initialValue = -.08f,
        targetValue = .1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "blob_one"
    )
    val driftTwo by motion.animateFloat(
        initialValue = .1f,
        targetValue = -.09f,
        animationSpec = infiniteRepeatable(tween(11000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "blob_two"
    )
    val driftThree by motion.animateFloat(
        initialValue = -.06f,
        targetValue = .08f,
        animationSpec = infiniteRepeatable(tween(13000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "blob_three"
    )
    val background = MaterialTheme.colorScheme.background
    val blueBlob = if (dark) Color(0xFF0756A8) else Color(0xFF9BC8FF)
    val aquaBlob = if (dark) Color(0xFF07545D) else Color(0xFFB6E4E6)
    val lilacBlob = if (dark) Color(0xFF302B74) else Color(0xFFC9CFFF)

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(background)
            val radius = size.minDimension * .72f
            listOf(
                Triple(Offset(size.width * (.22f + driftOne), size.height * .28f), blueBlob, .56f),
                Triple(Offset(size.width * (.82f + driftTwo), size.height * .38f), aquaBlob, .48f),
                Triple(Offset(size.width * (.58f + driftThree), size.height * .76f), lilacBlob, .4f),
            ).forEach { (center, color, strength) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = strength), color.copy(alpha = strength * .38f), color.copy(alpha = 0f)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
        }

        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WelcomeCircleAction(language.uppercase(), onLanguageChange)
                WelcomeCircleAction(if (dark) "☀" else "☾", onThemeChange)
            }

            Spacer(Modifier.height(30.dp))

            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    Modifier.size(112.dp)
                        .shadow(26.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = .38f))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF168AFF), SecondaryLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_lucide_pill), null, tint = Color.White, modifier = Modifier.size(52.dp))
                }
                Box(
                    Modifier.offset(x = 5.dp, y = (-5).dp)
                        .size(36.dp).shadow(8.dp, CircleShape).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_lucide_check), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.welcome_brand),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(28.dp))
            WelcomeFeatureCard(R.drawable.ic_lucide_scan_line, stringResource(R.string.welcome_scan), Color(0xFFE6F1FF), Color(0xFF007AFF))
            Spacer(Modifier.height(10.dp))
            WelcomeFeatureCard(R.drawable.ic_lucide_bell_ring, stringResource(R.string.welcome_reminders), Color(0xFFE6F7EC), Color(0xFF248A3D))
            Spacer(Modifier.height(10.dp))
            WelcomeFeatureCard(R.drawable.ic_lucide_circle_check, stringResource(R.string.welcome_progress), Color(0xFFEDEBFF), Color(0xFF6558E8))

            Spacer(Modifier.height(22.dp))
            PsButton(onClick = onStart, text = stringResource(R.string.welcome_start), modifier = Modifier.fillMaxWidth(), height = 56)
            Text(
                stringResource(R.string.welcome_login),
                Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onLogin).padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun WelcomeFeatureCard(icon: Int, text: String, iconBackground: Color, iconTint: Color) {
    Surface(
        Modifier.fillMaxWidth().height(66.dp),
        shape = RoundedCornerShape(22.dp),
        // Keep these cards opaque so the animated background never shows through
        // the text area as a distracting highlight.
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(iconBackground), contentAlignment = Alignment.Center) {
                Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(21.dp))
            }
            Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun WelcomeCircleAction(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).shadow(6.dp, CircleShape).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = .9f))
            .border(.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = .12f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
    }
}
