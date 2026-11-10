package com.pillsense.app.feature.emergency.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
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
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.displayLarge) }
        item { PsCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(listOf(PrimaryLight, SecondaryLight))), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text((userName?.takeIf { it.isNotBlank() } ?: email.substringBefore('@')).take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.headlineSmall)
            }
            Column(Modifier.weight(1f)) {
                Text(if (!userName.isNullOrBlank()) userName else email.substringBefore('@').replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge)
                Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        } } }
        item { PsSectionHeader(stringResource(R.string.profile_appearance_header))
            PsCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(32.dp).clip(PillSenseShape.small).background(Color(0xFF5856D6)), contentAlignment = androidx.compose.ui.Alignment.Center) { Icon(painterResource(R.drawable.ic_lucide_sun), null, tint = Color.White, modifier = Modifier.size(17.dp)) }
                    Text(stringResource(R.string.profile_mode), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    ProfileChoiceControl(listOf(stringResource(R.string.profile_light), stringResource(R.string.profile_dark)), if (dark) 1 else 0, { index -> if ((index == 1) != dark) onThemeChange() })
                }
                HorizontalDivider(Modifier.padding(start = 56.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(32.dp).clip(PillSenseShape.small).background(Color(0xFF30B0C7)), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("文", color = Color.White, style = MaterialTheme.typography.labelMedium) }
                    Text(stringResource(R.string.profile_language_label), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    ProfileChoiceControl(listOf(stringResource(R.string.profile_language_spanish), stringResource(R.string.profile_language_english)), if (language == "en") 1 else 0, { index -> onLanguageChange(if (index == 0) "es" else "en") })
                }
            } }
        }
        item { PsSectionHeader(stringResource(R.string.profile_my_meds)) }
        if (medications.none { it.active }) item { Text(stringResource(R.string.profile_no_active_treatments), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else item {
            PsCard(Modifier.fillMaxWidth()) { Column {
                medications.filter { it.active }.forEachIndexed { index, med ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(listOf(Color(0xFFE7F2FF), Color(0xFFFFF1DD), Color(0xFFE4F7F6), Color(0xFFEFECFF))[index % 4]), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_lucide_pill), null, tint = listOf(PrimaryLight, Color(0xFFF08A00), Color(0xFF179DAD), Color(0xFF6558E8))[index % 4], modifier = Modifier.size(18.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text("${med.name} ${med.dose}", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            Text("${stringResource(R.string.med_interval, med.intervalHours)} · ${med.firstTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        IconButton(onClick = { archive = med }) { Icon(painterResource(R.drawable.ic_lucide_trash2), contentDescription = stringResource(R.string.alert_finish_treatment), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    }
                    if (index < medications.count { it.active } - 1) HorizontalDivider(Modifier.padding(start = 58.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                }
            } }
        }
        item {
            PsSectionHeader(stringResource(R.string.profile_contacts_header))
            PsCard(Modifier.fillMaxWidth()) { Column {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFE9E7)), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_lucide_heart_pulse), null, tint = Color(0xFFFF3B30), modifier = Modifier.size(19.dp))
                    }
                    Text(stringResource(R.string.profile_notify_contacts), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    val smsSwitchEnabled = smsEnabled && PermissionManager.isGranted(context, AppPermission.SEND_SMS)
                    Switch(checked = smsSwitchEnabled, onCheckedChange = { checked ->
                        if (checked) {
                            if (PermissionManager.isGranted(context, AppPermission.SEND_SMS)) showSmsConsentDialog = true
                            else PermissionManager.requestPermission(smsPermissionLauncher, AppPermission.SEND_SMS)
                        } else { onSmsNotificationChange(false); smsEnabled = false }
                    })
                }
                if (smsEnabled) {
                    HorizontalDivider(Modifier.padding(horizontal = 14.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                    contacts.forEachIndexed { index, contact ->
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFE9E7)), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_lucide_heart_pulse), null, tint = Color(0xFFFF3B30), modifier = Modifier.size(19.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(contact.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            Text("${contact.relation} · ${contact.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))) }, modifier = Modifier.size(40.dp)) {
                            Icon(painterResource(R.drawable.ic_lucide_phone), stringResource(R.string.profile_call_contact, contact.name), tint = Color(0xFF30B967), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { removing = contact }, modifier = Modifier.size(36.dp)) {
                            Icon(painterResource(R.drawable.ic_lucide_trash2), stringResource(R.string.profile_remove_contact_text), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                        }
                    }
                    if (index < contacts.lastIndex) HorizontalDivider(Modifier.padding(start = 56.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                }
                    if (adding) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
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
                    }
                    } else if (contacts.size < 2) {
                    TextButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().height(48.dp), contentPadding = PaddingValues(horizontal = 14.dp)) {
                        Text("+  ${stringResource(R.string.profile_add_contact)}", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                }
            } }
        }
        item {
            PsSectionHeader(stringResource(R.string.profile_reminders_header))
            PsCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(AccentLight), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_lucide_bell_ring), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Text(stringResource(R.string.profile_reminders_label), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                PsButton(onPermissions, stringResource(R.string.profile_check_permissions), Modifier.wrapContentWidth(), style = PsButtonStyle.Secondary, height = 40)
            } }
        }
        item {
            PsSectionHeader(stringResource(R.string.profile_ai_header))
            PsCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(AccentLight), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_lucide_sparkles), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.profile_ai_recommendations), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.profile_ai_only_anon), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = aiEnabled, onCheckedChange = {
                    if (it && !isAiEnabled) showConsentDialog = true else { onAiConsentChange(it); aiEnabled = it }
                })
            } }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PsCard(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("↪", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            Text(stringResource(R.string.profile_logout), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Text(stringResource(R.string.profile_logout_note), Modifier.fillMaxWidth().padding(horizontal = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileChoiceControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.width(154.dp).height(42.dp).clip(PillSenseShape.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            Box(
                Modifier.weight(1f).fillMaxHeight().clip(PillSenseShape.large)
                    .background(if (selectedIndex == index) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelect(index) },
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (selectedIndex == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
            }
        }
    }
}
