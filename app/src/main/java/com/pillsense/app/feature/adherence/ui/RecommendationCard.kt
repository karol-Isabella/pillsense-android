package com.pillsense.app.feature.adherence.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pillsense.app.core.designsystem.PsCard
import com.pillsense.app.core.designsystem.PsBadge

@Composable
fun RecommendationCard(
    title: String,
    text: String,
    source: String,
    onApply: (Int) -> Unit = {},
    hasAction: Boolean = false
) {
    var showConfirm by remember { mutableStateOf(false) }

    PsCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(text)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PsBadge(source)
                if (hasAction) {
                    Button(onClick = { showConfirm = true }, modifier = Modifier.weight(1f)) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Aplicar cambios") },
            text = { Text("Se desplazarán los recordatorios según la recomendación. Puedes deshacer después.") },
            confirmButton = {
                Button(onClick = { onApply(30); showConfirm = false }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
