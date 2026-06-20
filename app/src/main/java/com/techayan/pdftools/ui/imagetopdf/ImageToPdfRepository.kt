package com.techayan.pdftools.ui.imagetopdf

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class ImageToPdfRepository(
    private val context: Context
) {

    suspend fun createPdf(
        images: List<SelectedImage>,
        requestedName: String
    ): GeneratedPdf = withContext(Dispatchers.IO) {
        if (images.isEmpty()) {
            throw IOException("Select at least one image before creating a PDF.")
        }

        val fileName = normalizePdfName(requestedName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfWithMediaStore(fileName = fileName, images = images)
        } else {
            savePdfToAppDocuments(fileName = fileName, images = images)
        }
    }

    fun createOpenIntent(pdf: GeneratedPdf): android.content.Intent {
        return android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(pdf.uri, PDF_MIME_TYPE)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, pdf.fileName, pdf.uri)
        }
    }

    fun createShareIntent(pdf: GeneratedPdf): android.content.Intent {
        return android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = PDF_MIME_TYPE
            putExtra(android.content.Intent.EXTRA_STREAM, pdf.uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, pdf.fileName)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, pdf.fileName, pdf.uri)
        }
    }

    private fun savePdfWithMediaStore(
        fileName: String,
        images: List<SelectedImage>
    ): GeneratedPdf {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, PDF_MIME_TYPE)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOCUMENTS}/$OUTPUT_DIRECTORY"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val outputUri = resolver.insert(collection, values)
            ?: throw IOException("Unable to create a PDF in Documents/$OUTPUT_DIRECTORY.")

        try {
            val skippedImages = resolver.openOutputStream(outputUri)?.use { outputStream ->
                writePdf(images = images, outputStream = outputStream)
            } ?: throw IOException("Unable to open the PDF output stream.")

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(outputUri, values, null, null)

            return GeneratedPdf(
                uri = outputUri,
                fileName = fileName,
                savedLocation = "Documents/$OUTPUT_DIRECTORY/$fileName",
                skippedImages = skippedImages
            )
        } catch (exception: Exception) {
            resolver.delete(outputUri, null, null)
            throw exception
        }
    }

    private fun savePdfToAppDocuments(
        fileName: String,
        images: List<SelectedImage>
    ): GeneratedPdf {
        val outputDirectory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            OUTPUT_DIRECTORY
        )
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw IOException("Unable to create the PDF output folder.")
        }

        val outputFile = File(outputDirectory, fileName)
        val skippedImages = FileOutputStream(outputFile).use { outputStream ->
            writePdf(images = images, outputStream = outputStream)
        }

        return GeneratedPdf(
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            ),
            fileName = fileName,
            savedLocation = "App Documents/$OUTPUT_DIRECTORY/$fileName",
            skippedImages = skippedImages
        )
    }

    private fun writePdf(
        images: List<SelectedImage>,
        outputStream: java.io.OutputStream
    ): List<String> {
        val pdfDocument = PdfDocument()
        val skippedImages = mutableListOf<String>()
        var pageNumber = 1

        try {
            images.forEach { image ->
                val imageFile = File(image.localPath)
                val bitmap = decodeBitmap(imageFile)
                if (bitmap == null) {
                    skippedImages += image.displayName
                    return@forEach
                }

                try {
                    val pageSize = pageSizeFor(bitmap)
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        pageSize.width,
                        pageSize.height,
                        pageNumber
                    ).create()
                    val page = pdfDocument.startPage(pageInfo)

                    drawBitmapOnPage(
                        canvas = page.canvas,
                        bitmap = bitmap,
                        pageWidth = pageSize.width,
                        pageHeight = pageSize.height
                    )

                    pdfDocument.finishPage(page)
                    pageNumber += 1
                } finally {
                    bitmap.recycle()
                }
            }

            if (pageNumber == 1) {
                throw IOException("No selected images could be decoded. Please choose the images again.")
            }

            pdfDocument.writeTo(outputStream)
            return skippedImages
        } finally {
            pdfDocument.close()
        }
    }

    private fun pageSizeFor(bitmap: Bitmap): PdfPageSize {
        return if (bitmap.width > bitmap.height) {
            PdfPageSize(width = A4_LONG_EDGE, height = A4_SHORT_EDGE)
        } else {
            PdfPageSize(width = A4_SHORT_EDGE, height = A4_LONG_EDGE)
        }
    }

    private fun drawBitmapOnPage(
        canvas: Canvas,
        bitmap: Bitmap,
        pageWidth: Int,
        pageHeight: Int
    ) {
        canvas.drawColor(Color.WHITE)

        val contentWidth = pageWidth - (PAGE_MARGIN * 2)
        val contentHeight = pageHeight - (PAGE_MARGIN * 2)
        val scale = min(
            contentWidth / bitmap.width.toFloat(),
            contentHeight / bitmap.height.toFloat()
        )
        val targetWidth = bitmap.width * scale
        val targetHeight = bitmap.height * scale
        val left = (pageWidth - targetWidth) / 2f
        val top = (pageHeight - targetHeight) / 2f
        val destination = RectF(left, top, left + targetWidth, top + targetHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        canvas.drawBitmap(bitmap, null, destination, paint)
    }

    fun decodeBitmap(file: File): Bitmap? {
        if (!file.exists() || file.length() <= 0L) return null

        return decodeWithImageDecoder(file)
            ?: decodeWithBitmapFactory(file)
    }

    fun canDecodeImage(file: File): Boolean {
        val bitmap = decodeBitmap(file) ?: return false
        bitmap.recycle()
        return true
    }

    private fun decodeWithImageDecoder(file: File): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null

        return runCatching {
            val source = ImageDecoder.createSource(file)
            val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(
                    calculateSampleSize(
                        width = info.size.width,
                        height = info.size.height,
                        maxDimension = MAX_BITMAP_DIMENSION
                    )
                )
            }

            applyExifOrientation(decoded, file)
        }.getOrNull()
    }

    private fun decodeWithBitmapFactory(file: File): Bitmap? {
        return runCatching {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateSampleSize(
                    width = bounds.outWidth,
                    height = bounds.outHeight,
                    maxDimension = MAX_BITMAP_DIMENSION
                )
            }

            val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
            applyExifOrientation(decoded, file)
        }.getOrNull()
    }

    private fun applyExifOrientation(
        bitmap: Bitmap,
        file: File
    ): Bitmap {
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
                if (it != bitmap) bitmap.recycle()
            }
        }.getOrElse {
            bitmap
        }
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int
    ): Int {
        var sampleSize = 1
        val largestEdge = max(width, height)
        while (largestEdge / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        return sampleSize.coerceAtLeast(1)
    }

    private fun normalizePdfName(requestedName: String): String {
        val sanitized = requestedName
            .trim()
            .ifBlank { "Techayan_PDF" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('_', '.', '-')
            .ifBlank { "Techayan_PDF" }

        return if (sanitized.endsWith(".pdf", ignoreCase = true)) {
            sanitized
        } else {
            "$sanitized.pdf"
        }
    }

    private data class PdfPageSize(
        val width: Int,
        val height: Int
    )

    companion object {
        const val PDF_MIME_TYPE = "application/pdf"
        const val OUTPUT_DIRECTORY = "TechayanPDF"
        private const val A4_SHORT_EDGE = 595
        private const val A4_LONG_EDGE = 842
        private const val PAGE_MARGIN = 36
        private const val MAX_BITMAP_DIMENSION = 2480
    }
}
