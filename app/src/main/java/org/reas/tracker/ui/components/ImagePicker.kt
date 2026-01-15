package org.reas.tracker.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun ImagePicker(
    showing: Boolean,
    onDismiss: () -> Unit,
    onSelected: (Uri) -> Unit
) {
    if (PickVisualMedia.isPhotoPickerAvailable(LocalContext.current)) {
        val imageRequestLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                onSelected(uri)
            }
            onDismiss()
        }
        LaunchedEffect(showing) {
            if (showing) {
                imageRequestLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }
        }
    } else {
        val imageRequestLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
            if (uri != null) {
                onSelected(uri)
            }
            onDismiss()
        }
        LaunchedEffect(showing) {
            if (showing) {
                imageRequestLauncher.launch(arrayOf("image/*"))
            }
        }
    }
}