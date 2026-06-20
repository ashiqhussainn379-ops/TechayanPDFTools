package com.techayan.pdftools.ui.imagetopdf

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import kotlin.math.max

@Composable
fun ImageToPdfScreen(viewModel: ImageToPdfViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.addImages(uris)
    }

    uiState.generatedPdf?.let { pdf ->
        ImageToPdfSuccessScreen(
            pdf = pdf,
            onOpenPdf = {
                openPdf(
                    context = context,
                    intent = viewModel.openIntentFor(pdf),
                    onError = viewModel::showError
                )
            },
            onSharePdf = {
                sharePdf(
                    context = context,
                    intent = viewModel.shareIntentFor(pdf),
                    onError = viewModel::showError
                )
            },
            onCreateAnother = viewModel::createAnotherPdf,
            message = uiState.statusMessage,
            errorMessage = uiState.errorMessage,
            onClearMessages = viewModel::clearMessages
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ImageToPdfHeader()
        }

        item {
            ImageToPdfControls(
                pdfName = uiState.pdfName,
                selectedCount = uiState.selectedImages.size,
                isBusy = uiState.isImporting || uiState.isCreatingPdf,
                isImporting = uiState.isImporting,
                isCreatingPdf = uiState.isCreatingPdf,
                onPdfNameChanged = viewModel::updatePdfName,
                onSelectImages = {
                    picker.launch(SUPPORTED_IMAGE_MIME_TYPES)
                },
                onCreatePdf = viewModel::createPdf
            )
        }

        if (uiState.isImporting || uiState.isCreatingPdf) {
            item {
                BusyCard(
                    text = if (uiState.isImporting) {
                        "Importing selected images..."
                    } else {
                        "Creating your PDF..."
                    }
                )
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                MessageCard(
                    title = "Action needed",
                    message = message,
                    isError = true,
                    onDismiss = viewModel::clearMessages
                )
            }
        }

        uiState.statusMessage?.let { message ->
            item {
                MessageCard(
                    title = "Status",
                    message = message,
                    isError = false,
                    onDismiss = viewModel::clearMessages
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Selected Images",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(100)
                ) {
                    Text(
                        text = "${uiState.selectedImages.size} selected",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (uiState.selectedImages.isEmpty()) {
            item {
                EmptyImageSelectionCard()
            }
        } else {
            itemsIndexed(
                items = uiState.selectedImages,
                key = { _, image -> image.id }
            ) { index, image ->
                SelectedImageCard(
                    image = image,
                    index = index,
                    totalCount = uiState.selectedImages.size,
                    onMoveUp = { viewModel.moveImageUp(image.id) },
                    onMoveDown = { viewModel.moveImageDown(image.id) },
                    onRemove = { viewModel.removeImage(image.id) }
                )
            }
        }
    }
}

@Composable
private fun ImageToPdfHeader() {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Convert",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Image to PDF",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select images from Gallery, Google Photos, WhatsApp, Gmail, Downloads, or any SAF provider. The app imports readable copies before creating your PDF.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ImageToPdfControls(
    pdfName: String,
    selectedCount: Int,
    isBusy: Boolean,
    isImporting: Boolean,
    isCreatingPdf: Boolean,
    onPdfNameChanged: (String) -> Unit,
    onSelectImages: () -> Unit,
    onCreatePdf: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "PDF details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextField(
                value = pdfName,
                onValueChange = onPdfNameChanged,
                label = { Text(text = "PDF name") },
                singleLine = true,
                enabled = !isBusy,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "The generated file is saved to Documents/TechayanPDF.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = onSelectImages,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (isImporting) "Importing" else "Select Images")
                }
                Button(
                    onClick = onCreatePdf,
                    enabled = selectedCount > 0 && !isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (isCreatingPdf) "Creating" else "Create PDF")
                }
            }
        }
    }
}

@Composable
private fun BusyCard(text: String) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onDismiss) {
                Text(text = "Dismiss")
            }
        }
    }
}

@Composable
private fun EmptyImageSelectionCard() {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IMG",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "No images selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose JPG, JPEG, PNG, or WEBP images to begin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedImageCard(
    image: SelectedImage,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ImageThumbnail(uri = image.localUri)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${index + 1}. ${image.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = image.mimeType.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = onMoveUp,
                        enabled = index > 0
                    ) {
                        Text(text = "Up")
                    }
                    TextButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1
                    ) {
                        Text(text = "Down")
                    }
                    TextButton(onClick = onRemove) {
                        Text(text = "Remove")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(uri: Uri) {
    val thumbnail by rememberImageThumbnail(uri)

    Box(
        modifier = Modifier
            .size(width = 82.dp, height = 104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!,
                contentDescription = "Selected image preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "IMG",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun rememberImageThumbnail(uri: Uri): State<androidx.compose.ui.graphics.ImageBitmap?> {
    val context = LocalContext.current

    return produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            decodeThumbnail(context = context, uri = uri)?.asImageBitmap()
        }
    }
}

private fun decodeThumbnail(
    context: Context,
    uri: Uri
): Bitmap? {
    return runCatching {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        openThumbnailStream(context, uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val largestEdge = max(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
        val sampleSize = (largestEdge / THUMBNAIL_MAX_SIZE).coerceAtLeast(1)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        openThumbnailStream(context, uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
    }.getOrNull()
}

private fun openThumbnailStream(
    context: Context,
    uri: Uri
): InputStream? {
    return if (uri.scheme == ContentResolver.SCHEME_FILE) {
        uri.path?.let(::File)?.inputStream()
    } else {
        context.contentResolver.openInputStream(uri)
    }
}

@Composable
private fun ImageToPdfSuccessScreen(
    pdf: GeneratedPdf,
    onOpenPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onCreateAnother: () -> Unit,
    message: String?,
    errorMessage: String?,
    onClearMessages: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "PDF created successfully",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pdf.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pdf.savedLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        errorMessage?.let {
            item {
                MessageCard(
                    title = "Action needed",
                    message = it,
                    isError = true,
                    onDismiss = onClearMessages
                )
            }
        }

        message?.let {
            item {
                MessageCard(
                    title = "Saved",
                    message = it,
                    isError = false,
                    onDismiss = onClearMessages
                )
            }
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenPdf,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Open PDF")
                    }
                    FilledTonalButton(
                        onClick = onSharePdf,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Share PDF")
                    }
                    OutlinedButton(
                        onClick = onCreateAnother,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Create another PDF")
                    }
                }
            }
        }
    }
}

private fun openPdf(
    context: Context,
    intent: Intent,
    onError: (String) -> Unit
) {
    try {
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    } catch (_: ActivityNotFoundException) {
        onError("No app is available to open PDF files.")
    } catch (_: Exception) {
        onError("Unable to open this PDF.")
    }
}

private fun sharePdf(
    context: Context,
    intent: Intent,
    onError: (String) -> Unit
) {
    try {
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    } catch (_: ActivityNotFoundException) {
        onError("No app is available to share PDF files.")
    } catch (_: Exception) {
        onError("Unable to share this PDF.")
    }
}

private val SUPPORTED_IMAGE_MIME_TYPES = arrayOf(
    "image/jpeg",
    "image/png",
    "image/webp"
)

private const val THUMBNAIL_MAX_SIZE = 512
