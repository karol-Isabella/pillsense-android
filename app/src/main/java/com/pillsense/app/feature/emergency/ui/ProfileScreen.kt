package com.pillsense.app.feature.emergency.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import com.pillsense.app.R
import com.pillsense.app.core.database.*
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.util.permissions.AppPermission
import com.pillsense.app.core.util.permissions.PermissionManager

@Composable
fun ProfileScreen(email: String, medications: List<Medication>, contacts: List<EmergencyContact>, dark: Boolean,
    onThemeChange: () -> Unit, onLogout: () -> Unit, onArchive: (String) -> Unit,
    onSaveContact: (String, String, String, () -> Unit) -> Unit, onRemoveContact: (String) -> Unit,
    onPermissions: () -> Unit, remindersReady: Boolean, userName: String? = null,
    isAiEnabled: Boolean = false, onAiConsentChange: (Boolean) -> Unit = {},
    isSmsNotificationEnabled: Boolean = false, onSmsNotificationChange: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    var adding by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var relation by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var consentAgreed by rememberSaveable { mutableStateOf(false) }
    var archive by remember { mutableStateOf<Medication?>(null) }
    var removing by remember { mutableStateOf<EmergencyContact?>(null) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var aiEnabled by remember { mutableStateOf(isAiEnabled) }
    var smsEnabled by remember { mutableStateOf(isSmsNotificationEnabled) }
    var showSmsConsentDialog by remember { mutableStateOf(false) }
    var showSmsPermissionDenied by remember { mutableStateOf(false) }
    
    val smsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onSmsNotificationChange(true)
            smsEnabled = true
        } else {
            showSmsPermissionDenied = true
            smsEnabled = false
        }
    }
    
    LaunchedEffect(isAiEnabled) { aiEnabled = isAiEnabled }
    
    if (showConsentDialog) {
        AlertDialog(onDismissRequest = { showConsentDialog = false }, 
            title = { Text("Análisis con IA") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Se enviarán al análisis de IA:", style = MaterialTheme.typography.titleSmall)
                Text("• Conteos anónimos de tomas (tomadas, pospuestas, omitidas)", style = MaterialTheme.typography.bodySmall)
                Text("• Porcentaje de adherencia por período", style = MaterialTheme.typography.bodySmall)
                Text("\nNO se enviarán:", style = MaterialTheme.typography.titleSmall)
                Text("• Nombres de medicamentos reales", style = MaterialTheme.typography.bodySmall)
                Text("• Dosis, instrucciones o frecuencias", style = MaterialTheme.typography.bodySmall)
                Text("• Tu correo electrónico o contactos", style = MaterialTheme.typography.bodySmall)
                Text("• Fechas exactas", style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { TextButton(onClick = { showConsentDialog = false; onAiConsentChange(true); aiEnabled = true }) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = { showConsentDialog = false; aiEnabled = false }) { Text("Ahora no") } })
    }
    if (showSmsConsentDialog) {
        AlertDialog(onDismissRequest = { showSmsConsentDialog = false }, 
            title = { Text(stringResource(R.string.profile_sms_dialog_title)) },
            text = { Text(stringResource(R.string.profile_sms_dialog_message)) },
            confirmButton = { TextButton(onClick = { 
                showSmsConsentDialog = false
                // Request SEND_SMS permission
                if (PermissionManager.isGranted(context, AppPermission.SEND_SMS)) {
                    onSmsNotificationChange(true)
                    smsEnabled = true
                } else {
                    PermissionManager.requestPermission(smsPermissionLauncher, AppPermission.SEND_SMS)
                }
            }) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = { showSmsConsentDialog = false; smsEnabled = false }) { Text("Ahora no") } })
    }
    if (showSmsPermissionDenied) {
        AlertDialog(onDismissRequest = { showSmsPermissionDenied = false }, 
            title = { Text("Permiso rechazado") },
            text = { Text(stringResource(R.string.profile_sms_permission_denied)) },
            confirmButton = { TextButton(onClick = { showSmsPermissionDenied = false }) { Text("Entendido") } })
    }
    archive?.let { med -> AlertDialog(onDismissRequest = { archive = null }, title = { Text("Finalizar tratamiento") },
        text = { Text("Se detendrán los recordatorios de ${med.name}. El historial se conservará.") },
        confirmButton = { TextButton(onClick = { onArchive(med.id); archive = null }) { Text("Finalizar") } },
        dismissButton = { TextButton(onClick = { archive = null }) { Text("Cancelar") } }) }
    removing?.let { contact -> AlertDialog(onDismissRequest = { removing = null }, title = { Text("Eliminar contacto") },
        text = { Text("¿Eliminar a ${contact.name} de tus contactos de emergencia?") },
        confirmButton = { TextButton(onClick = { onRemoveContact(contact.id); removing = null }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { removing = null }) { Text("Cancelar") } }) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Perfil", style = MaterialTheme.typography.displayMedium) }
        item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
            Text(if (!userName.isNullOrBlank()) userName else email.substringBefore('@').replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall)
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp)); PsBadge("Cuenta local · datos cifrados")
        } } }
        item { PsSectionHeader("Apariencia")
            PsCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Modo oscuro", style = MaterialTheme.typography.titleMedium)
                Switch(checked = dark, onCheckedChange = { onThemeChange() })
            } }
        }
        item { PsSectionHeader("Mis medicamentos") }
        if (medications.none { it.active }) item { Text("Aún no tienes tratamientos activos.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(medications.filter { it.active }, key = { it.id }) { med ->
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text("${med.name} · ${med.dose}", style = MaterialTheme.typography.titleMedium)
                Text("Cada ${med.intervalHours} horas · ${med.firstTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(med.durationDays?.let { "$it días desde ${med.startDate}" } ?: "Tratamiento continuo", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { archive = med }) { Text("Finalizar tratamiento", color = MaterialTheme.colorScheme.error) }
            } }
        }
        item { PsSectionHeader("Contactos de emergencia") }
        items(contacts, key = { it.id }) { contact ->
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium)
                Text("${contact.relation} · ${contact.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    TextButton(onClick = { removing = contact }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }
            } }
        }
        item {
            if (adding) PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PsTextField(name, { name = it }, label = "Nombre", modifier = Modifier.fillMaxWidth())
                PsTextField(relation, { relation = it }, label = "Relación (opcional)", modifier = Modifier.fillMaxWidth())
                PsTextField(phone, { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' } }, label = "Teléfono", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = consentAgreed, onCheckedChange = { consentAgreed = it }, modifier = Modifier.size(24.dp))
                    Text("Esta persona está de acuerdo en recibir estos avisos", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
                val phoneDigitCount = phone.count { it.isDigit() }
                PsButton({ onSaveContact(name, relation, phone) { adding = false; name = ""; relation = ""; phone = ""; consentAgreed = false } }, "Guardar contacto", Modifier.fillMaxWidth(), enabled = name.isNotBlank() && phoneDigitCount in 7..15 && consentAgreed)
                TextButton(onClick = { adding = false; name = ""; relation = ""; phone = ""; consentAgreed = false }) { Text("Cancelar") }
            } } else if (smsEnabled) PsButton({ adding = true }, "Añadir contacto", Modifier.fillMaxWidth(), style = PsButtonStyle.Secondary, enabled = contacts.size < 2)
        }
        item { PsSectionHeader("Recordatorios")
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (remindersReady) "Notificaciones y alarmas exactas habilitadas" else "Revisa los permisos para recibir recordatorios puntuales.")
                Text("Los avisos son locales y funcionan sin internet. Android puede retrasarlos si faltan permisos; forzar el cierre de la app los interrumpe hasta volver a abrirla.", style = MaterialTheme.typography.bodySmall)
                PsButton(onPermissions, "Revisar permisos", Modifier.fillMaxWidth(), style = PsButtonStyle.Secondary)
            } }
        }
        item { PsSectionHeader("Privacidad")
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Todo empieza en tu teléfono", style = MaterialTheme.typography.titleMedium)
                Text("Tus datos se guardan cifrados en tu teléfono. El análisis con IA es opcional y solo envía estadísticas anónimas.", style = MaterialTheme.typography.bodySmall)
            } }
        }
        item { PsSectionHeader("Análisis con IA")
            PsCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Recomendaciones con IA", style = MaterialTheme.typography.titleSmall)
                    Text("Solo estadísticas anónimas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = aiEnabled, onCheckedChange = { 
                    if (it && !isAiEnabled) {
                        showConsentDialog = true
                    } else {
                        onAiConsentChange(it)
                        aiEnabled = it
                    }
                })
            } }
        }
        item { PsSectionHeader("Notificaciones a contactos")
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Avisar a mis contactos", style = MaterialTheme.typography.titleSmall)
                    }
                    Switch(checked = smsEnabled, onCheckedChange = { checked ->
                        if (checked && !isSmsNotificationEnabled) {
                            // First activation: show consent dialog
                            showSmsConsentDialog = true
                        } else if (!checked) {
                            // Disabling
                            onSmsNotificationChange(false)
                            smsEnabled = false
                        }
                    })
                }
                Text(stringResource(R.string.profile_sms_notify_text), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } }
        }
        item { PsButton(onLogout, "Cerrar sesión", Modifier.fillMaxWidth(), style = PsButtonStyle.Destructive)
            Text("Al cerrar sesión se pausan tus avisos. Se reactivan cuando vuelves a entrar.", Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall) }
    }
}
