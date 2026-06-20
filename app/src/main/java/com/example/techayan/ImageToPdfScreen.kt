package com.techayan.pdfeditor

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var createdPdf by remember { mutableStateOf<ImageToPdfResult?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var pendingCreateAfterPermission by remember { mutableStateOf(false) }

    fun createSelectedPdf() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(context, "Please select image first", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isCreating = true
            createdPdf = null

            try {
                val result = withContext(Dispatchers.IO) {
                    ImageToPdfConverter.createPdf(context, selectedImages)
                }
                createdPdf = result
                Toast.makeText(
                    context,
                    "PDF saved in Documents/TechayanPDF",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: Exception) {

                exception.printStackTrace()

                Toast.makeText(
                    context,
                    exception.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isCreating = false
                pendingCreateAfterPermission = false
            }
        }
    }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCreateAfterPermission) {
            createSelectedPdf()
        } else if (!granted) {
            pendingCreateAfterPermission = false
            Toast.makeText(
                context,
                "Storage permission is required on this Android version",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val legacyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris
            createdPdf = null
            Toast.makeText(context, "${uris.size} image selected", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris
            createdPdf = null
            Toast.makeText(context, "${uris.size} image selected", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Techayan PDF Tools") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Image to PDF",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Select one or more images, preview them, then create a PDF saved to Documents/TechayanPDF.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating,
                onClick = {
                    if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } else {
                        legacyPicker.launch(arrayOf("image/*"))
                    }
                }
            ) {
                Text(if (selectedImages.isEmpty()) "Select Image" else "Select Different Images")
            }

            ImagePreviewPanel(selectedImages)

            ElevatedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImages.isNotEmpty() && !isCreating,
                onClick = {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        pendingCreateAfterPermission = true
                        writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        createSelectedPdf()
                    }
                }
            ) {
                if (isCreating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text("Creating PDF")
                    }
                } else {
                    Text("Create PDF")
                }
            }

            createdPdf?.let { pdf ->
                CreatedPdfPanel(
                    result = pdf,
                    onOpen = { PdfShareActions.openPdf(context, pdf.uri) },
                    onShare = { PdfShareActions.sharePdf(context, pdf.uri) }
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewPanel(images: List<Uri>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (images.isEmpty()) 180.dp else 320.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No image selected",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = images.first(),
                    contentDescription = "Selected image preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Fit
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(images) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image thumbnail",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatedPdfPanel(
    result: ImageToPdfResult,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PDF created successfully",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Documents/TechayanPDF/${result.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${result.imageCount} image${if (result.imageCount == 1) "" else "s"} added",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onOpen
                ) {
                    Text("Open PDF")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onShare
                ) {
                    Text("Share PDF")
                }
            }
        }
    }
}
