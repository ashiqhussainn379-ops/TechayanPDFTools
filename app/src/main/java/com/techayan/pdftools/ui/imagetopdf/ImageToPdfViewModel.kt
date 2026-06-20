package com.techayan.pdftools.ui.imagetopdf

import android.app.Application
import android.content.Intent
import android.database.Cursor
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageToPdfViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ImageToPdfRepository(application.applicationContext)
    private val contentResolver = application.contentResolver
    private val importedImagesDirectory = File(application.filesDir, IMPORTED_IMAGES_DIRECTORY)

    private val _uiState = MutableStateFlow(
        ImageToPdfUiState(pdfName = defaultPdfName())
    )
    val uiState: StateFlow<ImageToPdfUiState> = _uiState.asStateFlow()

    private var nextImageId = 0L

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return

        val existingSourceUris = _uiState.value.selectedImages.map { it.sourceUri }.toSet()
        val newUris = uris.distinct().filterNot { it in existingSourceUris }

        if (newUris.isEmpty()) {
            _uiState.update { it.copy(statusMessage = "Those images are already selected.") }
            return
        }

        _uiState.update {
            it.copy(
                isImporting = true,
                generatedPdf = null,
                errorMessage = null,
                statusMessage = null
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                importImages(newUris)
            }

            _uiState.update { state ->
                state.copy(
                    selectedImages = state.selectedImages + result.acceptedImages,
                    isImporting = false,
                    generatedPdf = null,
                    statusMessage = result.acceptedImages.takeIf { it.isNotEmpty() }?.let {
                        "${it.size} image${if (it.size == 1) "" else "s"} added."
                    },
                    errorMessage = result.rejectedMessages.takeIf { it.isNotEmpty() }?.let {
                        it.joinToString(separator = "\n")
                    }
                )
            }
        }
    }

    fun updatePdfName(pdfName: String) {
        _uiState.update {
            it.copy(
                pdfName = pdfName,
                generatedPdf = null
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
            state.selectedImages.firstOrNull { it.id == imageId }?.let(::deleteLocalImage)

            state.copy(
                selectedImages = state.selectedImages.filterNot { it.id == imageId },
                generatedPdf = null,
                statusMessage = "Image removed.",
                errorMessage = null
            )
        }
    }

    fun createPdf() {
        val state = _uiState.value
        val images = state.selectedImages
        val pdfName = state.pdfName.trim()

        when {
            images.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Select at least one image before creating a PDF.") }
                return
            }

            pdfName.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Enter a PDF name.") }
                return
            }
        }

        _uiState.update {
            it.copy(
                isCreatingPdf = true,
                generatedPdf = null,
                errorMessage = null,
                statusMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.createPdf(images = images, requestedName = pdfName)
            }.onSuccess { generatedPdf ->
                _uiState.update {
                    it.copy(
                        isCreatingPdf = false,
                        generatedPdf = generatedPdf,
                        statusMessage = "PDF saved to ${generatedPdf.savedLocation}."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isCreatingPdf = false,
                        errorMessage = throwable.message ?: "Unable to create PDF. Please try again."
                    )
                }
            }
        }
    }

    fun createAnotherPdf() {
        _uiState.value.selectedImages.forEach(::deleteLocalImage)
        _uiState.update {
            ImageToPdfUiState(pdfName = defaultPdfName())
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, statusMessage = null)
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun openIntentFor(pdf: GeneratedPdf): Intent {
        return repository.createOpenIntent(pdf)
    }

    fun shareIntentFor(pdf: GeneratedPdf): Intent {
        return repository.createShareIntent(pdf)
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.selectedImages.forEach(::deleteLocalImage)
    }

    private fun importImages(uris: List<Uri>): ImportResult {
        if (!importedImagesDirectory.exists() && !importedImagesDirectory.mkdirs()) {
            return ImportResult(
                acceptedImages = emptyList(),
                rejectedMessages = listOf("Unable to prepare the image import folder.")
            )
        }

        val acceptedImages = mutableListOf<SelectedImage>()
        val rejectedMessages = mutableListOf<String>()

        uris.forEach { sourceUri ->
            persistReadPermission(sourceUri)

            val metadata = readMetadata(sourceUri)
            if (metadata == null) {
                rejectedMessages += "Unsupported file skipped. Use JPG, JPEG, PNG, or WEBP images."
                return@forEach
            }

            val imageId = nextImageId++
            val localFile = File(
                importedImagesDirectory,
                "selected_${imageId}.${metadata.extension}"
            )

            val importResult = runCatching {
                copyUriToFile(sourceUri = sourceUri, destination = localFile)

                val localUri = Uri.fromFile(localFile)
                if (!repository.canDecodeImage(localUri)) {
                    localFile.delete()
                    throw IOException("Unable to decode ${metadata.displayName}.")
                }

                SelectedImage(
                    id = imageId,
                    sourceUri = sourceUri,
                    localUri = localUri,
                    displayName = metadata.displayName,
                    mimeType = metadata.mimeType
                )
            }

            importResult.onSuccess { selectedImage ->
                acceptedImages += selectedImage
            }.onFailure {
                localFile.delete()
                rejectedMessages += "${metadata.displayName} could not be read. Please choose it again from the picker."
            }
        }

        return ImportResult(
            acceptedImages = acceptedImages,
            rejectedMessages = rejectedMessages
        )
    }

    private fun copyUriToFile(
        sourceUri: Uri,
        destination: File
    ) {
        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destination).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("Unable to read selected image.")
    }

    private fun persistReadPermission(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun readMetadata(uri: Uri): ImageMetadata? {
        val displayName = queryDisplayName(uri) ?: "Image_${nextImageId + 1}"
        val mimeType = normalizeMimeType(
            mimeType = runCatching { contentResolver.getType(uri) }.getOrNull(),
            displayName = displayName
        ) ?: return null

        return ImageMetadata(
            displayName = displayName,
            mimeType = mimeType,
            extension = extensionForMimeType(mimeType)
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use(::readDisplayName)
        }.getOrNull()
    }

    private fun readDisplayName(cursor: Cursor): String? {
        if (!cursor.moveToFirst()) return null

        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        return if (index >= 0) cursor.getString(index) else null
    }

    private fun normalizeMimeType(
        mimeType: String?,
        displayName: String
    ): String? {
        val lowerMimeType = mimeType?.lowercase()
        if (lowerMimeType == "image/jpg") return "image/jpeg"
        if (lowerMimeType in SUPPORTED_MIME_TYPES) return lowerMimeType

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
                    statusMessage = "Image order updated.",
                    errorMessage = null
                )
            }
        }
    }

    private fun deleteLocalImage(image: SelectedImage) {
        runCatching {
            image.localUri.path?.let(::File)?.delete()
        }
    }

    private data class ImageMetadata(
        val displayName: String,
        val mimeType: String,
        val extension: String
    )

    private data class ImportResult(
        val acceptedImages: List<SelectedImage>,
        val rejectedMessages: List<String>
    )

    companion object {
        private const val IMPORTED_IMAGES_DIRECTORY = "image_to_pdf_imports"

        private val SUPPORTED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp"
        )

        fun defaultPdfName(): String {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            return "Techayan_PDF_$timestamp"
        }
    }
}
