package com.techayan.pdftools.ui.imagetopdf

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun ImageToPdfScreen(
    viewModel: ImageToPdfViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.addImages(uris)
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.generatePdf()
        } else {
            viewModel.showError("Storage permission is required to save PDFs on Android 8 and 9.")
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.statusMessage) {
        val message = uiState.errorMessage ?: uiState.statusMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
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
            SelectionActionsCard(
                selectedCount = uiState.selectedImages.size,
                isGenerating = uiState.isGenerating,
                onSelectImages = {
                    imagePickerLauncher.launch(SUPPORTED_IMAGE_MIME_TYPES)
                },
                onGeneratePdf = {
                    if (needsLegacyStoragePermission(context)) {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        viewModel.generatePdf()
                    }
                }
            )
        }

        if (uiState.isGenerating) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        uiState.generatedPdf?.let { pdf ->
            item {
                GeneratedPdfCard(
                    pdf = pdf,
                    onOpen = {
                        openPdf(
                            context = context,
                            pdf = pdf,
                            onError = viewModel::showError
                        )
                    },
                    onShare = {
                        sharePdf(
                            context = context,
                            pdf = pdf,
                            onError = viewModel::showError
                        )
                    }
                )
            }
        }

        item {
            Text(
                text = "Selected Images",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.selectedImages.isEmpty()) {
            item {
                EmptySelectionCard()
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
            Text(
                text = "Image to PDF",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select JPG, JPEG, PNG, or WEBP images, arrange them in order, then generate a shareable PDF.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SelectionActionsCard(
    selectedCount: Int,
    isGenerating: Boolean,
    onSelectImages: () -> Unit,
    onGeneratePdf: () -> Unit
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Saved PDFs go to Documents/TechayanPDF.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = onSelectImages,
                    enabled = !isGenerating,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Select Images")
                }
                Button(
                    onClick = onGeneratePdf,
                    enabled = selectedCount > 0 && !isGenerating,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (isGenerating) "Generating" else "Generate PDF")
                }
            }
        }
    }
}

@Composable
private fun GeneratedPdfCard(
    pdf: GeneratedPdf,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PDF ready",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = pdf.fileName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pdf.savedLocation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onOpen) {
                    Text(text = "Open PDF")
                }
                Button(onClick = onShare) {
                    Text(text = "Share PDF")
                }
            }
        }
    }
}

@Composable
private fun EmptySelectionCard() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No images selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Use Select Images to import JPG, JPEG, PNG, or WEBP files.",
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
            ImageThumbnail(uri = image.uri)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${index + 1}. ${image.name}",
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
            .size(width = 76.dp, height = 96.dp)
            .clip(RoundedCornerShape(16.dp))
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
            decodeThumbnail(context, uri)?.asImageBitmap()
        }
    }
}

private fun decodeThumbnail(
    context: Context,
    uri: Uri
): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val largestEdge = max(info.size.width, info.size.height).coerceAtLeast(1)
                val sampleSize = (largestEdge / THUMBNAIL_MAX_SIZE).coerceAtLeast(1)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(sampleSize)
            }
        } else {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            val largestEdge = max(options.outWidth, options.outHeight).coerceAtLeast(1)
            val sampleSize = (largestEdge / THUMBNAIL_MAX_SIZE).coerceAtLeast(1)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            }
        }
    }.getOrNull()
}

private fun needsLegacyStoragePermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
}

private fun openPdf(
    context: Context,
    pdf: GeneratedPdf,
    onError: (String) -> Unit
) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(pdf.uri, ImageToPdfRepository.PDF_MIME_TYPE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

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
    pdf: GeneratedPdf,
    onError: (String) -> Unit
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = ImageToPdfRepository.PDF_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, pdf.uri)
        putExtra(Intent.EXTRA_SUBJECT, pdf.fileName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

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
