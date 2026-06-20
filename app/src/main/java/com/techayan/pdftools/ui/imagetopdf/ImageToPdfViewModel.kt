package com.techayan.pdftools.ui.imagetopdf

import android.app.Application
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageToPdfViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ImageToPdfRepository(application.applicationContext)
    private val contentResolver = application.contentResolver

    private val _uiState = MutableStateFlow(ImageToPdfUiState())
    val uiState: StateFlow<ImageToPdfUiState> = _uiState.asStateFlow()

    private var nextImageId = 0L

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return

        val currentUris = _uiState.value.selectedImages.map { it.uri }.toSet()
        val acceptedImages = mutableListOf<SelectedImage>()
        val rejectedNames = mutableListOf<String>()

        uris.distinct().forEach { uri ->
            if (uri in currentUris) return@forEach

            persistReadPermission(uri)

            val metadata = readImageMetadata(uri)
            if (metadata == null) {
                rejectedNames += uri.lastPathSegment ?: "Unknown file"
            } else {
                acceptedImages += SelectedImage(
                    id = nextImageId++,
                    uri = uri,
                    name = metadata.name,
                    mimeType = metadata.mimeType
                )
            }
        }

        _uiState.update { state ->
            state.copy(
                selectedImages = state.selectedImages + acceptedImages,
                generatedPdf = null,
                errorMessage = rejectedNames.takeIf { it.isNotEmpty() }?.let {
                    "Unsupported image skipped: ${it.joinToString()}. Use JPG, JPEG, PNG, or WEBP."
                },
                statusMessage = acceptedImages.takeIf { it.isNotEmpty() }?.let {
                    "${it.size} image${if (it.size == 1) "" else "s"} added."
                }
            )
        }
    }

    fun moveImageUp(imageId: Long) {
        moveImage(imageId = imageId, offset = -1)
    }

    fun moveImageDown(imageId: Long) {
        moveImage(imageId = imageId, offset = 1)
    }

    fun removeImage(imageId: Long) {
        _uiState.update { state ->
            state.copy(
                selectedImages = state.selectedImages.filterNot { it.id == imageId },
                generatedPdf = null,
                statusMessage = "Image removed."
            )
        }
    }

    fun generatePdf() {
        val images = _uiState.value.selectedImages
        if (images.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Select at least one image before generating a PDF.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isGenerating = true,
                errorMessage = null,
                statusMessage = null,
                generatedPdf = null
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.generatePdf(images)
            }.onSuccess { pdf ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedPdf = pdf,
                        statusMessage = "PDF saved to ${pdf.savedLocation}."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = throwable.message ?: "Unable to generate PDF. Please try again."
                    )
                }
            }
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, statusMessage = null)
        }
    }

    private fun moveImage(
        imageId: Long,
        offset: Int
    ) {
        _uiState.update { state ->
            val images = state.selectedImages.toMutableList()
            val currentIndex = images.indexOfFirst { it.id == imageId }
            val targetIndex = currentIndex + offset

            if (currentIndex == -1 || targetIndex !in images.indices) {
                state
            } else {
                val image = images.removeAt(currentIndex)
                images.add(targetIndex, image)
                state.copy(
                    selectedImages = images,
                    generatedPdf = null,
                    statusMessage = "Image order updated."
                )
            }
        }
    }

    private fun persistReadPermission(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun readImageMetadata(uri: Uri): ImageMetadata? {
        val resolverMimeType = contentResolver.getType(uri)
        val displayName = queryDisplayName(uri) ?: "Image ${nextImageId + 1}"
        val normalizedMimeType = normalizeMimeType(resolverMimeType, displayName)

        return if (normalizedMimeType in SUPPORTED_MIME_TYPES) {
            ImageMetadata(
                name = displayName,
                mimeType = normalizedMimeType
            )
        } else {
            null
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use(::readDisplayName)
        }.getOrNull()
    }

    private fun readDisplayName(cursor: Cursor): String? {
        if (!cursor.moveToFirst()) return null
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        return if (nameIndex >= 0) cursor.getString(nameIndex) else null
    }

    private fun normalizeMimeType(
        mimeType: String?,
        displayName: String
    ): String {
        val lowerMime = mimeType?.lowercase()
        if (lowerMime in SUPPORTED_MIME_TYPES) return lowerMime.orEmpty()

        return when (displayName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> lowerMime.orEmpty()
        }
    }

    private data class ImageMetadata(
        val name: String,
        val mimeType: String
    )

    companion object {
        val SUPPORTED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp"
        )
    }
}
