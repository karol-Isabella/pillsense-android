package com.pillsense.app.feature.adherence.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.pillsense.app.R

@Composable
fun ConsentDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.consent_dialog_title)) },
        text = {
            Text(
                stringResource(R.string.consent_dialog_message),
                textAlign = TextAlign.Left
            )
        },
        confirmButton = {
            Button(onClick = onAccept) { Text(stringResource(R.string.consent_accept)) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.consent_later)) }
        }
    )
}
