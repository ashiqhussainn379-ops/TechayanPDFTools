package com.techayan.pdftools.ui.imagetopdf

import android.net.Uri

data class SelectedImage(
    val id: Long,
    val sourceUri: Uri,
    val uri: Uri,
    val name: String,
    val mimeType: String
)

data class GeneratedPdf(
    val uri: Uri,
    val fileName: String,
    val savedLocation: String
)

data class ImageToPdfUiState(
    val selectedImages: List<SelectedImage> = emptyList(),
    val isImporting: Boolean = false,
    val isGenerating: Boolean = false,
    val generatedPdf: GeneratedPdf? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)
