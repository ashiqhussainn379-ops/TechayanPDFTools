package com.techayan.pdftools.ui.imagetopdf

import android.app.Application
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ImageToPdfViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ImageToPdfRepository(application.applicationContext)
    private val contentResolver = application.contentResolver
    private val cacheDirectory = File(application.cacheDir, SELECTED_IMAGE_CACHE_DIRECTORY)

    private val _uiState = MutableStateFlow(ImageToPdfUiState())
    val uiState: StateFlow<ImageToPdfUiState> = _uiState.asStateFlow()

    private var nextImageId = 0L

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return

        val currentSourceUris = _uiState.value.selectedImages.map { it.sourceUri }.toSet()
        val newUris = uris.distinct().filterNot { it in currentSourceUris }
        if (newUris.isEmpty()) {
            _uiState.update { it.copy(statusMessage = "Those images are already selected.") }
            return
        }

        _uiState.update {
            it.copy(
                isImporting = true,
                errorMessage = null,
                statusMessage = null
            )
        }

        viewModelScope.launch {
            val importResult = withContext(Dispatchers.IO) {
                importImages(newUris)
            }

            _uiState.update { state ->
                state.copy(
                    selectedImages = state.selectedImages + importResult.acceptedImages,
                    isImporting = false,
                    generatedPdf = null,
                    errorMessage = importResult.rejectedItems.takeIf { it.isNotEmpty() }?.let {
                        "Skipped ${it.size} image${if (it.size == 1) "" else "s"}: ${it.joinToString()}."
                    },
                    statusMessage = importResult.acceptedImages.takeIf { it.isNotEmpty() }?.let {
                        "${it.size} image${if (it.size == 1) "" else "s"} added."
                    }
                )
            }
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
            state.selectedImages.firstOrNull { it.id == imageId }?.let(::deleteCachedImage)
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
                        errorMessage = throwable.message ?: "Unable to generate PDF. Please select the images again and retry."
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

    private fun importImages(uris: List<Uri>): ImportResult {
        val acceptedImages = mutableListOf<SelectedImage>()
        val rejectedItems = mutableListOf<String>()

        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }

        uris.forEach { uri ->
            persistReadPermission(uri)

            val metadata = readImageMetadata(uri)
            if (metadata == null) {
                rejectedItems += "${uri.lastPathSegment ?: "Unknown file"} is not JPG, JPEG, PNG, or WEBP"
                return@forEach
            }

            val imageId = nextImageId++
            val cachedFile = File(cacheDirectory, "selected_${imageId}.${metadata.extension}")

            val importedImage = runCatching {
                copyUriToFile(uri = uri, destination = cachedFile)
                if (!isReadableImage(cachedFile)) {
                    cachedFile.delete()
                    error("Unable to read selected image")
                }

                SelectedImage(
                    id = imageId,
                    sourceUri = uri,
                    uri = Uri.fromFile(cachedFile),
                    name = metadata.name,
                    mimeType = metadata.mimeType
                )
            }.getOrElse {
                cachedFile.delete()
                rejectedItems += "${metadata.name} could not be read"
                null
            }

            importedImage?.let(acceptedImages::add)
        }

        return ImportResult(
            acceptedImages = acceptedImages,
            rejectedItems = rejectedItems
        )
    }

    private fun copyUriToFile(
        uri: Uri,
        destination: File
    ) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destination).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: error("Unable to read selected image")
    }

    private fun isReadableImage(file: File): Boolean {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        return runCatching {
            file.inputStream().use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
            options.outWidth > 0 && options.outHeight > 0
        }.getOrDefault(false)
    }

    private fun deleteCachedImage(image: SelectedImage) {
        if (image.uri.scheme == "file") {
            runCatching {
                image.uri.path?.let(::File)?.delete()
            }
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
        val normalizedMimeType = normalizeMimeType(resolverMimeType, displayName) ?: return null

        return if (normalizedMimeType in SUPPORTED_MIME_TYPES) {
            ImageMetadata(
                name = displayName,
                mimeType = normalizedMimeType,
                extension = extensionForMimeType(normalizedMimeType)
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
    ): String? {
        val lowerMime = mimeType?.lowercase()
        if (lowerMime == "image/jpg") return "image/jpeg"
        if (lowerMime in SUPPORTED_MIME_TYPES) return lowerMime

        return when (displayName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> null
        }
    }

    private fun extensionForMimeType(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "img"
        }
    }

    private data class ImageMetadata(
        val name: String,
        val mimeType: String,
        val extension: String
    )

    private data class ImportResult(
        val acceptedImages: List<SelectedImage>,
        val rejectedItems: List<String>
    )

    companion object {
        private const val SELECTED_IMAGE_CACHE_DIRECTORY = "image_to_pdf_selected"

        val SUPPORTED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp"
        )
    }
}
