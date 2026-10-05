package com.pillsense.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.PillSenseSpacing
import com.pillsense.app.core.designsystem.PillSenseRadius
import com.pillsense.app.core.designsystem.PillSenseTheme

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf<LoginMode>(LoginMode.LOGIN) }
    var email by remember { mutableStateOf("sofia@pillsense.app") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<LoginError?>(null) }
    var isDarkTheme by remember { mutableStateOf(false) }
    var currentLang by remember { mutableStateOf("es") }

    val onSubmit: () -> Unit = {
        val emailRegex = """^\S+@\S+\.\S+$""".toRegex()
        when {
            !emailRegex.matches(email) -> error = LoginError.INVALID_EMAIL
            password.length < 8 -> error = LoginError.SHORT_PASSWORD
            else -> {
                error = null
                onLoginSuccess(email.trim().lowercase())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PillSenseSpacing.spacing_24)
            .padding(top = PillSenseSpacing.spacing_16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top buttons: Language & Theme
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = PillSenseSpacing.spacing_8),
            horizontalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_8)
        ) {
            LangButton(
                currentLang = currentLang,
                onClick = { currentLang = if (currentLang == "es") "en" else "es" }
            )
            ThemeButton(
                isDark = isDarkTheme,
                onClick = { isDarkTheme = !isDarkTheme }
            )
        }

        Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_32))

        // Logo & Title
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(PillSenseRadius.threeXL))
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF007AFF),
                            Color(0xFF1E90FF),
                            Color(0xFF30B0C7)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lucide_pill),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_24))

        Text(
            text = "PillSense",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 34.sp
        )

        Spacer(modifier = Modifier.height(PillSenseSpacing.spacing_8))

        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 288.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PillSenseSpacing.spacing_40),
            verticalArrangement = Arrangement.spacedBy(PillSenseSpacing.spacing_12)
        ) {
            // Email & Password combined field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(PillSenseRadius.xLarge))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text(stringResource(R.string.login_email)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text(stringResource(R.string.login_password)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showPassword = !showPassword },
                                    modifier = Modifier.size(PillSenseSpacing.spacing_48)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (showPassword) R.drawable.ic_lucide_eye_off
                                            else R.drawable.ic_lucide_eye
                                        ),
                                        contentDescription = stringResource(
                                            if (showPassword) R.string.login_hide else R.string.login_show
                                        ),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Error message
            Text(
                text = when (error) {
                    LoginError.INVALID_EMAIL -> stringResource(R.string.login_invalid_email)
                    LoginError.SHORT_PASSWORD -> stringResource(R.string.login_short_password)
                    null -> stringResource(R.string.login_no_errors)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (error != null) MaterialTheme.colorScheme.error else Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 20.dp)
                    .padding(horizontal = PillSenseSpacing.spacing_8)
            )

            // Sign in / Sign up button
            androidx.compose.material3.Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(PillSenseRadius.xLarge),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (mode == LoginMode.LOGIN) stringResource(R.string.login_sign_in)
                    else stringResource(R.string.login_sign_up),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Toggle mode button
            androidx.compose.material3.TextButton(
                onClick = {
                    mode = if (mode == LoginMode.LOGIN) LoginMode.SIGNUP else LoginMode.LOGIN
                    error = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (mode == LoginMode.LOGIN) stringResource(R.string.login_to_sign_up)
                    else stringResource(R.string.login_to_sign_in),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LangButton(
    currentLang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .wrapContentWidth(),
        shape = RoundedCornerShape(PillSenseRadius.large),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(
            text = if (currentLang == "es") "EN" else "ES",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
private fun ThemeButton(
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(PillSenseRadius.large))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Icon(
            painter = painterResource(
                id = if (isDark) R.drawable.ic_lucide_sun else R.drawable.ic_lucide_moon
            ),
            contentDescription = stringResource(
                if (isDark) R.string.theme_to_light else R.string.theme_to_dark
            ),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}

enum class LoginMode {
    LOGIN, SIGNUP
}

enum class LoginError {
    INVALID_EMAIL, SHORT_PASSWORD
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    PillSenseTheme {
        LoginScreen()
    }
}
