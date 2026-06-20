package com.techayan.pdftools.ui.imagetopdf

import android.net.Uri

data class SelectedImage(
    val id: Long,
    val sourceUri: Uri,
    val localUri: Uri,
    val localPath: String,
    val displayName: String,
    val mimeType: String
)

data class GeneratedPdf(
    val uri: Uri,
    val fileName: String,
    val savedLocation: String,
    val skippedImages: List<String> = emptyList()
)

data class ImageToPdfUiState(
    val selectedImages: List<SelectedImage> = emptyList(),
    val pdfName: String = "",
    val isImporting: Boolean = false,
    val isCreatingPdf: Boolean = false,
    val generatedPdf: GeneratedPdf? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)
