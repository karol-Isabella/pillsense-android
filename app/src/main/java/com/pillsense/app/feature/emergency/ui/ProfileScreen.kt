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
    isSmsNotificationEnabled: Boolean = false, onSmsNotificationChange: (Boolean) -> Unit = {},
    language: String = "es", onLanguageChange: (String) -> Unit = {}) {
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
    
    // Check SEND_SMS permission each time Perfil opens
    LaunchedEffect(Unit) {
        if (smsEnabled && !PermissionManager.isGranted(context, AppPermission.SEND_SMS)) {
            smsEnabled = false
            onSmsNotificationChange(false)
        }
    }
    
    if (showConsentDialog) {
        AlertDialog(onDismissRequest = { showConsentDialog = false }, 
            title = { Text(stringResource(R.string.consent_ai_analysis_title)) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.consent_ai_data_sent), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.consent_ai_data_anon), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.consent_ai_data_adherence), style = MaterialTheme.typography.bodySmall)
                Text("\n${stringResource(R.string.consent_ai_data_not_sent)}", style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.consent_ai_no_med_names), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.consent_ai_no_doses), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.consent_ai_no_contact), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.consent_ai_no_dates), style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { TextButton(onClick = { showConsentDialog = false; onAiConsentChange(true); aiEnabled = true }) { Text(stringResource(R.string.consent_ai_accept)) } },
            dismissButton = { TextButton(onClick = { showConsentDialog = false; aiEnabled = false }) { Text(stringResource(R.string.consent_ai_later)) } })
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
            }) { Text(stringResource(R.string.consent_ai_accept)) } },
            dismissButton = { TextButton(onClick = { showSmsConsentDialog = false; smsEnabled = false }) { Text(stringResource(R.string.consent_ai_later)) } })
    }
    if (showSmsPermissionDenied) {
        AlertDialog(onDismissRequest = { showSmsPermissionDenied = false }, 
            title = { Text(stringResource(R.string.alert_permission_denied)) },
            text = { Text(stringResource(R.string.profile_sms_permission_denied)) },
            confirmButton = { TextButton(onClick = { showSmsPermissionDenied = false }) { Text(stringResource(R.string.alert_understood)) } })
    }
    archive?.let { med -> AlertDialog(onDismissRequest = { archive = null }, title = { Text(stringResource(R.string.alert_finish_treatment)) },
        text = { Text(stringResource(R.string.alert_finish_treatment_desc, med.name)) },
        confirmButton = { TextButton(onClick = { onArchive(med.id); archive = null }) { Text(stringResource(R.string.alert_finish)) } },
        dismissButton = { TextButton(onClick = { archive = null }) { Text(stringResource(R.string.rec_cancel)) } }) }
    removing?.let { contact -> AlertDialog(onDismissRequest = { removing = null }, title = { Text(stringResource(R.string.alert_remove_contact)) },
        text = { Text(stringResource(R.string.alert_remove_contact_desc, contact.name)) },
        confirmButton = { TextButton(onClick = { onRemoveContact(contact.id); removing = null }) { Text(stringResource(R.string.alert_remove)) } },
        dismissButton = { TextButton(onClick = { removing = null }) { Text(stringResource(R.string.rec_cancel)) } }) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.displayMedium) }
        item { PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
            Text(if (!userName.isNullOrBlank()) userName else email.substringBefore('@').replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall)
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp)); PsBadge(stringResource(R.string.profile_account_local))
        } } }
        item { PsSectionHeader(stringResource(R.string.profile_appearance_header))
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(stringResource(R.string.profile_dark_mode), style = MaterialTheme.typography.titleMedium)
                    Switch(checked = dark, onCheckedChange = { onThemeChange() })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(stringResource(R.string.profile_language_label), style = MaterialTheme.typography.titleMedium)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(if (language == "es") stringResource(R.string.profile_language_spanish) else stringResource(R.string.profile_language_english))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.profile_language_spanish)) }, onClick = { onLanguageChange("es"); expanded = false })
                            DropdownMenuItem(text = { Text(stringResource(R.string.profile_language_english)) }, onClick = { onLanguageChange("en"); expanded = false })
                        }
                    }
                }
            } }
        }
        item { PsSectionHeader(stringResource(R.string.profile_my_meds)) }
        if (medications.none { it.active }) item { Text(stringResource(R.string.profile_no_active_treatments), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(medications.filter { it.active }, key = { it.id }) { med ->
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text("${med.name} · ${med.dose}", style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.med_interval, med.intervalHours) + " · ${med.firstTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(med.durationDays?.let { stringResource(R.string.med_duration, it, med.startDate) } ?: stringResource(R.string.med_duration_continuous), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { archive = med }) { Text(stringResource(R.string.alert_finish_treatment), color = MaterialTheme.colorScheme.error) }
            } }
        }
        item { PsSectionHeader(stringResource(R.string.profile_contacts_header))
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_notify_contacts), style = MaterialTheme.typography.titleSmall)
                    }
                    val smsSwitchEnabled = smsEnabled && PermissionManager.isGranted(context, AppPermission.SEND_SMS)
                    Switch(checked = smsSwitchEnabled, onCheckedChange = { checked ->
                        if (checked) {
                            if (PermissionManager.isGranted(context, AppPermission.SEND_SMS)) {
                                showSmsConsentDialog = true
                            } else {
                                PermissionManager.requestPermission(smsPermissionLauncher, AppPermission.SEND_SMS)
                            }
                        } else {
                            onSmsNotificationChange(false)
                            smsEnabled = false
                        }
                    })
                }
                Text(stringResource(R.string.profile_sms_notify_text), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } }
        }
        items(contacts, key = { it.id }) { contact ->
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium)
                Text("${contact.relation} · ${contact.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    TextButton(onClick = { removing = contact }) { Text(stringResource(R.string.profile_remove_contact_text), color = MaterialTheme.colorScheme.error) }
                }
            } }
        }
            item {
                if (adding) PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PsTextField(name, { name = it }, label = stringResource(R.string.profile_name_label), modifier = Modifier.fillMaxWidth())
                    PsTextField(relation, { relation = it }, label = stringResource(R.string.profile_relation_label), modifier = Modifier.fillMaxWidth())
                    PsTextField(phone, { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' } }, label = stringResource(R.string.profile_phone_label), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = consentAgreed, onCheckedChange = { consentAgreed = it }, modifier = Modifier.size(24.dp))
                        Text(stringResource(R.string.profile_contact_consent), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    }
                    val phoneDigitCount = phone.count { it.isDigit() }
                    PsButton({ onSaveContact(name, relation, phone) { adding = false; name = ""; relation = ""; phone = ""; consentAgreed = false } }, stringResource(R.string.profile_add_contact), Modifier.fillMaxWidth(), enabled = name.isNotBlank() && phoneDigitCount in 7..15 && consentAgreed)
                    TextButton(onClick = { adding = false; name = ""; relation = ""; phone = ""; consentAgreed = false }) { Text(stringResource(R.string.rec_cancel)) }
                } } else {
                    if (contacts.size >= 2) {
                        Text(stringResource(R.string.profile_contacts_max), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
                    } else {
                        PsButton({ adding = true }, stringResource(R.string.profile_add_contact), Modifier.fillMaxWidth(), style = PsButtonStyle.Secondary)
                    }
                }
            }
        item { PsSectionHeader(stringResource(R.string.profile_reminders_header))
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (remindersReady) stringResource(R.string.profile_reminders_enabled) else stringResource(R.string.profile_reminders_disabled))
                Text(stringResource(R.string.profile_reminders_note), style = MaterialTheme.typography.bodySmall)
                PsButton(onPermissions, stringResource(R.string.profile_check_permissions), Modifier.fillMaxWidth(), style = PsButtonStyle.Secondary)
            } }
        }
        item { PsSectionHeader(stringResource(R.string.profile_privacy_header))
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.profile_privacy_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.profile_privacy_desc), style = MaterialTheme.typography.bodySmall)
            } }
        }
        item { PsSectionHeader(stringResource(R.string.profile_ai_header))
            PsCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.profile_ai_recommendations), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.profile_ai_only_anon), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        item { PsButton(onLogout, stringResource(R.string.profile_logout), Modifier.fillMaxWidth(), style = PsButtonStyle.Destructive)
            Text(stringResource(R.string.profile_logout_note), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall) }
    }
}
