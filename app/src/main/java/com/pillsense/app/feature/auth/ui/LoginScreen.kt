package com.pillsense.app.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
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
        emailOk && passwordOk && nameOk && !busy
    }

    val primary = MaterialTheme.colorScheme.primary
    val softShadow = Color.Black.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
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

        Spacer(Modifier.height(32.dp))

        // Logo con degradado y sombra de color
        Box(
            modifier = Modifier
                .size(88.dp)
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
                tint = Color.White, modifier = Modifier.size(44.dp))
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

        Spacer(Modifier.height(32.dp))

        PsSegmentedControl(
            items = listOf(
                stringResource(R.string.login_sign_in),
                stringResource(R.string.login_sign_up)
            ),
            selectedIndex = if (mode == LoginMode.LOGIN) 0 else 1,
            onSelectionChange = { mode = if (it == 0) LoginMode.LOGIN else LoginMode.SIGNUP },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Tarjeta blanca con los campos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = softShadow,
                    spotColor = softShadow
                )
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Login mode: Email, Password
            if (mode == LoginMode.LOGIN) {
                PsTextField(
                    value = email,
                    onValueChange = {
                        email = it.take(254)
                        if (emailError != null) emailError = null
                    },
                    label = stringResource(R.string.login_email),
                    placeholder = "user@example.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(emailError!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                PsTextField(
                    value = password,
                    onValueChange = {
                        password = it.take(64)
                        if (passwordError != null) passwordError = null
                    },
                    label = stringResource(R.string.login_password),
                    placeholder = "••••••••",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                painter = painterResource(if (showPassword) R.drawable.ic_lucide_eye_off else R.drawable.ic_lucide_eye),
                                contentDescription = stringResource(if (showPassword) R.string.login_hide else R.string.login_show),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (passwordError != null) {
                    Text(passwordError!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            } else {
                // Signup mode: Name, Email, Password
                PsTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(40)
                        if (nameError != null) nameError = null
                    },
                    label = "Nombre",
                    placeholder = "Tu nombre",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(nameError!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                PsTextField(
                    value = email,
                    onValueChange = {
                        email = it.take(254)
                        if (emailError != null) emailError = null
                    },
                    label = stringResource(R.string.login_email),
                    placeholder = "user@example.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(emailError!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                PsTextField(
                    value = password,
                    onValueChange = {
                        password = it.take(64)
                        if (passwordError != null) passwordError = null
                    },
                    label = stringResource(R.string.login_password),
                    placeholder = "••••••••",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                painter = painterResource(if (showPassword) R.drawable.ic_lucide_eye_off else R.drawable.ic_lucide_eye),
                                contentDescription = stringResource(if (showPassword) R.string.login_hide else R.string.login_show),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (passwordError != null) {
                    Text(passwordError!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        authError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Cuenta local: tus datos permanecen en este teléfono.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
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
private fun LoginCircleButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
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
