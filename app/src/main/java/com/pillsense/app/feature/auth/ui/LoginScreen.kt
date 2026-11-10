package com.pillsense.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.security.AuthenticationLogic

enum class LoginMode { LOGIN, SIGNUP }
enum class LoginError { INVALID_EMAIL, SHORT_PASSWORD }

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, password: String, register: Boolean, name: String) -> Unit = { _, _, _, _ -> },
    busy: Boolean = false,
    authError: String? = null,
    dark: Boolean = false,
    onThemeChange: () -> Unit = {},
    language: String = "es",
    onLanguageChange: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(LoginMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableStateOf(0) }
    var lockoutUntil by remember { mutableStateOf(0L) }
    var secondsRemaining by remember { mutableStateOf(0) }
    
    LaunchedEffect(authError) {
        if (authError != null && mode == LoginMode.LOGIN && !busy) {
            failedAttempts += 1
            if (failedAttempts >= 5) {
                lockoutUntil = System.currentTimeMillis() + 30_000
            }
        }
    }
    
    LaunchedEffect(lockoutUntil) {
        if (lockoutUntil > System.currentTimeMillis()) {
            while (System.currentTimeMillis() < lockoutUntil) {
                secondsRemaining = ((lockoutUntil - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                delay(500)
            }
            failedAttempts = 0
            lockoutUntil = 0
            secondsRemaining = 0
        }
    }
    val isDarkTheme = dark
    val currentLang = language

    val onSubmit = {
        emailError = null
        passwordError = null
        nameError = null
        
        val emailValidation = AuthenticationLogic.validateEmail(email.trim(), mode == LoginMode.SIGNUP)
        if (!emailValidation.isValid) { 
            emailError = emailValidation.errorReason
        } else {
            val passwordValidation = AuthenticationLogic.validatePassword(password, mode == LoginMode.SIGNUP)
            if (!passwordValidation.isValid) { 
                passwordError = passwordValidation.errorReason
            } else {
                if (mode == LoginMode.SIGNUP) {
                    val nameValidation = AuthenticationLogic.validateName(name)
                    if (!nameValidation.isValid) { 
                        nameError = nameValidation.errorReason
                    } else {
                        onLoginSuccess(email.trim(), password, mode == LoginMode.SIGNUP, name.trim())
                    }
                } else {
                    onLoginSuccess(email.trim(), password, false, "")
                }
            }
        }
    }

    val isFormValid = run {
        val emailOk = AuthenticationLogic.validateEmail(email.trim(), mode == LoginMode.SIGNUP).isValid
        val passwordOk = AuthenticationLogic.validatePassword(password, mode == LoginMode.SIGNUP).isValid
        val nameOk = if (mode == LoginMode.SIGNUP) AuthenticationLogic.validateName(name).isValid else true
        emailOk && passwordOk && nameOk && !busy && lockoutUntil == 0L
    }
    
    val isLocked = lockoutUntil > 0L && System.currentTimeMillis() < lockoutUntil

    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior: idioma y tema como botones circulares
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoginCircleButton(
                label = currentLang.uppercase(),
                onClick = onLanguageChange
            )
            LoginCircleButton(
                label = if (isDarkTheme) "☀" else "☾",
                onClick = onThemeChange
            )
        }

        Spacer(Modifier.height(44.dp))

        // Logo con degradado y sombra de color
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = primary.copy(alpha = 0.25f),
                    spotColor = primary.copy(alpha = 0.40f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(primary, Color(0xFF1E90FF), SecondaryLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_lucide_pill), contentDescription = null,
                tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Spacer(Modifier.height(24.dp))

        // Título grande y subtítulo centrados
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(48.dp))

        if (mode == LoginMode.LOGIN) {
            LoginLabeledField(stringResource(R.string.login_email_label), email, { email = it.take(254); emailError = null }, "user@example.com", KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(14.dp))
            LoginLabeledField(stringResource(R.string.login_password), password, { password = it.take(64); passwordError = null }, "••••••••", KeyboardOptions(keyboardType = KeyboardType.Password), if (showPassword) VisualTransformation.None else PasswordVisualTransformation()) {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(40.dp)) {
                        Icon(painterResource(if (showPassword) R.drawable.ic_lucide_eye_off else R.drawable.ic_lucide_eye), stringResource(if (showPassword) R.string.login_hide else R.string.login_show), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
            }
            emailError?.let { Text(it, Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
            passwordError?.let { Text(it, Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
        } else {
            LoginLabeledField(stringResource(R.string.signup_name_label), name, { name = it.take(40); nameError = null }, stringResource(R.string.signup_name_placeholder), KeyboardOptions(keyboardType = KeyboardType.Text))
            Spacer(Modifier.height(14.dp))
            LoginLabeledField(stringResource(R.string.login_email_label), email, { email = it.take(254); emailError = null }, "user@example.com", KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(14.dp))
            LoginLabeledField(stringResource(R.string.login_password), password, { password = it.take(32); passwordError = null }, "••••••••", KeyboardOptions(keyboardType = KeyboardType.Password), if (showPassword) VisualTransformation.None else PasswordVisualTransformation()) {
                    IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(40.dp)) {
                        Icon(painterResource(if (showPassword) R.drawable.ic_lucide_eye_off else R.drawable.ic_lucide_eye), stringResource(if (showPassword) R.string.login_hide else R.string.login_show), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
            }
            nameError?.let { Text(it, Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
            emailError?.let { Text(it, Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
            if (passwordError == null) Text(stringResource(R.string.login_password_hint), Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else Text(passwordError!!, Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        authError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (isLocked) {
            Text(stringResource(R.string.login_too_many_attempts, secondsRemaining), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))
        PsButton(
            onClick = onSubmit,
            enabled = isFormValid,
            isLoading = busy,
            text = if (mode == LoginMode.LOGIN) stringResource(R.string.login_sign_in) else stringResource(R.string.login_sign_up),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (mode == LoginMode.LOGIN) stringResource(R.string.login_to_sign_up) else stringResource(R.string.login_to_sign_in),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { mode = if (mode == LoginMode.LOGIN) LoginMode.SIGNUP else LoginMode.LOGIN }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LoginLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, Modifier.padding(start = 2.dp, bottom = 7.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .1f), RoundedCornerShape(16.dp)).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f))
                BasicTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = keyboardOptions, visualTransformation = visualTransformation, textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface))
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun LoginCircleButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    PillSenseTheme { LoginScreen() }
}

@Preview(device = Devices.PIXEL_7, showSystemUi = true)
@Composable
private fun LoginScreenDarkPreview() {
    PillSenseTheme(darkTheme = true) { LoginScreen() }
}
