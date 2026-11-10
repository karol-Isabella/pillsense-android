package com.pillsense.app.feature.medication.ui.scan

import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillsense.app.R
import com.pillsense.app.core.designsystem.*
import com.pillsense.app.core.util.permissions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import com.pillsense.app.core.util.ExifRotation
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
    var selectedUri by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var cameraStarted by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    var scanMode by rememberSaveable { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) { selectedUri = uri.toString(); viewModel.reset() } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraStarted = true else cameraError = "Permiso de cámara denegado. Puedes usar la galería."
    }
    val startCamera = {
        if (PermissionManager.isGranted(context, AppPermission.CAMERA)) cameraStarted = true
        else PermissionManager.requestPermission(permission, AppPermission.CAMERA)
    }
    val takePhoto = {
        try {
            val dir = File(context.cacheDir, "photos").apply { mkdirs() }
            val photoFile = File.createTempFile("scan-", ".jpg", dir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", photoFile)
            val capture = imageCapture
            if (capture == null) cameraError = "La cámara todavía se está preparando."
            else {
                viewModel.trackCapture(uri)
                val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                capture.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(result: ImageCapture.OutputFileResults) { selectedUri = uri.toString(); cameraError = null; viewModel.reset() }
                    override fun onError(exception: ImageCaptureException) { photoFile.delete(); cameraError = "No se pudo capturar la foto. ${exception.message.orEmpty()}" }
                })
            }
        } catch (_: Exception) { cameraError = "No se pudo iniciar la cámara. Selecciona una imagen de la galería." }
    }
    LaunchedEffect(flashEnabled) { activeCamera?.cameraControl?.enableTorch(flashEnabled) }
    DisposableEffect(lifecycleOwner) { onDispose { cameraProvider?.unbindAll() } }
    Column(modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = .45f)).clickable(enabled = !state.busy) { onClose() }, contentAlignment = Alignment.Center) { Text("×", color = Color.White, style = MaterialTheme.typography.headlineMedium) }
            Row(Modifier.clip(RoundedCornerShape(24.dp)).background(Color.Black.copy(alpha = .5f)).padding(3.dp)) {
                listOf(stringResource(R.string.scan_mode_caja), stringResource(R.string.scan_mode_receta)).forEachIndexed { index, label ->
                    Box(Modifier.clip(RoundedCornerShape(22.dp)).background(if (scanMode == index) Color.White.copy(alpha = .92f) else Color.Transparent).clickable { scanMode = index }.padding(horizontal = 16.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = if (scanMode == index) Color(0xFF27231D) else Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Box(Modifier.size(44.dp).clip(CircleShape).background(if (flashEnabled) Color(0xFFFFD60A) else Color.Black.copy(alpha = .45f)).clickable { flashEnabled = !flashEnabled }, contentAlignment = Alignment.Center) {
                Icon(painterResource(if (flashEnabled) R.drawable.ic_lucide_zap else R.drawable.ic_lucide_zap_off), null, tint = if (flashEnabled) Color.Black else Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (selectedUri != null) {
                ThumbnailImage(selectedUri, Modifier.fillMaxSize())
            } else if (cameraStarted) {
                AndroidView(factory = { viewContext ->
                    PreviewView(viewContext).also { previewView ->
                        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        val future = ProcessCameraProvider.getInstance(viewContext)
                        future.addListener({
                            runCatching {
                                val provider = future.get()
                                cameraProvider = provider
                                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .setTargetRotation(previewView.display.rotation)
                                    .build()
                                provider.unbindAll()
                                activeCamera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                                imageCapture = capture
                                activeCamera?.cameraControl?.enableTorch(flashEnabled)
                            }.onFailure { cameraError = "No se pudo abrir la cámara. Puedes elegir una foto de la galería." }
                        }, ContextCompat.getMainExecutor(viewContext))
                    }
                }, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_lucide_scan_line), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                }
            }
            if (selectedUri == null) Box(Modifier.fillMaxWidth(.72f).fillMaxHeight(.62f).border(BorderStroke(3.dp, Color.White.copy(alpha = .95f)), RoundedCornerShape(28.dp))) {
                Box(Modifier.align(Alignment.TopCenter).padding(top = 14.dp).fillMaxWidth(.8f).height(2.dp).background(Color(0xFF30B0C7)))
            }
            if (state.busy) {
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xE6201710)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✧  ANALIZANDO CON IA", color = Color(0xFF64D2FF), style = MaterialTheme.typography.labelMedium)
                    listOf(stringResource(R.string.scan_detecting), stringResource(R.string.scan_identifying), stringResource(R.string.scan_interpreting)).forEachIndexed { index, label ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (index < 2) Text("✓", color = Color.White, modifier = Modifier.clip(CircleShape).background(Color(0xFF30C759)).padding(horizontal = 4.dp)) else CircularProgressIndicator(Modifier.size(16.dp), color = Color(0xFF64D2FF), strokeWidth = 2.dp)
                            Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        if (selectedUri != null && !state.busy && state.result == null) {
            LaunchedEffect(selectedUri) { viewModel.analyze(Uri.parse(selectedUri)) }
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!state.busy && selectedUri == null) Text(stringResource(if (scanMode == 0) R.string.scan_hint_box else R.string.scan_hint_rx), color = Color.White, style = MaterialTheme.typography.bodySmall)
            cameraError?.let { Text(it, color = Color(0xFFFF6961), style = MaterialTheme.typography.bodySmall) }
            if (selectedUri == null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .18f)).clickable(enabled = !state.busy) { gallery.launch("image/*") }, contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_lucide_images), null, tint = Color.White, modifier = Modifier.size(21.dp)) }
                    Box(Modifier.size(76.dp).border(3.dp, Color.White, CircleShape).padding(6.dp).clip(CircleShape).background(Color.White).clickable(enabled = !state.busy && imageCapture != null) { takePhoto() }, contentAlignment = Alignment.Center) {}
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .18f)).clickable(enabled = !state.busy) { viewModel.reset(); onManual() }, contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_lucide_pencil), contentDescription = stringResource(R.string.scan_manual_register), tint = Color.White, modifier = Modifier.size(21.dp))
                    }
                }
            }
            if (state.result != null && selectedUri != null) {
                PsCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(state.result!!.name.ifBlank { stringResource(R.string.scan_photo_hint) }, style = MaterialTheme.typography.titleMedium)
                    Text(state.result!!.dose, style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Icon(painterResource(R.drawable.ic_lucide_lightbulb), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                        Text(stringResource(R.string.confirm_verify), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                } }
                PsButton({ onScanComplete(state.result!!) }, stringResource(R.string.confirm_title), Modifier.fillMaxWidth())
                TextButton(onClick = { selectedUri = null; viewModel.reset() }) { Text(stringResource(R.string.dialog_cancel), color = Color.White) }
                TextButton(onClick = onManual) { Text(stringResource(R.string.scan_manual_register), color = MaterialTheme.colorScheme.primary) }
            }
        }
        LaunchedEffect(Unit) { startCamera() }
    }
}

