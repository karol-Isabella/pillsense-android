package com.pillsense.app.feature.medication.ui.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.util.permissions.*
import java.io.File

data class ScannedMedication(
    val name: String, val dose: String, val form: String, val frequency: String,
    val duration: String, val instructions: String = "", val originalText: String = "",
)

@Composable
fun ScanScreen(onScanComplete: (ScannedMedication) -> Unit = {}, onClose: () -> Unit = {},
    onManual: () -> Unit = {}, modifier: Modifier = Modifier, viewModel: ScanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedUri by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { selectedUri = photoUri; viewModel.reset() }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) { selectedUri = uri.toString(); viewModel.reset() } }
    val takePhoto = {
        try {
            val dir = File(context.cacheDir, "photos").apply { mkdirs() }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", File.createTempFile("scan-", ".jpg", dir))
            viewModel.trackCapture(uri)
            photoUri = uri.toString(); camera.launch(uri)
        } catch (_: Exception) { cameraError = "No hay una cámara disponible. Selecciona una imagen de la galería." }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) takePhoto() else cameraError = "Permiso de cámara denegado. Puedes usar la galería."
    }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onClose, enabled = !state.busy) { Text("Cancelar") }
            Text("Escanear medicamento", style = MaterialTheme.typography.titleMedium)
        }
        PsSegmentedControl(listOf("Caja", "Receta"), mode, { mode = it })
        PsCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().heightIn(min = 230.dp).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Icon(painterResource(R.drawable.ic_lucide_scan_line), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(20.dp))
                Text(if (state.busy) "Leyendo el texto de tu foto…" else if (selectedUri != null) "Foto seleccionada" else "Encuadra el nombre y la dosis con buena luz.",
                    style = MaterialTheme.typography.bodyLarge)
                if (state.busy) { Spacer(Modifier.height(20.dp)); CircularProgressIndicator() }
            }
        }
        Text("La lectura se realiza en tu teléfono, sin claves ni internet. Revisa cada dato con la receta antes de guardar.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        (cameraError ?: state.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.result != null) {
            PsCard { Column(Modifier.padding(20.dp)) {
                Text(state.result!!.name.ifBlank { "Texto reconocido" }, style = MaterialTheme.typography.headlineSmall)
                Text(state.result!!.dose)
                Text("Verifica los datos y completa los campos que no aparecen en la foto.")
            } }
            PsButton({ onScanComplete(state.result!!) }, "Revisar datos", Modifier.fillMaxWidth())
        } else if (selectedUri != null) {
            PsButton({ viewModel.analyze(Uri.parse(selectedUri)) }, "Leer foto", Modifier.fillMaxWidth(), enabled = !state.busy)
        }
        PsButton({ cameraError = null; if (PermissionManager.isGranted(context, AppPermission.CAMERA)) takePhoto()
            else PermissionManager.requestPermission(permission, AppPermission.CAMERA) }, "Tomar foto", Modifier.fillMaxWidth(), enabled = !state.busy)
        PsButton({ gallery.launch("image/*") }, "Seleccionar de la galería", Modifier.fillMaxWidth(), enabled = !state.busy, style = PsButtonStyle.Secondary)
        PsButton(onManual, "Registrar manualmente", Modifier.fillMaxWidth(), enabled = !state.busy, style = PsButtonStyle.Text)
    }
}
