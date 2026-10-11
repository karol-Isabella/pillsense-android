package com.pillsense.app.feature.adherence.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ConsentDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Análisis con IA") },
        text = {
            Text(
                "Enviaremos solo estadísticas anónimas (sin nombres, correos ni fechas exactas). " +
                "No incluiremos información personal ni detalles de medicamentos. " +
                "¿Aceptas usar IA para mejorar tus recomendaciones?",
                textAlign = TextAlign.Left
            )
        },
        confirmButton = {
            Button(onClick = onAccept) { Text("Aceptar") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Ahora no") }
        }
    )
}
