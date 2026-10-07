package com.pillsense.app.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.*
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.model.Schedule
import com.pillsense.app.core.util.permissions.*
import com.pillsense.app.feature.auth.ui.LoginScreen
import com.pillsense.app.feature.intake.ui.*
import com.pillsense.app.feature.medication.ui.confirm.*
import com.pillsense.app.feature.medication.ui.scan.*
import com.pillsense.app.feature.adherence.ui.InsightsScreen
import com.pillsense.app.feature.emergency.ui.ProfileScreen
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PillSenseNavigation(viewModel: AppViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val rawHealth by viewModel.health.collectAsState()
    val health = HealthState(rawHealth.medications.filter { it.owner == user }, rawHealth.intakes.filter { it.owner == user }, rawHealth.contacts.filter { it.owner == user })
    val busy by viewModel.busy.collectAsState()
    val error by viewModel.error.collectAsState()
    val dark by viewModel.dark.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current
    SideEffect {
        (context as? android.app.Activity)?.window?.let { window ->
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    val lifecycle = LocalLifecycleOwner.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var ready by remember { mutableStateOf(false) }
    var permissionIssue by remember { mutableStateOf(false) }
    fun checkPermissions() {
        ready = PermissionManager.isGranted(context, AppPermission.NOTIFICATIONS) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled() && PermissionManager.isGranted(context, AppPermission.EXACT_ALARM)
        permissionIssue = !ready
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) { checkPermissions(); viewModel.refresh() } }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(30_000) } }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "today"
    var scannedName by rememberSaveable { mutableStateOf("") }
    var scannedDose by rememberSaveable { mutableStateOf("") }
    var scannedForm by rememberSaveable { mutableStateOf("") }
    var scannedFrequency by rememberSaveable { mutableStateOf("") }
    var scannedDuration by rememberSaveable { mutableStateOf("") }
    var scannedInstructions by rememberSaveable { mutableStateOf("") }
    var scannedOriginal by rememberSaveable { mutableStateOf("") }
    var hasScan by rememberSaveable { mutableStateOf(false) }
    var pendingSave by remember { mutableStateOf<ConfirmMedicationData?>(null) }
    var navUser by rememberSaveable { mutableStateOf(user) }
    LaunchedEffect(user) {
        if (navUser == user) return@LaunchedEffect
        navUser = user
        hasScan = false
        scannedName = ""; scannedDose = ""; scannedForm = ""; scannedFrequency = ""
        scannedDuration = ""; scannedInstructions = ""; scannedOriginal = ""; pendingSave = null
        listOf("history", "profile", "insights", "scan", "confirm").forEach { nav.clearBackStack(it) }
        nav.popBackStack("today", false)
    }
    fun finishPermissions() {
        checkPermissions()
        pendingSave?.let { data ->
            pendingSave = null
            viewModel.save(data) { nav.popBackStack("today", false) }
        }
        viewModel.refresh()
    }
    val exact = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { finishPermissions() }
    fun requestExact() {
        if (Build.VERSION.SDK_INT >= 31 && !PermissionManager.isGranted(context, AppPermission.EXACT_ALARM)) {
            try { exact.launch(PermissionManager.exactAlarmIntent(context)) } catch (_: Exception) { finishPermissions() }
        } else finishPermissions()
    }
    val notificationSettings = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { requestExact() }
    val notification = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { requestExact() }
    fun permissions() {
        val permissionPrefs = context.getSharedPreferences("permission-prompts", android.content.Context.MODE_PRIVATE)
        if (!PermissionManager.isGranted(context, AppPermission.NOTIFICATIONS) && !permissionPrefs.getBoolean("notifications-asked", false)) {
            permissionPrefs.edit().putBoolean("notifications-asked", true).apply()
            PermissionManager.requestPermission(notification, AppPermission.NOTIFICATIONS)
        } else if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            notificationSettings.launch(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
        } else requestExact()
    }
    val localizedContext = remember(context, language) {
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(language))
        android.view.ContextThemeWrapper(context, 0).apply { applyOverrideConfiguration(config) }
    }
    CompositionLocalProvider(LocalContext provides localizedContext) {
        PillSenseTheme(darkTheme = dark) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (user == null) {
                    LoginScreen(onLoginSuccess = viewModel::authenticate, busy = busy, authError = error,
                        dark = dark, onThemeChange = viewModel::toggleTheme, language = language, onLanguageChange = viewModel::toggleLanguage)
                } else {
                    val tabs = listOf("today", "history", "scan", "insights", "profile")
                    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                        error?.let { message ->
                            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(message, Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                                TextButton(onClick = { viewModel.error.value = null }) { Text("Cerrar") }
                            }
                        }
                        if (permissionIssue && route == "today") TextButton(onClick = { permissions() }, modifier = Modifier.fillMaxWidth()) { Text("Activar permisos de recordatorios") }
                        NavHost(navController = nav, startDestination = "today", modifier = Modifier.weight(1f)) {
                            composable("today") {
                                val zone = ZoneId.systemDefault()
                                val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
                                val names = health.medications.associateBy { it.id }
                                val doses = health.intakes.filter { Instant.ofEpochMilli(it.scheduledAt).atZone(zone).toLocalDate() == today }
                                    .sortedBy { it.postponedUntil ?: it.scheduledAt }.mapNotNull { intake ->
                                        val med = names[intake.medicationId] ?: return@mapNotNull null
                                        DoseItem(intake.id, Instant.ofEpochMilli(intake.postponedUntil ?: intake.scheduledAt).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm")),
                                            med.name, med.dose, med.instructions, DoseStatus.valueOf(intake.status), intake.scheduledAt <= now)
                                    }
                                val recent = health.intakes.filter { it.scheduledAt >= today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli() }
                                TodayScreen(userName = user!!.substringBefore('@').replaceFirstChar { it.uppercase() }, doses = doses,
                                    weeklyAverage = Schedule.adherence(recent, now) ?: 0, dark = dark, onThemeChange = viewModel::toggleTheme,
                                    onScanClick = { nav.navigate("scan") }, onInsightsClick = { nav.navigate("insights") },
                                    onDoseStatusChange = { id, status -> viewModel.act(id, status.name) }, showBottomBar = false)
                            }
                            composable("history") { HistoryScreen(health.intakes, health.medications) }
                            composable("insights") { InsightsScreen(health.intakes, health.medications) }
                            composable("profile") { ProfileScreen(user.orEmpty(), health.medications, health.contacts, dark,
                                viewModel::toggleTheme, { viewModel.logout(); nav.popBackStack("today", false) }, viewModel::archive,
                                viewModel::saveContact, viewModel::removeContact, { permissions() }, ready) }
                            composable("scan") { ScanScreen(onScanComplete = {
                                hasScan = true; scannedName = it.name; scannedDose = it.dose; scannedForm = it.form
                                scannedFrequency = it.frequency; scannedDuration = it.duration; scannedInstructions = it.instructions; scannedOriginal = it.originalText
                                nav.navigate("confirm")
                            }, onClose = { nav.popBackStack() }, onManual = { hasScan = false; nav.navigate("confirm") }) }
                            composable("confirm") { ConfirmScreen(
                                scannedMed = if (hasScan) ScannedMedication(scannedName, scannedDose, scannedForm, scannedFrequency, scannedDuration, scannedInstructions, scannedOriginal) else null,
                                onCancel = { nav.popBackStack() }, onSave = { pendingSave = it; permissions() }, saving = busy,
                            ) }
                        }
                        if (route != "scan" && route != "confirm") {
                            val icons = listOf(R.drawable.ic_lucide_calendar_check, R.drawable.ic_lucide_history, R.drawable.ic_lucide_scan_line, R.drawable.ic_lucide_chart_bars, R.drawable.ic_lucide_user_round)
                            val labels = listOf(stringResource(R.string.tab_today), stringResource(R.string.tab_history), "Escanear", stringResource(R.string.tab_insights), stringResource(R.string.tab_profile))
                            PsBottomBar(icons.mapIndexed { index, icon -> PsBottomBarItem(icon = {
                                if (index == 2) Box(Modifier.size(48.dp).clip(CircleShape).background(Brush.linearGradient(listOf(PrimaryLight, SecondaryLight))), contentAlignment = Alignment.Center) {
                                    Icon(painterResource(icon), null, Modifier.size(26.dp), tint = Color.White)
                                } else Icon(painterResource(icon), null, Modifier.size(24.dp))
                            }, label = labels[index], prominent = index == 2) }, selectedIndex = tabs.indexOf(route).coerceAtLeast(0), onItemSelected = {
                                nav.navigate(tabs[it]) { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true }
                            })
                        }
                    }
                }
            }
        }
    }
}