@Composable
fun ThumbnailImage(selectedUri: String?, modifier: Modifier = Modifier.size(160.dp)) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(selectedUri) {
        if (selectedUri != null) {
            try {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val uri = Uri.parse(selectedUri)
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: throw Exception("No se pudo abrir el archivo")
                        
                        // Leer EXIF y obtener rotación
                        val exif = try {
                            ExifInterface(stream)
                        } catch (e: Exception) {
                            null
                        }
                        stream.close()
                        
                        // Decodificar con inSampleSize para miniatura
                        val stream2 = context.contentResolver.openInputStream(uri)
                            ?: throw Exception("No se pudo abrir el archivo")
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 4 // Reduce tamaño
                        var decodedBitmap = BitmapFactory.decodeStream(stream2, null, options)
                        stream2.close()
                        
                        // Aplicar rotación EXIF
                        if (decodedBitmap != null && exif != null) {
                            val degrees = ExifRotation.getRotationDegrees(exif)
                            decodedBitmap = ExifRotation.rotateBitmap(decodedBitmap, degrees)
                        }
                        
                        decodedBitmap?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                bitmap = result
                loadError = result == null && selectedUri != null
            } catch (e: Exception) {
                loadError = true
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = "Foto seleccionada",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else if (loadError) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(R.drawable.ic_lucide_images), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.scan_photo_selected), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        CircularProgressIndicator()
    }
}
